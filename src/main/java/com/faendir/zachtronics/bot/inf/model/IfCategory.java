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
    CF(INBOUNDS, List.of(CYCLES, INFINITE, FOOTPRINT, BLOCKS), 0b100),
    CB(INBOUNDS, List.of(CYCLES, INFINITE, BLOCKS, FOOTPRINT), 0b100),
    CFNG(NO_OOB_GRA, List.of(CYCLES, NO_GRA, INFINITE, FOOTPRINT, BLOCKS), 0b100),
    CBNG(NO_OOB_GRA, List.of(CYCLES, NO_GRA, INFINITE, BLOCKS, FOOTPRINT), 0b100),

    FC(ANY_FLAG, List.of(FOOTPRINT, CYCLES, BLOCKS), 0b010),
    FB(ANY_FLAG, List.of(FOOTPRINT, BLOCKS, CYCLES), 0b010),
    FIC(INBOUNDS, List.of(FOOTPRINT, INBOUNDS, CYCLES, BLOCKS), 0b010),
    FIB(INBOUNDS, List.of(FOOTPRINT, INBOUNDS, BLOCKS, CYCLES), 0b010),

    BC(ANY_FLAG, List.of(BLOCKS, CYCLES, FOOTPRINT), 0b001),
    BF(ANY_FLAG, List.of(BLOCKS, FOOTPRINT, CYCLES), 0b001),
    BNC(NO_FLAGS, List.of(BLOCKS, NO_FLAGS, CYCLES, FOOTPRINT), 0b001),
    BNF(NO_FLAGS, List.of(BLOCKS, NO_FLAGS, FOOTPRINT, CYCLES), 0b001);

    /** contains <tt>%d%s%d%s%d%s</tt> plus a bunch of <tt>*</tt> most likely */
    static final String[] FORMAT_STRINGS = {"%d%s%d%s%d%s", "%d%s%d%s**%d**%s", "%d%s**%d**%s%d%s", null, "**%d**%s%d%s%d%s"};

    private final String displayName = name();
    private final IfMetric<Boolean> admission;
    private final List<IfMetric<?>> metrics;
    private final Comparator<IfScore> scoreComparator;
    private final Set<IfType> supportedTypes;
    private final int scoreFormatId;

    IfCategory(@NotNull IfMetric<Boolean> admission, @NotNull List<IfMetric<?>> metrics, int scoreFormatId) {
        this.admission = admission;
        this.metrics = metrics;
        this.scoreComparator = makeCategoryComparator(metrics);
        this.supportedTypes = (admission == INFINITE || admission == NO_FLAGS) ? Collections.singleton(STANDARD)
                                                                               : EnumSet.allOf(IfType.class);
        this.scoreFormatId = scoreFormatId;
    }

    @Override
    public boolean supportsScore(@NotNull IfScore score) {
        return !admission.get(score);
    }
}
