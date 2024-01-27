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

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScCategoryTest {

    @Test
    void testSupportsScore() {
        assertTrue(supportsFlags(ScCategory.C, true, true));
        assertTrue(supportsFlags(ScCategory.C, false, false));

        assertFalse(supportsFlags(ScCategory.SNB, true, false));
        assertTrue(supportsFlags(ScCategory.SNB, false, true));

        assertFalse(supportsFlags(ScCategory.RCNBP, true, false));
        assertFalse(supportsFlags(ScCategory.RCNBP, false, true));
        assertTrue(supportsFlags(ScCategory.RCNBP, false, false));
    }

    private static boolean supportsFlags(@NotNull ScCategory category, boolean bugged, boolean precognitive) {
        return category.supportsScore(new ScScore(1, 1, 1, bugged, precognitive));
    }
}