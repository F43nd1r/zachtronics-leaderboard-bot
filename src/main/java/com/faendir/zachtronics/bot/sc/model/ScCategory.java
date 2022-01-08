/*
 * Copyright (c) 2021
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

import com.faendir.zachtronics.bot.model.Category;
import com.faendir.zachtronics.bot.model.Metric;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.faendir.zachtronics.bot.sc.model.ScCategory.ScScoreFormatStrings.*;
import static com.faendir.zachtronics.bot.sc.model.ScMetric.*;
import static com.faendir.zachtronics.bot.sc.model.ScType.*;

@Getter
public enum ScCategory implements Category {
    C("C", List.of(CYCLES, REACTORS, SYMBOLS, ANY_FLAG), ScTypeSets.ALL, F100),
    CNB("CNB", List.of(CYCLES, REACTORS, SYMBOLS, NO_BUGS), ScTypeSets.ALL, F100),
    CNP("CNP", List.of(CYCLES, REACTORS, SYMBOLS, NO_PRECOG), ScTypeSets.ALL, F100),
    CNBP("CNBP", List.of(CYCLES, REACTORS, SYMBOLS, NO_FLAGS), ScTypeSets.ALL, F100),

    S("S", List.of(SYMBOLS, REACTORS, CYCLES, ANY_FLAG), ScTypeSets.ALL, F001),
    SNB("SNB", List.of(SYMBOLS, REACTORS, CYCLES, NO_BUGS), ScTypeSets.ALL, F001),
    SNP("SNP", List.of(SYMBOLS, REACTORS, CYCLES, NO_PRECOG), ScTypeSets.ALL, F001),
    SNBP("SNBP", List.of(SYMBOLS, REACTORS, CYCLES, NO_FLAGS), ScTypeSets.ALL, F001),

    RC("RC", List.of(REACTORS, CYCLES, SYMBOLS, ANY_FLAG), ScTypeSets.PROD, F110),
    RCNB("RCNB", List.of(REACTORS, CYCLES, SYMBOLS, NO_BUGS), ScTypeSets.PROD, F110),
    RCNP("RCNP", List.of(REACTORS, CYCLES, SYMBOLS, NO_PRECOG), ScTypeSets.PROD, F110),
    RCNBP("RCNBP", List.of(REACTORS, CYCLES, SYMBOLS, NO_FLAGS), ScTypeSets.PROD, F110),

    RS("RS", List.of(REACTORS, SYMBOLS, CYCLES, ANY_FLAG), ScTypeSets.PROD, F011),
    RSNB("RSNB", List.of(REACTORS, SYMBOLS, CYCLES, NO_BUGS), ScTypeSets.PROD, F011),
    RSNP("RSNP", List.of(REACTORS, SYMBOLS, CYCLES, NO_PRECOG), ScTypeSets.PROD, F011),
    RSNBP("RSNBP", List.of(REACTORS, SYMBOLS, CYCLES, NO_FLAGS), ScTypeSets.PROD, F011);

    private final String displayName;
    private final List<Metric> metrics;
    private final Comparator<ScScore> scoreComparator;
    private final Set<ScType> supportedTypes;
    private final boolean bugFree;
    private final boolean precogFree;
    /** contains <tt>%s%s%s%d%s%d%s</tt> plus a bunch of <tt>*</tt> most likely */
    private final String scoreFormatString;

    @SuppressWarnings("unchecked")
    ScCategory(String displayName, @NotNull List<ScMetric> metrics, Set<ScType> supportedTypes, String scoreFormatString) {
        this.displayName = displayName;
        this.metrics = (List<Metric>)(List<?>) metrics;
        this.scoreComparator = metrics.stream()
                                      .map(ScMetric::getExtract)
                                      .filter(Objects::nonNull)
                                      .map(Comparator::comparingInt)
                                      .reduce(Comparator::thenComparing)
                                      .orElseThrow();
        this.supportedTypes = supportedTypes;
        this.bugFree = metrics.contains(NO_BUGS) || metrics.contains(NO_FLAGS);
        this.precogFree = metrics.contains(NO_PRECOG) || metrics.contains(NO_FLAGS);
        this.scoreFormatString = scoreFormatString;
    }

    public boolean supportsScore(@NotNull ScScore score) {
        return !(score.isBugged() && bugFree) && !(score.isPrecognitive() && precogFree);
    }

    static class ScScoreFormatStrings {
        static final String F000 = "%s%s%s%d%s%d%s";
        static final String F100 = "**%s**%s%s%d%s%d%s";
        static final String F001 = "%s%s%s%d%s**%d**%s";
        static final String F110 = "**%s**%s%s**%d**%s%d%s";
        static final String F011 = "%s%s%s**%d**%s**%d**%s";

        private ScScoreFormatStrings() {}
    }

    static class ScTypeSets {
        static final EnumSet<ScType> RES = EnumSet.of(RESEARCH, PRODUCTION_TRIVIAL);
        static final EnumSet<ScType> PROD = EnumSet.of(PRODUCTION, BOSS);
        static final EnumSet<ScType> ALL = enumSetUnion(RES, PROD);

        @NotNull
        public static <T extends Enum<T>> EnumSet<T> enumSetUnion(EnumSet<T> s1, EnumSet<T> s2) {
            EnumSet<T> res = EnumSet.copyOf(s1);
            res.addAll(s2);
            return res;
        }

        private ScTypeSets() {}
    }
}
