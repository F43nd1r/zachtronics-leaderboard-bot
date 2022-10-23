/*
 * Copyright (c) 2022
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

package com.faendir.zachtronics.bot.sz.validation;

import com.faendir.zachtronics.bot.sz.model.SzPuzzle;
import com.faendir.zachtronics.bot.sz.model.SzScore;
import com.faendir.zachtronics.bot.sz.model.SzSubmission;
import com.faendir.zachtronics.bot.validation.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SzValidatorTest {

    @Test
    void testBadData() {
        // typical HTML (non raw) page
        String data = """
                      <!DOCTYPE html>
                      <html lang="en" data-color-mode="auto" data-light-theme="light" data-dark-theme="dark">
                      """;
        assertThrows(ValidationException.class, () -> SzValidator.validate(data, "837951602", null));
    }

    @Test
    public void testCopyBad() {
        String baseContent = """
                [name] TITLE
                [puzzle] Sz000
                [production-cost] 0
                [power-usage] 100
                [lines-of-code] 0
                
                [traces]\s
                ......................
                ......................
                ......................
                ......................
                ......................
                ......................
                ......................
                ......................
                ......................
                ......................
                ......................
                ......................
                ......................
                ......................
                """;
        for (String title : new String[]{"title (Copy)", "title (Copy) (Copy)"}) {
            String content = baseContent.replace("TITLE", title);
            SzSubmission result = SzValidator.validate(content, "12345ieee", "image.link");
            SzSubmission expected = new SzSubmission(SzPuzzle.Sz000, new SzScore(0, 100, 0),
                                                     "12345ieee", "image.link",
                                                     content.replace(" (Copy)", ""));
            assertEquals(expected, result);
        }
    }
}