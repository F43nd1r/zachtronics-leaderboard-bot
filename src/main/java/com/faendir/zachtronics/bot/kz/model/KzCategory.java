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

package com.faendir.zachtronics.bot.kz.model;

import com.faendir.zachtronics.bot.model.CategoryJava;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static com.faendir.zachtronics.bot.kz.model.KzMetric.*;
import static com.faendir.zachtronics.bot.kz.model.KzType.STANDARD;

@Getter
public enum KzCategory implements CategoryJava<KzCategory, KzScore, KzMetric> {
    TC(List.of(TIME, COST, AREA), 0b100),
    TA(List.of(TIME, AREA, COST), 0b100),

    CT(List.of(COST, TIME, AREA), 0b010),
    CA(List.of(COST, AREA, TIME), 0b010),

    AT(List.of(AREA, TIME, COST), 0b001),
    AC(List.of(AREA, COST, TIME), 0b001);

    /** contains <tt>%d%s%d%s%d</tt> plus a bunch of <tt>*</tt> most likely */
    static final String[] FORMAT_STRINGS = {"%d%s%d%s%d", "%d%s%d%s**%d**", "%d%s**%d**%s%d", null, "**%d**%s%d%s%d"};


    private final String displayName;
    private final List<KzMetric> metrics;
    private final Comparator<KzScore> scoreComparator;
    private final Set<KzType> supportedTypes = Collections.singleton(STANDARD);
    private final int scoreFormatId;

    KzCategory(@NotNull List<KzMetric> metrics, int scoreFormatId) {
        this.displayName = name();
        this.metrics = metrics;
        this.scoreComparator = makeCategoryComparator(metrics);
        this.scoreFormatId = scoreFormatId;
    }

    @Override
    public boolean supportsScore(@NotNull KzScore score) {
        return true;
    }
}
