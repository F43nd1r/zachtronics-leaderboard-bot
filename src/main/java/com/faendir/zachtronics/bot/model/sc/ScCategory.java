package com.faendir.zachtronics.bot.model.sc;

import com.faendir.zachtronics.bot.model.Category;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

import static com.faendir.zachtronics.bot.model.sc.ScCategory.ScScoreComparators.*;
import static com.faendir.zachtronics.bot.model.sc.ScCategory.ScScoreFormatStrings.*;
import static com.faendir.zachtronics.bot.model.sc.ScType.*;
import static com.faendir.zachtronics.bot.utils.Utils.makeComparator3;

@RequiredArgsConstructor
public enum ScCategory implements Category<ScScore, ScPuzzle> {
    C("C", "c/r/s[/BP]", comparatorCRS, Set.of(RESEARCH, PRODUCTION, PRODUCTION_TRIVIAL), false, false, F100),
    S("S", "c/r/s[/BP]", comparatorSRC, Set.of(RESEARCH, PRODUCTION, PRODUCTION_TRIVIAL), false, false, F001),
    RC("RC", "c/r/s[/BP]", comparatorRCS, Collections.singleton(PRODUCTION), false, false, F110),
    RS("RS", "c/r/s[/BP]", comparatorRSC, Collections.singleton(PRODUCTION), false, false, F011),

    CNB("CNB", "c/r/s[/P]", comparatorCRS, Set.of(RESEARCH, PRODUCTION, PRODUCTION_TRIVIAL), true, false, F100),
    SNB("SNB", "c/r/s[/P]", comparatorSRC, Set.of(RESEARCH, PRODUCTION, PRODUCTION_TRIVIAL), true, false, F001),
    RCNB("RCNB", "c/r/s[/P]", comparatorRCS, Collections.singleton(PRODUCTION), true, false, F110),
    RSNB("RSNB", "c/r/s[/P]", comparatorRSC, Collections.singleton(PRODUCTION), true, false, F011),

    CNP("CNP", "c/r/s[/B]", comparatorCRS, Set.of(RESEARCH, PRODUCTION, PRODUCTION_TRIVIAL), false, true, F100),
    SNP("SNP", "c/r/s[/B]", comparatorSRC, Set.of(RESEARCH, PRODUCTION, PRODUCTION_TRIVIAL), false, true, F001),
    RCNP("RCNP", "c/r/s[/B]", comparatorRCS, Collections.singleton(PRODUCTION), false, true, F110),
    RSNP("RSNP", "c/r/s[/B]", comparatorRSC, Collections.singleton(PRODUCTION), false, true, F011);

    @Getter
    private final String displayName;
    @Getter
    private final String contentDescription;
    @Getter
    private final Comparator<ScScore> scoreComparator;
    private final Set<ScType> supportedTypes;
    private final boolean bugFree;
    private final boolean precogFree;
    @Getter
    private final String formatStringLb;

    @Override
    public boolean supportsPuzzle(@NotNull ScPuzzle puzzle) {
        return supportedTypes.contains(puzzle.getType()) && !(puzzle.isDeterministic() && precogFree);
    }

    @Override
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
