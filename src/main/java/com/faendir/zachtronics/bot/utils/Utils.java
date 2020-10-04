package com.faendir.zachtronics.bot.utils;

import java.util.Comparator;
import java.util.function.ToIntFunction;

public final class Utils {
    private Utils() {}

    public static <T> Comparator<T> makeComparator3(ToIntFunction<T> c1, ToIntFunction<T> c2, ToIntFunction<T> c3) {
        return Comparator.comparingInt(c1).thenComparingInt(c2).thenComparingInt(c3);
    }
}
