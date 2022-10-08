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

package com.faendir.zachtronics.bot.sz.model;

import com.faendir.zachtronics.bot.model.CategoryJava;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static com.faendir.zachtronics.bot.sz.model.SzMetric.*;
import static com.faendir.zachtronics.bot.sz.model.SzType.STANDARD;

@Getter
public enum SzCategory implements CategoryJava<SzCategory, SzScore, SzMetric, SzType> {
    CP("CP", List.of(COST, POWER, LINES), 0b100),
    CL("CL", List.of(COST, LINES, POWER), 0b100),

    PC("PC", List.of(POWER, COST, LINES), 0b010),
    PL("PL", List.of(POWER, LINES, COST), 0b010),

    LC("LC", List.of(LINES, COST, POWER), 0b001),
    LP("LP", List.of(LINES, POWER, COST), 0b001);

    /** contains <tt>%d%s%d%s%d</tt> plus a bunch of <tt>*</tt> most likely */
    static final String[] FORMAT_STRINGS = {"%d%s%d%s%d", "%d%s%d%s**%d**", "%d%s**%d**%s%d", null, "**%d**%s%d%s%d"};

    private final String displayName;
    private final List<SzMetric> metrics;
    private final Comparator<SzScore> scoreComparator;
    private final Set<SzType> supportedTypes = Collections.singleton(STANDARD);
    private final int scoreFormatId;

    SzCategory(String displayName, @NotNull List<SzMetric> metrics, int scoreFormatId) {
        this.displayName = displayName;
        this.metrics = metrics;
        this.scoreComparator = makeCategoryComparator(metrics);
        this.scoreFormatId = scoreFormatId;
    }

    @Override
    public boolean supportsScore(@NotNull SzScore score) {
        return true;
    }
}
