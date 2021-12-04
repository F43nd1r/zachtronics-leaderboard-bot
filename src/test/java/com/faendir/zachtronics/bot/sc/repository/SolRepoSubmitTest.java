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

import com.faendir.zachtronics.bot.Application;
import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.model.DisplayContext;
import com.faendir.zachtronics.bot.repository.SubmitResult;
import com.faendir.zachtronics.bot.sc.model.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertTrue;

@BotTest
public class SolRepoSubmitTest {

    @Autowired
    private ScSolutionRepository repository;

    @Test
    public void testArchiveScore() {
        // we start with a 100/100/100
        ScScore score = new ScScore(100, 100, 100, true, true);
        assertTrue(doSubmitScore(score) instanceof SubmitResult.NothingBeaten); // our score is a BP, fail
        score = new ScScore(100, 100, 100, false, true);
        assertTrue(doSubmitScore(score) instanceof SubmitResult.NothingBeaten); // our score is a P, fail
        score = new ScScore(100, 100, 100, false, false);
        assertTrue(doSubmitScore(score) instanceof SubmitResult.Success); // identical score, different content, accept
        score = new ScScore(10, 100, 1000, true, true);
        assertTrue(doSubmitScore(score) instanceof SubmitResult.Success); // new frontier piece
        score = new ScScore(1000, 100, 10, true, true);
        assertTrue(doSubmitScore(score) instanceof SubmitResult.Success); // new frontier piece
        score = new ScScore(10, 10, 10, true, true);
        assertTrue(doSubmitScore(score) instanceof SubmitResult.Success); // beats them both
        score = new ScScore(20, 20, 20, true, true);
        assertTrue(doSubmitScore(score) instanceof SubmitResult.NothingBeaten); // falls flat
        score = new ScScore(20, 20, 20, false, true);
        assertTrue(doSubmitScore(score) instanceof SubmitResult.Success); // new non bugged record
    }

    @Test
    public void testSubmitData() {
        // we start at 100/100/100
        ScScore score = new ScScore(50, 50, 50, false, false);
        assertTrue(doSubmitScore(score) instanceof SubmitResult.Success); // 50/50/50

        String content = "SOLUTION:A Most Unfortunate Malfunction,12345ieee,45-1-14\nbunch of stuff...";
        assertTrue(doSubmitData(content) instanceof SubmitResult.Success); // 45/1/14
        assertTrue(doSubmitData(content) instanceof SubmitResult.AlreadyPresent); // identical

        content = "SOLUTION:A Most Unfortunate Malfunction,12345ieee,45-1-14\ndifferent stuff...";
        assertTrue(doSubmitData(content) instanceof SubmitResult.Success); // changed content, I can

        content = "SOLUTION:A Most Unfortunate Malfunction,BadGuy,45-1-14\ndifferent stuff...";
        assertTrue(doSubmitData(content) instanceof SubmitResult.AlreadyPresent); // stealing is bad

        content = "SOLUTION:A Most Unfortunate Malfunction,BadGuy,50-1-50\nsome more stuff...";
        assertTrue(doSubmitData(content) instanceof SubmitResult.NothingBeaten); // just give up, man
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
