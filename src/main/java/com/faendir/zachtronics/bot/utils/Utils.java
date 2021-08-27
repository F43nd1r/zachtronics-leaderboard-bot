package com.faendir.zachtronics.bot.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.function.ToIntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Utils {
    private Utils() {}

    public static <T> Comparator<T> makeComparator3(ToIntFunction<T> c1, ToIntFunction<T> c2, ToIntFunction<T> c3) {
        return Comparator.comparingInt(c1).thenComparingInt(c2).thenComparingInt(c3);
    }

    private static final Pattern PASTEBIN_PATTERN = Pattern.compile("(?:https?://)?pastebin.com/(?:raw/)?(\\w+)");
    @NotNull
    public static String rawContentURL(@NotNull String link) {
        Matcher m = PASTEBIN_PATTERN.matcher(link);
        if (m.matches()) { // pastebin has an easy way to get raw text
            return "https://pastebin.com/raw/" + m.group(1);
        }
        else
            return link;
    }
}
