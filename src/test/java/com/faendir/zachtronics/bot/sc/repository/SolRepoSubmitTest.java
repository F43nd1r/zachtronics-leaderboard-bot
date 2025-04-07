/*
 * Copyright (c) 2025
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.faendir.zachtronics.bot.sc.repository;

import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.repository.CategoryRecord;
import com.faendir.zachtronics.bot.repository.SubmitResult;
import com.faendir.zachtronics.bot.sc.model.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@BotTest
public class SolRepoSubmitTest {

    @Autowired
    private ScSolutionRepository repository;

    @Test
    public void testSubmitScore() {
        // we start with a 100/100/100
        ScScore score = new ScScore(100, 100, 100, true, true);
        assertInstanceOf(SubmitResult.NothingBeaten.class, doSubmitScore(score)); // our score is a BP, fail
        score = new ScScore(100, 100, 100, false, true);
        assertInstanceOf(SubmitResult.NothingBeaten.class, doSubmitScore(score)); // our score is a P, fail
        score = new ScScore(100, 100, 100, false, false);
        assertInstanceOf(SubmitResult.Updated.class, doSubmitScore(score)); // identical score, different content, accept
        assertInstanceOf(SubmitResult.AlreadyPresent.class, doSubmitScore(score)); // identical everything, fail
        score = new ScScore(10, 100, 1000, true, true);
        assertInstanceOf(SubmitResult.Success.class, doSubmitScore(score)); // new frontier piece
        score = new ScScore(1000, 100, 10, true, true);
        assertInstanceOf(SubmitResult.Success.class, doSubmitScore(score)); // new frontier piece
        score = new ScScore(10, 10, 10, true, true);
        assertInstanceOf(SubmitResult.Success.class, doSubmitScore(score)); // beats them both
        score = new ScScore(20, 20, 20, true, true);
        assertInstanceOf(SubmitResult.NothingBeaten.class, doSubmitScore(score)); // falls flat
        score = new ScScore(20, 20, 20, false, true);
        assertInstanceOf(SubmitResult.Success.class, doSubmitScore(score)); // new non bugged record
    }

    @Test
    public void testSubmitData() {
        // we start at 100/100/100
        ScScore score = new ScScore(50, 50, 50, false, false);
        assertInstanceOf(SubmitResult.Success.class, doSubmitScore(score)); // 50/50/50

        String data = "SOLUTION:A Most Unfortunate Malfunction,12345ieee,45-1-14\nbunch of stuff...";
        assertInstanceOf(SubmitResult.Success.class, doSubmitData(data)); // 45/1/14
        assertInstanceOf(SubmitResult.AlreadyPresent.class, doSubmitData(data)); // identical

        data = "SOLUTION:A Most Unfortunate Malfunction,12345ieee,45-1-14\ndifferent stuff...";
        assertInstanceOf(SubmitResult.Updated.class, doSubmitData(data)); // changed data, I can

        data = "SOLUTION:A Most Unfortunate Malfunction,BadGuy,45-1-14\ndifferent stuff...";
        assertInstanceOf(SubmitResult.AlreadyPresent.class, doSubmitData(data)); // stealing is bad

        data = "SOLUTION:A Most Unfortunate Malfunction,BadGuy,50-1-50\nsome more stuff...";
        assertInstanceOf(SubmitResult.NothingBeaten.class, doSubmitData(data)); // just give up, man
    }

    @Test
    public void testSubmitNewCategories() {
        // we start with a 100/100/100 that holds no categories
        ScScore score = new ScScore(50, 50, 50, false, false);
        SubmitResult.Success<ScRecord, ScCategory> result = (SubmitResult.Success<ScRecord, ScCategory>) doSubmitScore(score);
        Set<ScCategory> wonCategories = result.getBeatenRecords().stream()
                                              .map(CategoryRecord::getCategories)
                                              .flatMap(Set::stream)
                                              .collect(Collectors.toCollection(() -> EnumSet.noneOf(ScCategory.class)));
        Set<ScCategory> supportedCategories = EnumSet.copyOf(ScPuzzle.bonding_boss.getSupportedCategories());
        assertEquals(supportedCategories, wonCategories);
    }

    @Test
    public void testSubmitDataVideo() {
        // we start at 100/100/100
        String data = "SOLUTION:A Most Unfortunate Malfunction,12345ieee,100-100-100\nhas video";
        assertInstanceOf(SubmitResult.Updated.class, doSubmitDataVideo(data, "http://my.video")); // add video

        data = "SOLUTION:A Most Unfortunate Malfunction,AnotherGuy,100-100-100\ndifferent video";
        assertInstanceOf(SubmitResult.Updated.class, doSubmitDataVideo(data, "http://his.video")); // with a video you can steal

        data = "SOLUTION:A Most Unfortunate Malfunction,AnotherGuy,100-100-100\nchanged data";
        assertInstanceOf(SubmitResult.AlreadyPresent.class, doSubmitDataVideo(data, null)); // cannot regress video state
    }

    @NotNull
    private SubmitResult<ScRecord, ScCategory> doSubmitScore(@NotNull ScScore score) {
        String data = "SOLUTION:A Most Unfortunate Malfunction,12345ieee," + score.toExportString();
        return doSubmitData(data);
    }

    @NotNull
    private SubmitResult<ScRecord, ScCategory> doSubmitData(String data) {
        return doSubmitDataVideo(data, null);
    }

    @NotNull
    private SubmitResult<ScRecord, ScCategory> doSubmitDataVideo(String data, String displayLink) {
        return repository.submit(ScSubmission.fromDataNoValidation(data, null, displayLink));
    }
}
