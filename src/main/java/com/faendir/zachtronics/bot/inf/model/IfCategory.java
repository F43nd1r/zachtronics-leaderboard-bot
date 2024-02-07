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

package com.faendir.zachtronics.bot.inf.model;

import com.faendir.zachtronics.bot.model.CategoryJava;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.faendir.zachtronics.bot.inf.model.IfMetric.*;
import static com.faendir.zachtronics.bot.inf.model.IfType.STANDARD;

@Getter
public enum IfCategory implements CategoryJava<IfCategory, IfScore, IfMetric<?>> {
    CF(List.of(CYCLES, INFINITE, FOOTPRINT, BLOCKS), INBOUNDS, 0b100),
    CB(List.of(CYCLES, INFINITE, BLOCKS, FOOTPRINT), INBOUNDS, 0b100),
    CFNG(List.of(CYCLES, NO_GRA, INFINITE, FOOTPRINT, BLOCKS), NO_OOB_GRA, 0b100),
    CBNG(List.of(CYCLES, NO_GRA, INFINITE, BLOCKS, FOOTPRINT), NO_OOB_GRA, 0b100),

    FC(List.of(FOOTPRINT, CYCLES, BLOCKS), ANY_FLAG, 0b010),
    FB(List.of(FOOTPRINT, BLOCKS, CYCLES), ANY_FLAG, 0b010),
    FIC(List.of(FOOTPRINT, INBOUNDS, CYCLES, BLOCKS), INBOUNDS, 0b010),
    FIB(List.of(FOOTPRINT, INBOUNDS, BLOCKS, CYCLES), INBOUNDS, 0b010),

    BC(List.of(BLOCKS, CYCLES, FOOTPRINT), ANY_FLAG, 0b001),
    BF(List.of(BLOCKS, FOOTPRINT, CYCLES), ANY_FLAG, 0b001),
    BNC(List.of(BLOCKS, NO_FLAGS, CYCLES, FOOTPRINT), NO_FLAGS, 0b001),
    BNF(List.of(BLOCKS, NO_FLAGS, FOOTPRINT, CYCLES), NO_FLAGS, 0b001);

    /** contains <tt>%d%s%d%s%d%s</tt> plus a bunch of <tt>*</tt> most likely */
    static final String[] FORMAT_STRINGS = {"%d%s%d%s%d%s", "%d%s%d%s**%d**%s", "%d%s**%d**%s%d%s", null, "**%d**%s%d%s%d%s"};

    private final String displayName = name();
    private final List<IfMetric<?>> metrics;
    private final Comparator<IfScore> scoreComparator;
    private final IfMetric<Boolean> admission;
    private final Set<IfType> supportedTypes;
    private final int scoreFormatId;

    IfCategory(@NotNull List<IfMetric<?>> metrics, @NotNull IfMetric<Boolean> admission, int scoreFormatId) {
        this.metrics = metrics;
        this.scoreComparator = makeCategoryComparator(metrics);
        this.admission = admission;
        this.supportedTypes = (admission == INFINITE || admission == NO_FLAGS) ? Collections.singleton(STANDARD)
                                                                               : EnumSet.allOf(IfType.class);
        this.scoreFormatId = scoreFormatId;
    }

    @Override
    public boolean supportsScore(@NotNull IfScore score) {
        return !admission.get(score);
    }
}
