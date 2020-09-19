package com.faendir.zachtronics.bot.model.sc;

import com.faendir.zachtronics.bot.leaderboards.ScLeaderboard;
import com.faendir.zachtronics.bot.leaderboards.Leaderboard;
import com.faendir.zachtronics.bot.model.Game;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class SpaceChem implements Game<ScCategory, ScScore, ScPuzzle> {
    @Getter
    private final String discordChannel = "spacechem";
    @Getter
    private final List<Leaderboard<ScCategory, ScScore, ScPuzzle>> leaderboards;

    public SpaceChem(ScLeaderboard scLeaderboard) {
        leaderboards = Collections.singletonList(scLeaderboard);
    }

    @NotNull
    @Override
    public List<ScPuzzle> findPuzzleByName(@NotNull String name) {
        String nameLower = name.toLowerCase();
        return Arrays.stream(ScPuzzle.values()).filter(p -> p.getDisplayName().toLowerCase().contains(nameLower))
                     .collect(Collectors.toList());
    }

    @Nullable
    @Override
    public ScScore parseScore(@NotNull ScPuzzle puzzle, @NotNull String string) {
        return parseScore(string);
    }

    public static final Pattern SCORE_REGEX = Pattern.compile(
            "\\(?\\**(?<cycles>\\d+)\\**c?\\**/\\**(?<reactors>\\d+)\\**r?\\**/\\**(?<symbols>\\d+)\\**s?\\**\\)?"); // (ccc/r/ss)

    public static ScScore parseScore(@NotNull String string) {
        Matcher m = SCORE_REGEX.matcher(string);
        if (m.matches()) {
            int cycles = Integer.parseInt(m.group(1));
            int reactors = Integer.parseInt(m.group(2));
            int symbols = Integer.parseInt(m.group(3));
            return new ScScore(cycles, reactors, symbols);
        }
        else {
            return null;
        }
    }
}