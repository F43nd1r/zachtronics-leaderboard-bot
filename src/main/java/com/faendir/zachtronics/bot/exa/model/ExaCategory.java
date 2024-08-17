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

package com.faendir.zachtronics.bot.exa.model;

import com.faendir.zachtronics.bot.model.CategoryJava;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static com.faendir.zachtronics.bot.exa.model.ExaMetric.*;
import static com.faendir.zachtronics.bot.exa.model.ExaType.STANDARD;
import static com.faendir.zachtronics.bot.exa.model.ExaType.UNCHEESABLE;


@Getter
public enum ExaCategory implements CategoryJava<ExaCategory, ExaScore, ExaMetric<?>> {
    CS(LEGIT, List.of(CYCLES, SIZE, ACTIVITY), EnumSet.of(STANDARD, UNCHEESABLE), 0b100),
    CA(LEGIT, List.of(CYCLES, ACTIVITY, SIZE), EnumSet.of(STANDARD, UNCHEESABLE), 0b100),

    SC(LEGIT, List.of(SIZE, CYCLES, ACTIVITY), EnumSet.of(STANDARD, UNCHEESABLE), 0b010),
    SA(LEGIT, List.of(SIZE, ACTIVITY, CYCLES), EnumSet.of(STANDARD, UNCHEESABLE), 0b010),

    AC(LEGIT, List.of(ACTIVITY, CYCLES, SIZE), EnumSet.of(STANDARD, UNCHEESABLE), 0b001),
    AS(LEGIT, List.of(ACTIVITY, SIZE, CYCLES), EnumSet.of(STANDARD, UNCHEESABLE), 0b001),

    cCS(CAN_CHEESE, List.of(CAN_CHEESE, CYCLES, SIZE, ACTIVITY), EnumSet.of(STANDARD), 0b100),
    cCA(CAN_CHEESE, List.of(CAN_CHEESE, CYCLES, ACTIVITY, SIZE), EnumSet.of(STANDARD), 0b100),

    cSC(CAN_CHEESE, List.of(CAN_CHEESE, SIZE, CYCLES, ACTIVITY), EnumSet.of(STANDARD), 0b010),
    cSA(CAN_CHEESE, List.of(CAN_CHEESE, SIZE, ACTIVITY, CYCLES), EnumSet.of(STANDARD), 0b010),

    cAC(CAN_CHEESE, List.of(CAN_CHEESE, ACTIVITY, CYCLES, SIZE), EnumSet.of(STANDARD), 0b001),
    cAS(CAN_CHEESE, List.of(CAN_CHEESE, ACTIVITY, SIZE, CYCLES), EnumSet.of(STANDARD), 0b001);


    /** contains <tt>%d%s%d%s%d%s</tt> plus a bunch of <tt>*</tt> most likely */
    static final String[] FORMAT_STRINGS = {"%d%s%d%s%d%s", "%d%s%d%s**%d**%s", "%d%s**%d**%s%d%s", null, "**%d**%s%d%s%d%s"};

    private final String displayName;
    private final ExaMetric<Boolean> admission;
    private final List<ExaMetric<?>> metrics;
    private final Comparator<ExaScore> scoreComparator;
    private final Set<ExaType> supportedTypes;
    private final int scoreFormatId;

    ExaCategory(@NotNull ExaMetric<Boolean> admission, @NotNull List<ExaMetric<?>> metrics, Set<ExaType> supportedTypes, int scoreFormatId) {
        this.supportedTypes = supportedTypes;
        this.displayName = name();
        this.admission = admission;
        this.metrics = metrics;
        this.scoreComparator = makeCategoryComparator(metrics);
        this.scoreFormatId = scoreFormatId;
    }

    @Override
    public boolean supportsScore(@NotNull ExaScore score) {
        return admission.get(score);
    }
}
