package com.faendir.zachtronics.bot.model.sc;

import com.faendir.zachtronics.bot.discord.commands.UtilsKt;
import com.faendir.zachtronics.bot.leaderboards.Leaderboard;
import com.faendir.zachtronics.bot.leaderboards.ScLeaderboard;
import com.faendir.zachtronics.bot.model.Game;
import com.faendir.zachtronics.bot.utils.Result;
import kotlin.Pair;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class SpaceChem implements Game<ScCategory, ScScore, ScPuzzle, ScRecord> {
    @Getter
    private final String discordChannel = "spacechem";
    @Getter
    private final List<Leaderboard<ScCategory, ScScore, ScPuzzle, ScRecord>> leaderboards;
    @Getter
    private final String submissionSyntax = "<puzzle>:<cycles/reactors/symbols[/BP]> (e.g. 100/1/20 or 100/3/41/B) <youtube link>";

    public SpaceChem(ScLeaderboard scLeaderboard) {
        leaderboards = Collections.singletonList(scLeaderboard);
    }

    @NotNull
    @Override
    public Result<Pair<ScPuzzle, ScRecord>> parseSubmission(@NotNull Message message) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public List<ScCategory> parseCategory(@NotNull String name) {
        return Arrays.stream(ScCategory.values()).filter(c -> c.getDisplayName().equalsIgnoreCase(name))
                     .collect(Collectors.toList());
    }

    @NotNull
    @Override
    public Result<ScPuzzle> parsePuzzle(@NotNull String name) {
        String nameLower = name.toLowerCase();
        List<ScPuzzle> result = new ArrayList<>();
        for (ScPuzzle puzzle : ScPuzzle.values()) {
            String puzzleNameLower = puzzle.getDisplayName().toLowerCase();
            if (puzzleNameLower.contains(nameLower)) {
                if (puzzleNameLower.equals(nameLower))
                    return new Result.Success<>(puzzle);
                else
                    result.add(puzzle);
            }
        }
        return UtilsKt.getSinglePuzzle(result, name);
    }

    /** ccc/r/ss */
    private static final Pattern SCORE_REGEX = Pattern.compile(
            "\\**(?<cycles>\\d+)\\**c?\\**/\\**(?<reactors>\\d+)\\**r?\\**/\\**(?<symbols>\\d+)\\**s?\\**");

    public static ScScore parseScore(@NotNull String string) {
        Matcher m = SCORE_REGEX.matcher(string);
        if (m.matches()) {
            int cycles = Integer.parseInt(m.group(1));
            int reactors = Integer.parseInt(m.group(2));
            int symbols = Integer.parseInt(m.group(3));
            return new ScScore(cycles, reactors, symbols, false, false);
        }
        else {
            return null;
        }
    }
}