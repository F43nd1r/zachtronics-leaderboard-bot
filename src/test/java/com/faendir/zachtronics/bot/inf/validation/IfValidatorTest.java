/*
 * Copyright (c) 2024
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

package com.faendir.zachtronics.bot.inf.validation;

import com.faendir.zachtronics.bot.inf.model.IfPuzzle;
import com.faendir.zachtronics.bot.inf.model.IfScore;
import com.faendir.zachtronics.bot.inf.model.IfSubmission;
import com.faendir.zachtronics.bot.validation.ValidationResult;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IfValidatorTest {

    @Test
    public void testOne() {
        String content = """
                         InputRate.1-1.1 = 1
                         Solution.1-1.1 = AwAAAAAAAAA=
                         """;
        String author = "12345ieee";
        IfScore score = new IfScore(1, 1, 0, false, false, false);
        IfSubmission result = IfValidator.validateSavefile(content, author, score).iterator().next().getSubmission();
        IfSubmission expected = new IfSubmission(IfPuzzle.LEVEL_1_1, score, author, Collections.emptyList(),
                                                 content.replace("1-1.1", "1-1.0"));
        assertEquals(expected, result);
    }

    @Test
    public void testMany() {
        String content = """
                         Best.1-1.Blocks = 0
                         Best.1-1.Cycles = 44
                         Best.1-1.Footprint = 21
                         InputRate.1-1.0 = 1
                         InputRate.1-1.1 = 1
                         Last.1-1.0.Blocks = 0
                         Last.1-1.0.Cycles = 44
                         Last.1-1.0.Footprint = 47
                         Solution.1-1.0 = AwAAAAAAAAA=
                         Last.1-1.1.Blocks = 0
                         Last.1-1.1.Cycles = 58
                         Last.1-1.1.Footprint = 21
                         Solution.1-1.1 = AwAAAAAAAAA=
                         """;
        String author = "12345ieee";
        Collection<ValidationResult<IfSubmission>> results = IfValidator.validateSavefile(content, author, null);

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(v -> v instanceof ValidationResult.Valid<IfSubmission>));
        assertEquals(new IfScore(44, 47, 0, false, false, true), results.iterator().next().getSubmission().getScore());
    }
}