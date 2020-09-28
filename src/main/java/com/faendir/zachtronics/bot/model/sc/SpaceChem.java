package com.faendir.zachtronics.bot.model.sc;

import com.faendir.zachtronics.bot.discord.commands.UtilsKt;
import com.faendir.zachtronics.bot.leaderboards.Leaderboard;
import com.faendir.zachtronics.bot.leaderboards.sc.ScRedditLeaderboard;
import com.faendir.zachtronics.bot.model.Game;
import com.faendir.zachtronics.bot.utils.Result;
import kotlin.Pair;
import kotlin.text.Regex;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;
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
    private final String submissionSyntax =
            "<puzzle> (<cycles/reactors/symbols[/BP]>) by <author> <youtube link>";

    public SpaceChem(ScRedditLeaderboard scLeaderboard) {
        leaderboards = Collections.singletonList(scLeaderboard);
    }

    private static final Pattern submissionRegex = Pattern.compile(
            "!submit\\s+(?<puzzle>.+)\\s+\\((?<score>" + ScScore.REGEX_BP_SCORE +
            ")\\)\\s+(?:by\\s+)?(?<author>.+?)\\s+(?<link>\\S+)\\s*",
            Pattern.CASE_INSENSITIVE);

    @NotNull
    @Override
    public Result<Pair<ScPuzzle, ScRecord>> parseSubmission(@NotNull Message message) {
        Matcher m = submissionRegex.matcher(message.getContentRaw());
        if (!m.matches())
            return new Result.Failure<>("Couldn't parse request");

        return parsePuzzle(m.group("puzzle")).flatMap(puzzle -> {
            ScScore score = ScScore.parseBPScore(m.group("score"));
            if (score == null)
                return new Result.Failure<>("Couldn't parse score");
            ScRecord record = new ScRecord(score, m.group("author"), m.group("link"), m.group("oldRNG") != null);
            return new Result.Success<>(new Pair<>(puzzle, record));
        });
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
        return Arrays.stream(ScPuzzle.values())
                     .filter(p -> p.getDisplayName().equalsIgnoreCase(name))
                     .findFirst()
                     .<Result<ScPuzzle>>map(Result.Success::new)
                     .orElse(UtilsKt.getSinglePuzzle(UtilsKt.getMatchingPuzzles(ScPuzzle.values(),
                                                                                name, new Regex("[\\s-/,:]+")), name));
    }

    private static final Set<Long> WIKI_ADMINS = Set.of(295868901042946048L, // 12345ieee,
                                                        516462621382410260L, // TT
                                                        185983061190508544L  // Zig
    );
    @Override
    public boolean hasWritePermission(@Nullable Member member) {
        if (member == null)
            return false;
        return WIKI_ADMINS.contains(member.getIdLong());
    }
}