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
    C("C", List.of(CYCLES, REACTORS, SYMBOLS, ANY_FLAG), EnumSet.of(RESEARCH, PRODUCTION, PRODUCTION_TRIVIAL, BOSS), F100),
    CNB("CNB", List.of(CYCLES, REACTORS, SYMBOLS, NO_BUGS), EnumSet.of(RESEARCH, PRODUCTION, PRODUCTION_TRIVIAL, BOSS), F100),
    CNP("CNP", List.of(CYCLES, REACTORS, SYMBOLS, NO_PRECOG), EnumSet.of(RESEARCH, PRODUCTION, PRODUCTION_TRIVIAL, BOSS), F100),

    S("S", List.of(SYMBOLS, REACTORS, CYCLES, ANY_FLAG), EnumSet.of(RESEARCH, PRODUCTION, PRODUCTION_TRIVIAL, BOSS), F001),
    SNB("SNB", List.of(SYMBOLS, REACTORS, CYCLES, NO_BUGS), EnumSet.of(RESEARCH, PRODUCTION, PRODUCTION_TRIVIAL, BOSS), F001),
    SNP("SNP", List.of(SYMBOLS, REACTORS, CYCLES, NO_PRECOG), EnumSet.of(RESEARCH, PRODUCTION, PRODUCTION_TRIVIAL, BOSS), F001),

    RC("RC", List.of(REACTORS, CYCLES, SYMBOLS, ANY_FLAG), EnumSet.of(PRODUCTION, BOSS), F110),
    RCNB("RCNB", List.of(REACTORS, CYCLES, SYMBOLS, NO_BUGS), EnumSet.of(PRODUCTION, BOSS), F110),
    RCNP("RCNP", List.of(REACTORS, CYCLES, SYMBOLS, NO_PRECOG), EnumSet.of(PRODUCTION, BOSS), F110),

    RS("RS", List.of(REACTORS, SYMBOLS, CYCLES, ANY_FLAG), EnumSet.of(PRODUCTION, BOSS), F011),
    RSNB("RSNB", List.of(REACTORS, SYMBOLS, CYCLES, NO_BUGS), EnumSet.of(PRODUCTION, BOSS), F011),
    RSNP("RSNP", List.of(REACTORS, SYMBOLS, CYCLES, NO_PRECOG), EnumSet.of(PRODUCTION, BOSS), F011);

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
        this.bugFree = metrics.contains(NO_BUGS);
        this.precogFree = metrics.contains(NO_PRECOG);
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
}
