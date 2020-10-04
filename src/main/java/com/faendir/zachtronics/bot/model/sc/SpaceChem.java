package com.faendir.zachtronics.bot.model.sc;

import com.faendir.zachtronics.bot.leaderboards.Leaderboard;
import com.faendir.zachtronics.bot.model.Game;
import com.faendir.zachtronics.bot.utils.Result;
import com.faendir.zachtronics.bot.utils.UtilsKt;
import kotlin.Pair;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SpaceChem implements Game<ScCategory, ScScore, ScPuzzle, ScRecord> {
    @Getter
    private final String discordChannel = "spacechem";
    @Getter
    private final String displayName = "SpaceChem";
    @Getter
    private final List<Leaderboard<ScCategory, ScScore, ScPuzzle, ScRecord>> leaderboards;
    @Getter
    private final String submissionSyntax = "<puzzle> (<cycles/reactors/symbols[/BP]>) by <author> <youtube link>";
    @Getter
    private final String categoryHelp = "The supported categories for researches are Cycles and Symbols, " +
                                        "ties broken by the other metric.\n" +
                                        "For productions we have in addition Cycles and Symbols at minimum reactors.\n" +
                                        "Boss levels are currently not supported.\n" +
                                        "\n" +
                                        "Each category also comes in a \"No bugs allowed\" and a " +
                                        "\"No precognition allowed\" version (for random puzzles), " +
                                        "whose record is very often equal to the unrestricted record.\n" +
                                        "\n" +
                                        "For further information see: " +
                                        "<https://www.reddit.com/r/spacechem/wiki/index#wiki_explanations>\n" +
                                        "\n" +
                                        "All categories are:\n" +
                                        Arrays.stream(ScCategory.values())
                                              .map(c -> c.getDisplayName() + " (" + c.getContentDescription() + ")")
                                              .collect(Collectors.joining("\n", "```", "```"));
    @Getter
    private final String scoreHelp = "Scores are in the format cycles/reactors/symbols.\n" +
                                     "To mark a score as bugged in a submission append a `/B`, (`c/r/s/B`), " +
                                     "`/P` for precognition, `/BP` for both.";

    private static final Pattern SUBMISSION_REGEX = Pattern.compile(
            "!submit\\s+" +
            "(?<puzzle>.+)\\s+" +
            "\\((?<score>" + ScScore.REGEX_BP_SCORE + ")\\)\\s+" +
            "(?:by\\s+)?(?<author>.+?)\\s+" +
            "(?<link>\\S+)\\s*",
            Pattern.CASE_INSENSITIVE);

    @NotNull
    @Override
    public Result<Pair<ScPuzzle, ScRecord>> parseSubmission(@NotNull Message message) {
        Matcher m = SUBMISSION_REGEX.matcher(message.getContentRaw());
        if (!m.matches())
            return Result.failure("Couldn't parse request");

        return parsePuzzle(m.group("puzzle")).flatMap(puzzle -> {
            ScScore score = ScScore.parseBPScore(m.group("score"));
            if (score == null)
                return Result.failure("Couldn't parse score");
            ScRecord record = new ScRecord(score, m.group("author"), m.group("link"), m.group("oldRNG") != null);
            return Result.success(new Pair<>(puzzle, record));
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
                     .map(Result::success)
                     .orElse(UtilsKt.getSingleMatchingPuzzle(ScPuzzle.values(), name));
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