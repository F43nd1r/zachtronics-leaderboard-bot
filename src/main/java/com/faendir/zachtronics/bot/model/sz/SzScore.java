package com.faendir.zachtronics.bot.model.sz;

import com.faendir.zachtronics.bot.model.Score;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Value
public class SzScore implements Score {
    int cost;
    int power;
    int lines;

    /** ccc/r/ss */
    @NotNull
    @Override
    public String toDisplayString() {
        return cost + "/" + power + "/" + lines;
    }

    /** cc/ppp/ll */
    public static final Pattern REGEX_SIMPLE_SCORE = Pattern.compile(
            "\\**(?<cost>\\d+)\\**/\\**(?<power>\\d+)\\**/\\**(?<lines>\\d+)\\**");

    /** we assume m matches */
    public static SzScore parseSimpleScore(@NotNull Matcher m) {
        int cost = Integer.parseInt(m.group("cost"));
        int power = Integer.parseInt(m.group("power"));
        int lines = Integer.parseInt(m.group("lines"));
        return new SzScore(cost, power, lines);
    }
}
