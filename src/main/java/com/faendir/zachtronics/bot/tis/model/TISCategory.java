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

import com.faendir.zachtronics.bot.model.CategoryJava;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static com.faendir.zachtronics.bot.tis.model.TISMetric.*;
import static com.faendir.zachtronics.bot.tis.model.TISType.STANDARD;
import static com.faendir.zachtronics.bot.tis.model.TISType.WITH_ACHIEVEMENT;

@Getter
public enum TISCategory implements CategoryJava<TISCategory, TISScore, TISMetric<?>> {
    CN("CN", NOT_CHEATING, List.of(CYCLES, NODES, INSTRUCTIONS), 0b100),
    CI("CI", NOT_CHEATING, List.of(CYCLES, INSTRUCTIONS, NODES), 0b100),
    CX("CX", NOT_CHEATING, List.of(CYCLES, PROD_NI), 0b100),

    NC("NC", NOT_CHEATING, List.of(NODES, CYCLES, INSTRUCTIONS), 0b010),
    NI("NI", NOT_CHEATING, List.of(NODES, INSTRUCTIONS, CYCLES), 0b010),
    NX("NX", NOT_CHEATING, List.of(NODES, PROD_CI), 0b010),

    IC("IC", NOT_CHEATING, List.of(INSTRUCTIONS, CYCLES, NODES), 0b001),
    IN("IN", NOT_CHEATING, List.of(INSTRUCTIONS, NODES, CYCLES), 0b001),
    IX("IX", NOT_CHEATING, List.of(INSTRUCTIONS, PROD_CN), 0b001),

    aCN("aCN", ACHIEVEMENT, List.of(ACHIEVEMENT, CYCLES, NODES, INSTRUCTIONS), 0b100),
    aCI("aCI", ACHIEVEMENT, List.of(ACHIEVEMENT, CYCLES, INSTRUCTIONS, NODES), 0b100),
    aCX("aCX", ACHIEVEMENT, List.of(ACHIEVEMENT, CYCLES, PROD_NI), 0b100),

    aNC("aNC", ACHIEVEMENT, List.of(ACHIEVEMENT, NODES, CYCLES, INSTRUCTIONS), 0b010),
    aNI("aNI", ACHIEVEMENT, List.of(ACHIEVEMENT, NODES, INSTRUCTIONS, CYCLES), 0b010),
    aNX("aNX", ACHIEVEMENT, List.of(ACHIEVEMENT, NODES, PROD_CI), 0b010),

    aIC("aIC", ACHIEVEMENT, List.of(ACHIEVEMENT, INSTRUCTIONS, CYCLES, NODES), 0b001),
    aIN("aIN", ACHIEVEMENT, List.of(ACHIEVEMENT, INSTRUCTIONS, NODES, CYCLES), 0b001),
    aIX("aIX", ACHIEVEMENT, List.of(ACHIEVEMENT, INSTRUCTIONS, PROD_CN), 0b001),

    cCN("cCN", CAN_CHEAT, List.of(CAN_CHEAT, CYCLES, NODES, INSTRUCTIONS), 0b100),
    cCI("cCI", CAN_CHEAT, List.of(CAN_CHEAT, CYCLES, INSTRUCTIONS, NODES), 0b100),
    cCX("cCX", CAN_CHEAT, List.of(CAN_CHEAT, CYCLES, PROD_NI), 0b100),

    cNC("cNC", CAN_CHEAT, List.of(CAN_CHEAT, NODES, CYCLES, INSTRUCTIONS), 0b010),
    cNI("cNI", CAN_CHEAT, List.of(CAN_CHEAT, NODES, INSTRUCTIONS, CYCLES), 0b010),
    cNX("cNX", CAN_CHEAT, List.of(CAN_CHEAT, NODES, PROD_CI), 0b010),

    cIC("cIC", CAN_CHEAT, List.of(CAN_CHEAT, INSTRUCTIONS, CYCLES, NODES), 0b001),
    cIN("cIN", CAN_CHEAT, List.of(CAN_CHEAT, INSTRUCTIONS, NODES, CYCLES), 0b001),
    cIX("cIX", CAN_CHEAT, List.of(CAN_CHEAT, INSTRUCTIONS, PROD_CN), 0b001);

    /** contains <tt>%d%s%d%s%d%s</tt> plus a bunch of <tt>*</tt> most likely */
    static final String[] FORMAT_STRINGS = {"%d%s%d%s%d%s", "%d%s%d%s**%d**%s", "%d%s**%d**%s%d%s", null, "**%d**%s%d%s%d%s"};

    private final String displayName;
    private final TISMetric<Boolean> admission;
    private final List<TISMetric<?>> metrics;
    private final Comparator<TISScore> scoreComparator;
    private final Set<TISType> supportedTypes;
    private final int scoreFormatId;

    TISCategory(String displayName, @NotNull TISMetric<Boolean> admission, @NotNull List<TISMetric<?>> metrics,
                int scoreFormatId) {
        this.displayName = displayName;
        this.admission = admission;
        this.metrics = metrics;
        this.scoreComparator = makeCategoryComparator(metrics);
        this.supportedTypes = admission == ACHIEVEMENT ? EnumSet.of(WITH_ACHIEVEMENT) : EnumSet.of(STANDARD, WITH_ACHIEVEMENT);
        this.scoreFormatId = scoreFormatId;
    }

    @Override
    public boolean supportsScore(@NotNull TISScore score) {
        return admission.get(score);
    }
}
