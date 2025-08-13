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

package com.faendir.zachtronics.bot.sc.model;

import com.faendir.zachtronics.bot.model.CategoryJava;
import com.faendir.zachtronics.bot.utils.Utils;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static com.faendir.zachtronics.bot.sc.model.ScMetric.*;
import static com.faendir.zachtronics.bot.sc.model.ScType.*;

@Getter
public enum ScCategory implements CategoryJava<ScCategory, ScScore, ScMetric<?>> {
    C(ANY_FLAG, List.of(CYCLES, REACTORS, SYMBOLS, ANY_FLAG), ScTypeSets.ALL, 0b100),
    CNB(NO_BUGS, List.of(CYCLES, REACTORS, SYMBOLS, NO_BUGS), ScTypeSets.ALL, 0b100),
    CNP(NO_PRECOG, List.of(CYCLES, REACTORS, SYMBOLS, NO_PRECOG), ScTypeSets.ALL, 0b100),
    CNBP(NO_FLAGS, List.of(CYCLES, REACTORS, SYMBOLS, NO_FLAGS), ScTypeSets.ALL, 0b100),

    S(ANY_FLAG, List.of(SYMBOLS, REACTORS, CYCLES, ANY_FLAG), ScTypeSets.ALL, 0b001),
    SNB(NO_BUGS, List.of(SYMBOLS, REACTORS, CYCLES, NO_BUGS), ScTypeSets.ALL, 0b001),
    SNP(NO_PRECOG, List.of(SYMBOLS, REACTORS, CYCLES, NO_PRECOG), ScTypeSets.ALL, 0b001),
    SNBP(NO_FLAGS, List.of(SYMBOLS, REACTORS, CYCLES, NO_FLAGS), ScTypeSets.ALL, 0b001),

    RC(ANY_FLAG, List.of(REACTORS, CYCLES, SYMBOLS, ANY_FLAG), ScTypeSets.PROD, 0b110),
    RCNB(NO_BUGS, List.of(REACTORS, CYCLES, SYMBOLS, NO_BUGS), ScTypeSets.PROD, 0b110),
    RCNP(NO_PRECOG, List.of(REACTORS, CYCLES, SYMBOLS, NO_PRECOG), ScTypeSets.PROD, 0b110),
    RCNBP(NO_FLAGS, List.of(REACTORS, CYCLES, SYMBOLS, NO_FLAGS), ScTypeSets.PROD, 0b110),

    RS(ANY_FLAG, List.of(REACTORS, SYMBOLS, CYCLES, ANY_FLAG), ScTypeSets.PROD, 0b011),
    RSNB(NO_BUGS, List.of(REACTORS, SYMBOLS, CYCLES, NO_BUGS), ScTypeSets.PROD, 0b011),
    RSNP(NO_PRECOG, List.of(REACTORS, SYMBOLS, CYCLES, NO_PRECOG), ScTypeSets.PROD, 0b011),
    RSNBP(NO_FLAGS, List.of(REACTORS, SYMBOLS, CYCLES, NO_FLAGS), ScTypeSets.PROD, 0b011);

    /** contains <tt>%s%s%d%s%d%s</tt> plus a bunch of <tt>*</tt> most likely */
    static final String[] FORMAT_STRINGS = {"%s%s%d%s%d%s", "%s%s%d%s**%d**%s",
                                            "%s%s**%d**%s%d%s", "%s%s**%d**%s**%d**%s",
                                            "**%s**%s%d%s%d%s", "**%s**%s%d%s**%d**%s",
                                            "**%s**%s**%d**%s%d%s", "**%s**%s**%d**%s**%d**%s"};

    private final String displayName;
    private final ScMetric<Boolean> admission;
    private final List<ScMetric<?>> metrics;
    private final Comparator<ScScore> scoreComparator;
    private final Set<ScType> supportedTypes;
    private final int scoreFormatId;

    ScCategory(@NotNull ScMetric<Boolean> admission, @NotNull List<ScMetric<?>> metrics, Set<ScType> supportedTypes, int scoreFormatId) {
        this.displayName = name();
        this.admission = admission;
        this.metrics = metrics;
        this.scoreComparator = makeCategoryComparator(metrics);
        this.supportedTypes = supportedTypes;
        this.scoreFormatId = scoreFormatId;
    }

    @Override
    public boolean supportsScore(@NotNull ScScore score) {
        return !admission.get(score);
    }

    static class ScTypeSets {
        static final EnumSet<ScType> RES = EnumSet.of(RESEARCH, PRODUCTION_FIXED, PRODUCTION_TRIVIAL, BOSS_FIXED);
        static final EnumSet<ScType> PROD = EnumSet.of(PRODUCTION, BOSS);
        static final EnumSet<ScType> ALL = Utils.enumSetUnion(RES, PROD);

        private ScTypeSets() {}
    }
}
