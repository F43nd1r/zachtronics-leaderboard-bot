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
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IfValidatorTest {

    @Test
    public void testOne() {
        String content = """
                         InputRate.1-1.1 = 1
                         Solution.1-1.1 = AwAAAAAAAAA=
                         """;
        String author = "User";
        IfScore score = new IfScore(1, 1, 0, false, false, false);
        IfSubmission result = IfValidator.validateSavefile(content, author, score, null, false).iterator().next().getSubmission();
        IfSubmission expected = new IfSubmission(IfPuzzle.LEVEL_1_1, score, author, Collections.emptyList(),
                                                 content.replace("1-1.1", "1-1.0"));
        assertEquals(expected, result);
    }

    @Test
    public void testManyAsAdmin() {
        String content = """
                         Best.1-1.Blocks = 0
                         Best.1-1.Cycles = 44
                         Best.1-1.Footprint = 21
                         InputRate.1-1.0 = 1
                         InputRate.1-1.1 = 1
                         Last.1-1.0.Blocks = 0
                         Last.1-1.0.Cycles = 44
                         Last.1-1.0.Footprint = 47
                         Last.1-1.1.Blocks = 0
                         Last.1-1.1.Cycles = 58
                         Last.1-1.1.Footprint = 21
                         Solution.1-1.0 = AwAAAAAAAAA=
                         Solution.1-1.1 = AwAAAAAAAAA=
                         """;
        String author = "12345ieee";
        Collection<ValidationResult<IfSubmission>> results = IfValidator.validateSavefile(content, author, null, null, true);

        assertEquals(2, results.size());
        results.forEach(v -> assertInstanceOf(ValidationResult.Valid.class, v));
        assertEquals(new IfScore(44, 47, 0, false, false, true), results.iterator().next().getSubmission().getScore());
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
                         Last.1-1.0.Flags =
                         Last.1-1.1.Blocks = 0
                         Last.1-1.1.Cycles = 58
                         Last.1-1.1.Footprint = 21
                         Last.1-1.1.Flags = /F
                         Solution.1-1.0 = AwAAAAAAAAA=
                         Solution.1-1.1 = AwAAAAAAAAA=
                         """;
        String author = "User";
        Collection<ValidationResult<IfSubmission>> results = IfValidator.validateSavefile(content, author, null, null, false);

        assertEquals(2, results.size());
        results.forEach(v -> assertInstanceOf(ValidationResult.Valid.class, v));
        Iterator<ValidationResult<IfSubmission>> it = results.iterator();
        assertFalse(it.next().getSubmission().getScore().isFinite());
        assertTrue(it.next().getSubmission().getScore().isFinite());
    }

    @Test
    public void testExtensions() {
        String content = """
                         InputRate.1-1.2 = 1
                         Last.1-1.2.Blocks = 0
                         Last.1-1.2.Cycles = 55
                         Last.1-1.2.Footprint = 33
                         Last.1-1.2.Flags = /O
                         Solution.1-1.2 = AwAAAAAAAAA=
                         Author.1-1.2 = fileAuthor
                         Videos.1-1.2 = https://example.com
                         """;
        String author = "NotAuthor";
        List<String> videos = List.of("notLink");
        Collection<ValidationResult<IfSubmission>> results = IfValidator.validateSavefile(content, author, null, videos, false);

        assertEquals(1, results.size());
        IfSubmission submission = results.iterator().next().getSubmission();
        assertTrue(submission.getScore().isOutOfBounds());
        assertEquals("fileAuthor", submission.getAuthor());
        assertEquals("https://example.com", submission.getDisplayLink());
    }

    @Test
    public void testBad() {
        String content = """
                         InputRate.1-1.2 = 1
                         Last.1-1.2.Blocks = 0
                         Last.1-1.2.Cycles = 55
                         Last.1-1.2.Footprint = 33
                         Solution.1-1.2 = AwAAAAAAAAA=
                         """;
        String author = "User";
        Collection<ValidationResult<IfSubmission>> results = IfValidator.validateSavefile(content, author, null, null, false);

        assertEquals(1, results.size());
        assertInstanceOf(ValidationResult.Unparseable.class, results.iterator().next());
    }
}