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

package com.faendir.zachtronics.bot.tis.model;

import com.faendir.zachtronics.bot.utils.MetricsTreeKt;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.faendir.zachtronics.bot.tis.model.TISCategory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TISCategoryTest {

    @Test
    public void ensureCategoryPacking() {
        List<TISCategory> nCats = Arrays.asList(CN, CI, CX);
        List<TISCategory> ncCats = Arrays.asList(CN, cCN, CI, cCI, CX, cCX);
        List<TISCategory> nacCats = Arrays.asList(CN, aCN, cCN, CI, aCI, cCI, CX, aCX, cCX);

        assertEquals("C", MetricsTreeKt.smartFormat(nCats, Arrays.asList(TISCategory.values())));
        assertEquals("C, cC", MetricsTreeKt.smartFormat(ncCats, Arrays.asList(TISCategory.values())));
        assertEquals("C, aC, cC", MetricsTreeKt.smartFormat(nacCats, Arrays.asList(TISCategory.values())));
        assertEquals("C", MetricsTreeKt.smartFormat(nCats, TISPuzzle.UNKNOWN.getSupportedCategories()));
        assertEquals("C, cC", MetricsTreeKt.smartFormat(ncCats, TISPuzzle.UNKNOWN.getSupportedCategories()));
    }
}