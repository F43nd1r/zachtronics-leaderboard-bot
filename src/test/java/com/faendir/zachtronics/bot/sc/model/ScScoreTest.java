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

package com.faendir.zachtronics.bot.sc.model;

import com.faendir.zachtronics.bot.model.DisplayContext;
import com.faendir.zachtronics.bot.model.StringFormat;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.EnumSet;

import static com.faendir.zachtronics.bot.sc.model.ScCategory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ScScoreTest {
    @Test
    public void testParse() {
        assertEquals(new ScScore(123, 4, 56, false, false), ScScore.parseScore("123/4/56"));
        assertEquals(new ScScore(123, 4, 56, true, false), ScScore.parseScore("123-4-56-B"));
        assertEquals(new ScScore(123, 4, 56, false, true), ScScore.parseScore("123/4-56/P"));
        assertEquals(new ScScore(123, 4, 56, true, true), ScScore.parseScore("123-4/56-BP"));
    }

    @Test
    public void testDisplayReddit() {
        ScScore score = new ScScore(123, 4, 56, false, false);
        assertBoldMetrics(score, 1, EnumSet.of(C));
        assertBoldMetrics(score, 2, EnumSet.of(RC));
        assertBoldMetrics(score, 1, EnumSet.range(C, CNBP));
        assertBoldMetrics(score, 1, EnumSet.of(C, RC));
        assertBoldMetrics(score, 1, EnumSet.of(RC, RS));
        assertBoldMetrics(score, 0, EnumSet.of(S, RC));
        assertBoldMetrics(score, 0, EnumSet.allOf(ScCategory.class));
        assertBoldMetrics(score, 0, EnumSet.noneOf(ScCategory.class));
        assertBoldMetrics(score, 0, null);
    }

    private static void assertBoldMetrics(@NotNull ScScore score, int number, @Nullable Collection<ScCategory> categories) {
        String result = score.toDisplayString(new DisplayContext<>(StringFormat.REDDIT, categories));
        assertEquals(number, StringUtils.countMatches(result, "**") / 2);
    }
}