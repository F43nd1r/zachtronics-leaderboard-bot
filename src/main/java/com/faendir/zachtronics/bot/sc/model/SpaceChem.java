package com.faendir.zachtronics.bot.sc.model;

import com.faendir.zachtronics.bot.utils.UtilsKt;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class SpaceChem {
    @NotNull
    public static ScPuzzle parsePuzzle(@NotNull String name) {
        return Arrays.stream(ScPuzzle.values())
                .filter(p -> p.getDisplayName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> UtilsKt.getSingleMatchingPuzzle(ScPuzzle.values(), name));
    }
}