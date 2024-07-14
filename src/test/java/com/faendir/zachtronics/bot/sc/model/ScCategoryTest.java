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

package com.faendir.zachtronics.bot.sc.model;

import com.faendir.zachtronics.bot.utils.MetricsTreeKt;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.faendir.zachtronics.bot.sc.model.ScCategory.*;
import static org.junit.jupiter.api.Assertions.*;

class ScCategoryTest {

    @Test
    public void ensureCategoryPacking() {
        List<ScCategory> allCats = Arrays.asList(ScCategory.values());
        assertEquals("C", MetricsTreeKt.smartFormat(Arrays.asList(C, CNB, CNP, CNBP), allCats));
        assertEquals("C, CNB, CNP", MetricsTreeKt.smartFormat(Arrays.asList(C, CNB, CNP), allCats));
    }

    @Test
    public void testSupportsScore() {
        assertTrue(supportsFlags(C, true, true));
        assertTrue(supportsFlags(C, false, false));

        assertFalse(supportsFlags(SNB, true, false));
        assertTrue(supportsFlags(SNB, false, true));

        assertFalse(supportsFlags(RCNBP, true, false));
        assertFalse(supportsFlags(RCNBP, false, true));
        assertTrue(supportsFlags(RCNBP, false, false));
    }

    private static boolean supportsFlags(@NotNull ScCategory category, boolean bugged, boolean precognitive) {
        return category.supportsScore(new ScScore(1, 1, 1, bugged, precognitive));
    }
}