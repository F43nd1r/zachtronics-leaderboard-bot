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

package com.faendir.zachtronics.bot.sc.validation;

import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.ScScore;
import com.faendir.zachtronics.bot.sc.model.ScSubmission;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScMetadataReaderTest {

    @Test
    public void testCopyBad() {
        for (String name : new String[]{"name (copy)", "'comma, comma (copy)'"}) {
            String content = "SOLUTION:Of Pancakes and Spaceships,12345ieee,50-50-50," + name;
            ScSubmission result = ScMetadataReader.fromHeader(content, null, "vi.deo");
            ScSubmission expected = new ScSubmission(ScPuzzle.research_example_1, new ScScore(50, 50, 50, false, false),
                                                     "12345ieee", "vi.deo", content.replace(" (copy)", ""));
            assertEquals(expected, result);
        }
    }
}