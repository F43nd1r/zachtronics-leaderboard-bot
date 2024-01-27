/*
 * Copyright (c) 2023
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
public enum ScCategory implements CategoryJava<ScCategory, ScScore, ScMetric> {
    C("C", List.of(CYCLES, REACTORS, SYMBOLS, ANY_FLAG), ANY_FLAG, ScTypeSets.ALL, 0b100),
    CNB("CNB", List.of(CYCLES, REACTORS, SYMBOLS, NO_BUGS), NO_BUGS, ScTypeSets.ALL, 0b100),
    CNP("CNP", List.of(CYCLES, REACTORS, SYMBOLS, NO_PRECOG), NO_PRECOG, ScTypeSets.ALL, 0b100),
    CNBP("CNBP", List.of(CYCLES, REACTORS, SYMBOLS, NO_FLAGS), NO_FLAGS, ScTypeSets.ALL, 0b100),

    S("S", List.of(SYMBOLS, REACTORS, CYCLES, ANY_FLAG), ANY_FLAG, ScTypeSets.ALL, 0b001),
    SNB("SNB", List.of(SYMBOLS, REACTORS, CYCLES, NO_BUGS), NO_BUGS, ScTypeSets.ALL, 0b001),
    SNP("SNP", List.of(SYMBOLS, REACTORS, CYCLES, NO_PRECOG), NO_PRECOG, ScTypeSets.ALL, 0b001),
    SNBP("SNBP", List.of(SYMBOLS, REACTORS, CYCLES, NO_FLAGS), NO_FLAGS, ScTypeSets.ALL, 0b001),

    RC("RC", List.of(REACTORS, CYCLES, SYMBOLS, ANY_FLAG), ANY_FLAG, ScTypeSets.PROD, 0b110),
    RCNB("RCNB", List.of(REACTORS, CYCLES, SYMBOLS, NO_BUGS), NO_BUGS, ScTypeSets.PROD, 0b110),
    RCNP("RCNP", List.of(REACTORS, CYCLES, SYMBOLS, NO_PRECOG), NO_PRECOG, ScTypeSets.PROD, 0b110),
    RCNBP("RCNBP", List.of(REACTORS, CYCLES, SYMBOLS, NO_FLAGS), NO_FLAGS, ScTypeSets.PROD, 0b110),

    RS("RS", List.of(REACTORS, SYMBOLS, CYCLES, ANY_FLAG), ANY_FLAG, ScTypeSets.PROD, 0b011),
    RSNB("RSNB", List.of(REACTORS, SYMBOLS, CYCLES, NO_BUGS), NO_BUGS, ScTypeSets.PROD, 0b011),
    RSNP("RSNP", List.of(REACTORS, SYMBOLS, CYCLES, NO_PRECOG), NO_PRECOG, ScTypeSets.PROD, 0b011),
    RSNBP("RSNBP", List.of(REACTORS, SYMBOLS, CYCLES, NO_FLAGS), NO_FLAGS, ScTypeSets.PROD, 0b011);

    /** contains <tt>%s%s%d%s%d%s</tt> plus a bunch of <tt>*</tt> most likely */
    static final String[] FORMAT_STRINGS = {"%s%s%d%s%d%s", "%s%s%d%s**%d**%s",
                                            "%s%s**%d**%s%d%s", "%s%s**%d**%s**%d**%s",
                                            "**%s**%s%d%s%d%s", "**%s**%s%d%s**%d**%s",
                                            "**%s**%s**%d**%s%d%s", "**%s**%s**%d**%s**%d**%s"};

    private final String displayName;
    private final List<ScMetric> metrics;
    private final Comparator<ScScore> scoreComparator;
    private final ScMetric eligibility;
    private final Set<ScType> supportedTypes;
    private final int scoreFormatId;

    ScCategory(String displayName, @NotNull List<ScMetric> metrics, ScMetric eligibility, Set<ScType> supportedTypes, int scoreFormatId) {
        this.displayName = displayName;
        this.metrics = metrics;
        this.scoreComparator = makeCategoryComparator(metrics);
        this.supportedTypes = supportedTypes;
        this.eligibility = eligibility;
        this.scoreFormatId = scoreFormatId;
    }

    @Override
    public boolean supportsScore(@NotNull ScScore score) {
        return !((Boolean) eligibility.getExtract().apply(score));
    }

    static class ScTypeSets {
        static final EnumSet<ScType> RES = EnumSet.of(RESEARCH, PRODUCTION_FIXED, PRODUCTION_TRIVIAL, BOSS_FIXED);
        static final EnumSet<ScType> PROD = EnumSet.of(PRODUCTION, BOSS);
        static final EnumSet<ScType> ALL = Utils.enumSetUnion(RES, PROD);

        private ScTypeSets() {}
    }
}
