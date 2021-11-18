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

package com.faendir.zachtronics.bot.sc.model;

import com.faendir.zachtronics.bot.Application;
import com.faendir.zachtronics.bot.BotTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@BotTest(Application.class)
class ScSolutionMetadataTest {
    @Test
    public void testNoFlags() {
        ScSolutionMetadata s1 = ScSolutionMetadata.fromHeader(
                "SOLUTION:Of Pancakes and Spaceships,12345ieee,50-50-50", null);
        ScSolutionMetadata s2 = ScSolutionMetadata.fromHeader(
                "SOLUTION:Of Pancakes and Spaceships,12345ieee,50-50-50", ScPuzzle.research_example_2);
        ScSolutionMetadata s3 = ScSolutionMetadata.fromHeader(
                "SOLUTION:Of Pancakes and Spaceships,12345ieee,50-50-50", ScPuzzle.research_example_1);
        assertNotEquals(s1, s2);
        assertEquals(s1, s3);
    }

    @Test
    public void testCommas() {
        String content = "SOLUTION:'1,3-Dimetoxibencene',andy,922-1-27,s";
        ScSolutionMetadata result = ScSolutionMetadata.fromHeader(content, null);
        ScSolutionMetadata expected = new ScSolutionMetadata(ScPuzzle.published_43_2, "andy",
                                                             new ScScore(922, 1, 27, false, false), "s");
        assertEquals(expected, result);

        content = "SOLUTION:Of Pancakes and Spaceships,12345ieee,50-50-50,'/B with, commmas, :('";
        result = ScSolutionMetadata.fromHeader(content, null);
        expected = new ScSolutionMetadata(ScPuzzle.research_example_1, "12345ieee",
                                          new ScScore(50, 50, 50, true, false), "/B with, commmas, :(");
        assertEquals(expected, result);
    }

    @Test
    public void testFlags() {
        List<ScScore> scores = Stream.of("", "/B", "/P", "/BP")
                                     .map("SOLUTION:Of Pancakes and Spaceships,12345ieee,50-50-50,"::concat)
                                     .map(e -> ScSolutionMetadata.fromHeader(e, null).getScore())
                                     .toList();
        boolean[] bugged = {false, true, false, true};
        boolean[] precog = {false, false, true, true};
        for (int i = 0; i < scores.size(); i++) {
            assertEquals(bugged[i], scores.get(i).isBugged());
            assertEquals(precog[i], scores.get(i).isPrecognitive());
        }
    }

    @Test
    public void testInvalidContent() {
        assertBreaks("No SOL block in sight\nstuff...");
        assertBreaks("SOLUTION:No puzzle that exists,12345ieee,50-50-50\nstuff...");
        assertBreaks("stuff...SOLUTION:Of Pancakes and Spaceships,12345ieee,50-50-50\nstuff...");
        assertBreaks("SOLUTION:Of Pancakes and Spaceships,12345ieee,0-0-0,way too fast\nstuff...");
    }

    @Test
    public void testExtendToSubmission() {
        String[] headers = {"SOLUTION:Yellowcake,whoever314,10288-1-97,/P\nstuff...",
                            "SOLUTION:Vitamin B3,Andy,2174-5-222\nstuff..."};
        ScPuzzle[] puzzles = {null, ScPuzzle.published_42_3};
        for (int i = 0; i < headers.length; i++) {
            String header = headers[i];
            ScSolutionMetadata solutionMetadata = ScSolutionMetadata.fromHeader(header, puzzles[i]);
            ScSubmission submission = solutionMetadata.extendToSubmission(null, header);
            assertEquals(header, submission.getData());
        }
    }

    private static void assertBreaks(String content) {
        assertThrows(IllegalArgumentException.class, () -> ScSolutionMetadata.fromHeader(content, null));
    }
}