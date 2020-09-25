package com.faendir.zachtronics.bot.model.sc;

import com.faendir.zachtronics.bot.model.Category;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.function.ToIntFunction;

import static com.faendir.zachtronics.bot.model.sc.ScCategory.ScScoreComparators.*;

@RequiredArgsConstructor
public enum ScCategory implements Category<ScCategory, ScScore, ScPuzzle> {
    CYCLES("C", "c/r/s[/BP]", comparatorCRS, Set.of(ScType.RESEARCH, ScType.PRODUCTION, ScType.PRODUCTION_TRIVIAL), false),
    SYMBOLS("S", "c/r/s[/BP]", comparatorSRC, Set.of(ScType.RESEARCH, ScType.PRODUCTION, ScType.PRODUCTION_TRIVIAL), false),
    RC("RC", "c/r/s[/BP]", comparatorRCS, Collections.singleton(ScType.PRODUCTION), false),
    RS("RS", "c/r/s[/BP]", comparatorRSC, Collections.singleton(ScType.PRODUCTION), false),

    CNB("CNB", "c/r/s[/P]", comparatorCRS, Set.of(ScType.RESEARCH, ScType.PRODUCTION, ScType.PRODUCTION_TRIVIAL), false),
    SNB("SNB", "c/r/s[/P]", comparatorSRC, Set.of(ScType.RESEARCH, ScType.PRODUCTION, ScType.PRODUCTION_TRIVIAL), false),
    RCNB("RCNB", "c/r/s[/P]", comparatorRCS, Collections.singleton(ScType.PRODUCTION), false),
    RSNB("RSNB", "c/r/s[/P]", comparatorRSC, Collections.singleton(ScType.PRODUCTION), false),

    CNP("CNP", "c/r/s[/B]", comparatorCRS, Set.of(ScType.RESEARCH, ScType.PRODUCTION, ScType.PRODUCTION_TRIVIAL), true),
    SNP("SNP", "c/r/s[/B]", comparatorSRC, Set.of(ScType.RESEARCH, ScType.PRODUCTION, ScType.PRODUCTION_TRIVIAL), true),
    RCNP("RCNP", "c/r/s[/B]", comparatorRCS, Collections.singleton(ScType.PRODUCTION), true),
    RSNP("RSNP", "c/r/s[/B]", comparatorRSC, Collections.singleton(ScType.PRODUCTION), true);

    @Getter
    private final String displayName;
    @Getter
    private final String contentDescription;
    private final Comparator<ScScore> comparator;
    private final Set<ScType> supportedTypes;
    private final boolean needsRandomness;

    @Override
    public boolean isBetterOrEqual(@NotNull ScScore s1, @NotNull ScScore s2) {
        return comparator.compare(s1, s2) <= 0;
    }

    @Override
    public boolean supportsPuzzle(@NotNull ScPuzzle puzzle) {
        return supportedTypes.contains(puzzle.getType()) && !(puzzle.isDeterministic() && needsRandomness);
    }

    @Override
    public boolean supportsScore(@NotNull ScScore score) {
        return !score.usesPrecognition() || needsRandomness;
    }

    static class ScScoreComparators {
        private static <T> Comparator<T> makeComparator3(ToIntFunction<T> c1, ToIntFunction<T> c2, ToIntFunction<T> c3) {
            return Comparator.comparingInt(c1).thenComparingInt(c2).thenComparingInt(c3);
        }
        static final Comparator<ScScore> comparatorCRS = makeComparator3(ScScore::getCycles, ScScore::getReactors, ScScore::getSymbols);
        static final Comparator<ScScore> comparatorSRC = makeComparator3(ScScore::getSymbols, ScScore::getReactors, ScScore::getCycles);
        static final Comparator<ScScore> comparatorRCS = makeComparator3(ScScore::getReactors, ScScore::getCycles, ScScore::getSymbols);
        static final Comparator<ScScore> comparatorRSC = makeComparator3(ScScore::getReactors, ScScore::getSymbols, ScScore::getCycles);

    }
}
