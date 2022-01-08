/*
 * Copyright (c) 2021
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
import com.faendir.zachtronics.bot.model.DisplayContext;
import com.faendir.zachtronics.bot.repository.SubmitResult;
import com.faendir.zachtronics.bot.sc.model.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@BotTest
public class SolRepoSubmitTest {

    @Autowired
    private ScSolutionRepository repository;

    @Test
    public void testArchiveScore() {
        // we start with a 100/100/100
        ScScore score = new ScScore(100, 100, 100, true, true);
        assertInstanceOf(SubmitResult.NothingBeaten.class, doSubmitScore(score)); // our score is a BP, fail
        score = new ScScore(100, 100, 100, false, true);
        assertInstanceOf(SubmitResult.NothingBeaten.class, doSubmitScore(score)); // our score is a P, fail
        score = new ScScore(100, 100, 100, false, false);
        assertInstanceOf(SubmitResult.Success.class, doSubmitScore(score)); // identical score, different content, accept
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

        String content = "SOLUTION:A Most Unfortunate Malfunction,12345ieee,45-1-14\nbunch of stuff...";
        assertInstanceOf(SubmitResult.Success.class, doSubmitData(content)); // 45/1/14
        assertInstanceOf(SubmitResult.AlreadyPresent.class, doSubmitData(content)); // identical

        content = "SOLUTION:A Most Unfortunate Malfunction,12345ieee,45-1-14\ndifferent stuff...";
        assertInstanceOf(SubmitResult.Success.class, doSubmitData(content)); // changed content, I can

        content = "SOLUTION:A Most Unfortunate Malfunction,BadGuy,45-1-14\ndifferent stuff...";
        assertInstanceOf(SubmitResult.AlreadyPresent.class, doSubmitData(content)); // stealing is bad

        content = "SOLUTION:A Most Unfortunate Malfunction,BadGuy,50-1-50\nsome more stuff...";
        assertInstanceOf(SubmitResult.NothingBeaten.class, doSubmitData(content)); // just give up, man
    }

    @NotNull
    private SubmitResult<ScRecord, ScCategory> doSubmitScore(@NotNull ScScore score) {
        String content = "SOLUTION:A Most Unfortunate Malfunction," + score.toDisplayString(DisplayContext.fileName());
        return repository.submit(new ScSubmission(ScPuzzle.bonding_boss, score, "12345ieee", null, content));
    }

    @NotNull
    private SubmitResult<ScRecord, ScCategory> doSubmitData(String data) {
        return repository.submit(ScSubmission.fromDataNoValidation(data, null));
    }
}
