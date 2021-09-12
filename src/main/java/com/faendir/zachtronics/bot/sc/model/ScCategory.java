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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Set;

import static com.faendir.zachtronics.bot.sc.model.ScCategory.ScScoreComparators.*;
import static com.faendir.zachtronics.bot.sc.model.ScCategory.ScScoreFormatStrings.*;
import static com.faendir.zachtronics.bot.sc.model.ScType.*;
import static com.faendir.zachtronics.bot.utils.Utils.makeComparator3;

@RequiredArgsConstructor
public enum ScCategory implements Category {
    C("C", comparatorCRS, EnumSet.of(RESEARCH, PRODUCTION, PRODUCTION_TRIVIAL), false, false, F100),
    CNB("CNB", comparatorCRS, EnumSet.of(RESEARCH, PRODUCTION, PRODUCTION_TRIVIAL), true, false, F100),
    CNP("CNP", comparatorCRS, EnumSet.of(RESEARCH, PRODUCTION, PRODUCTION_TRIVIAL), false, true, F100),

    S("S", comparatorSRC, EnumSet.of(RESEARCH, PRODUCTION, PRODUCTION_TRIVIAL), false, false, F001),
    SNB("SNB", comparatorSRC, EnumSet.of(RESEARCH, PRODUCTION, PRODUCTION_TRIVIAL), true, false, F001),
    SNP("SNP", comparatorSRC, EnumSet.of(RESEARCH, PRODUCTION, PRODUCTION_TRIVIAL), false, true, F001),

    RC("RC", comparatorRCS, Collections.singleton(PRODUCTION), false, false, F110),
    RCNB("RCNB", comparatorRCS, Collections.singleton(PRODUCTION), true, false, F110),
    RCNP("RCNP", comparatorRCS, Collections.singleton(PRODUCTION), false, true, F110),

    RS("RS", comparatorRSC, Collections.singleton(PRODUCTION), false, false, F011),
    RSNB("RSNB", comparatorRSC, Collections.singleton(PRODUCTION), true, false, F011),
    RSNP("RSNP", comparatorRSC, Collections.singleton(PRODUCTION), false, true, F011);

    @Getter
    private final String displayName;
    @Getter
    private final Comparator<ScScore> scoreComparator;
    private final Set<ScType> supportedTypes;
    private final boolean bugFree;
    private final boolean precogFree;
    @Getter
    private final String formatStringLb;

    public boolean supportsPuzzle(@NotNull ScPuzzle puzzle) {
        return supportedTypes.contains(puzzle.getType()) && !(puzzle.isDeterministic() && precogFree);
    }

    public boolean supportsScore(@NotNull ScScore score) {
        return !(score.isBugged() && bugFree) && !(score.isPrecognitive() && precogFree);
    }

    static class ScScoreComparators {
        static final Comparator<ScScore> comparatorCRS = makeComparator3(ScScore::getCycles, ScScore::getReactors, ScScore::getSymbols);
        static final Comparator<ScScore> comparatorSRC = makeComparator3(ScScore::getSymbols, ScScore::getReactors, ScScore::getCycles);
        static final Comparator<ScScore> comparatorRCS = makeComparator3(ScScore::getReactors, ScScore::getCycles, ScScore::getSymbols);
        static final Comparator<ScScore> comparatorRSC = makeComparator3(ScScore::getReactors, ScScore::getSymbols, ScScore::getCycles);
    }

    static class ScScoreFormatStrings {
        static final String F100 = "**%s**%s/%d/%d";
        static final String F001 = "%s%s/%d/**%d**";
        static final String F110 = "**%s**%s/**%d**/%d";
        static final String F011 = "%s%s/**%d**/**%d**";
    }
}
