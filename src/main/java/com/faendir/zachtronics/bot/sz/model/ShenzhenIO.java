package com.faendir.zachtronics.bot.sz.model;

import com.faendir.zachtronics.bot.utils.UtilsKt;
import org.jetbrains.annotations.NotNull;

public class ShenzhenIO {
    @NotNull
    public static SzPuzzle parsePuzzle(@NotNull String name) {
        return UtilsKt.getSingleMatchingPuzzle(SzPuzzle.values(), name);
    }
}