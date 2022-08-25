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

import com.faendir.zachtronics.bot.model.CategoryJava;
import com.faendir.zachtronics.bot.utils.Utils;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static com.faendir.zachtronics.bot.sc.model.ScMetric.*;
import static com.faendir.zachtronics.bot.sc.model.ScType.*;

@Getter
public enum ScCategory implements CategoryJava<ScCategory, ScScore, ScMetric, ScType> {
    C("C", List.of(CYCLES, REACTORS, SYMBOLS, ANY_FLAG), ScTypeSets.ALL, 0b100),
    CNB("CNB", List.of(CYCLES, REACTORS, SYMBOLS, NO_BUGS), ScTypeSets.ALL, 0b100),
    CNP("CNP", List.of(CYCLES, REACTORS, SYMBOLS, NO_PRECOG), ScTypeSets.ALL, 0b100),
    CNBP("CNBP", List.of(CYCLES, REACTORS, SYMBOLS, NO_FLAGS), ScTypeSets.ALL, 0b100),

    S("S", List.of(SYMBOLS, REACTORS, CYCLES, ANY_FLAG), ScTypeSets.ALL, 0b001),
    SNB("SNB", List.of(SYMBOLS, REACTORS, CYCLES, NO_BUGS), ScTypeSets.ALL, 0b001),
    SNP("SNP", List.of(SYMBOLS, REACTORS, CYCLES, NO_PRECOG), ScTypeSets.ALL, 0b001),
    SNBP("SNBP", List.of(SYMBOLS, REACTORS, CYCLES, NO_FLAGS), ScTypeSets.ALL, 0b001),

    RC("RC", List.of(REACTORS, CYCLES, SYMBOLS, ANY_FLAG), ScTypeSets.PROD, 0b110),
    RCNB("RCNB", List.of(REACTORS, CYCLES, SYMBOLS, NO_BUGS), ScTypeSets.PROD, 0b110),
    RCNP("RCNP", List.of(REACTORS, CYCLES, SYMBOLS, NO_PRECOG), ScTypeSets.PROD, 0b110),
    RCNBP("RCNBP", List.of(REACTORS, CYCLES, SYMBOLS, NO_FLAGS), ScTypeSets.PROD, 0b110),

    RS("RS", List.of(REACTORS, SYMBOLS, CYCLES, ANY_FLAG), ScTypeSets.PROD, 0b011),
    RSNB("RSNB", List.of(REACTORS, SYMBOLS, CYCLES, NO_BUGS), ScTypeSets.PROD, 0b011),
    RSNP("RSNP", List.of(REACTORS, SYMBOLS, CYCLES, NO_PRECOG), ScTypeSets.PROD, 0b011),
    RSNBP("RSNBP", List.of(REACTORS, SYMBOLS, CYCLES, NO_FLAGS), ScTypeSets.PROD, 0b011);

    /** contains <tt>%s%s%d%s%d%s</tt> plus a bunch of <tt>*</tt> most likely */
    static final String[] FORMAT_STRINGS = {"%s%s%d%s%d%s", "%s%s%d%s**%d**%s",
                                            "%s%s**%d**%s%d%s", "%s%s**%d**%s**%d**%s",
                                            "**%s**%s%d%s%d%s", "**%s**%s%d%s**%d**%s",
                                            "**%s**%s**%d**%s%d%s", "**%s**%s**%d**%s**%d**%s"};

    private final String displayName;
    @Accessors(fluent = true)
    private final List<ScMetric> metrics;
    private final Comparator<ScScore> scoreComparator;
    private final Set<ScType> supportedTypes;
    private final boolean bugFree;
    private final boolean precogFree;
    private final int scoreFormatId;

    ScCategory(String displayName, @NotNull List<ScMetric> metrics, Set<ScType> supportedTypes, int scoreFormatId) {
        this.displayName = displayName;
        this.metrics = metrics;
        this.scoreComparator = makeCategoryComparator(metrics);
        this.supportedTypes = supportedTypes;
        this.bugFree = metrics.contains(NO_BUGS) || metrics.contains(NO_FLAGS);
        this.precogFree = metrics.contains(NO_PRECOG) || metrics.contains(NO_FLAGS);
        this.scoreFormatId = scoreFormatId;
    }

    @Override
    public boolean supportsScore(@NotNull ScScore score) {
        return !(score.isBugged() && bugFree) && !(score.isPrecognitive() && precogFree);
    }

    static class ScTypeSets {
        static final EnumSet<ScType> RES = EnumSet.of(RESEARCH, PRODUCTION_FIXED, PRODUCTION_TRIVIAL, BOSS_FIXED);
        static final EnumSet<ScType> PROD = EnumSet.of(PRODUCTION, BOSS);
        static final EnumSet<ScType> ALL = Utils.enumSetUnion(RES, PROD);

        private ScTypeSets() {}
    }
}
