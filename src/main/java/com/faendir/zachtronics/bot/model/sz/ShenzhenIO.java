package com.faendir.zachtronics.bot.model.sz;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ShenzhenIO implements Game<SzCategory, SzScore, SzPuzzle, SzRecord> {
    @Getter
    private final String discordChannel = "shenzhen-io";
    @Getter
    private final String displayName = "Shenzhen I/O";
    @Getter
    private final List<Leaderboard<SzCategory, SzScore, SzPuzzle, SzRecord>> leaderboards;
    @Getter
    private final String submissionSyntax = "<puzzle> (<cost/power/lines>) by <author> <link>";
    @Getter
    private final String categoryHelp = "Categories are named by Primary metric, then Secondary.\n" +
                                        "Ties in both are broken by the remaining metric.\n" +
                                        "\n" +
                                        "All categories are:\n" +
                                        Arrays.stream(SzCategory.values())
                                              .map(c -> c.getDisplayName() + " (" + c.getContentDescription() + ")")
                                              .collect(Collectors.joining("\n", "```", "```"));
    @Getter
    private final String scoreHelp = "Scores are in the format cost/power/lines.";

    private static final Pattern SUBMISSION_REGEX = Pattern.compile(
            "!submit\\s+" +
            "(?<puzzle>.+)\\s+" +
            "\\(" + SzScore.REGEX_SIMPLE_SCORE + "\\)\\s+" +
            "(?:by\\s+)?(?<author>.+?)?\\s+" +
            "(?<link>\\S+)\\s*",
            Pattern.CASE_INSENSITIVE);

    @NotNull
    @Override
    public Result<Pair<SzPuzzle, SzRecord>> parseSubmission(@NotNull Message message) {
        Matcher m = SUBMISSION_REGEX.matcher(message.getContentRaw());
        if (!m.matches())
            return Result.failure("Couldn't parse request");

        return parsePuzzle(m.group("puzzle")).flatMap(puzzle -> {
            SzScore score = SzScore.parseSimpleScore(m);
            SzRecord record = new SzRecord(score, m.group("author"), m.group("link"));
            return Result.success(new Pair<>(puzzle, record));
        });
    }

    @NotNull
    @Override
    public List<SzCategory> parseCategory(@NotNull String name) {
        return Arrays.stream(SzCategory.values()).filter(c -> c.getDisplayName().equalsIgnoreCase(name))
                     .collect(Collectors.toList());
    }

    @NotNull
    @Override
    public Result<SzPuzzle> parsePuzzle(@NotNull String name) {
        return UtilsKt.getSingleMatchingPuzzle(SzPuzzle.values(), name);
    }

    private static final long WIKI_ADMIN = 295868901042946048L; // 12345ieee
    @Override
    public boolean hasWritePermission(@Nullable Member member) {
        if (member == null)
            return false;
        return member.getIdLong() == WIKI_ADMIN;
    }
}