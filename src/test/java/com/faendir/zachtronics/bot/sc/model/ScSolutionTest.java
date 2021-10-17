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
class ScSolutionTest {
    @Test
    public void testNoFlags() {
        ScSolution s1 = ScSolution.fromContentNoValidation(
                "SOLUTION:Of Pancakes and Spaceships,12345ieee,50-50-50\nstuff...", null);
        ScSolution s2 = ScSolution.fromContentNoValidation(
                "SOLUTION:Of Pancakes and Spaceships,12345ieee,50-50-50\nstuff...", ScPuzzle.research_example_2);
        ScSolution s3 = ScSolution.fromContentNoValidation(
                "SOLUTION:Of Pancakes and Spaceships,12345ieee,50-50-50\nstuff...", ScPuzzle.research_example_1);
        assertNotEquals(s1, s2);
        assertEquals(s1, s3);
    }

    @Test
    public void testCommas() {
        String content = "SOLUTION:'1,3-Dimetoxibencene',andy,922-1-27,s\nstuff...";
        ScSolution result = ScSolution.fromContentNoValidation(content, null);
        ScSolution expected = new ScSolution(ScPuzzle.published_43_2, new ScScore(922, 1, 27, false, false), content);
        assertEquals(expected, result);

        content = "SOLUTION:Of Pancakes and Spaceships,12345ieee,50-50-50,'/B with, commmas, :('\nstuff...";
        result = ScSolution.fromContentNoValidation(content, null);
        expected = new ScSolution(ScPuzzle.research_example_1, new ScScore(50, 50, 50, true, false), content);
        assertEquals(expected, result);
    }

    @Test
    public void testCopyBad() {
        for (String name: new String[]{"name (copy)", "'comma, comma (copy)'"}) {
            String content = "SOLUTION:Of Pancakes and Spaceships,12345ieee,50-50-50," + name + "\nstuff...";
            ScSolution result = ScSolution.fromContentNoValidation(content, null);
            ScSolution expected = new ScSolution(ScPuzzle.research_example_1, new ScScore(50, 50, 50, false, false),
                                                 content.replace(" (copy)", ""));
            assertEquals(expected, result);
        }
    }

    @Test
    public void testFlags() {
        List<ScScore> scores = Stream.of("", "/B", "/P", "/BP")
                                     .map("SOLUTION:Of Pancakes and Spaceships,12345ieee,50-50-50,"::concat)
                                     .map(e -> ScSolution.fromContentNoValidation(e, null).getScore())
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

    private static void assertBreaks(String content) {
        assertThrows(IllegalArgumentException.class, () -> ScSolution.fromContentNoValidation(content, null));
    }
}