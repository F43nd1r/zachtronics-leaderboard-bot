package com.faendir.zachtronics.bot.sc.discord;

import com.faendir.zachtronics.bot.generic.discord.AbstractSubmitCommand;
import com.faendir.zachtronics.bot.model.Leaderboard;
import com.faendir.zachtronics.bot.sc.archive.ScArchive;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.ScRecord;
import com.faendir.zachtronics.bot.sc.model.ScScore;
import com.faendir.zachtronics.bot.sc.model.SpaceChem;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.List;
import java.util.regex.Pattern;

//@Component
@RequiredArgsConstructor
public class ScSubmitCommand extends AbstractSubmitCommand<ScPuzzle, ScRecord> {
    private final SpaceChem spaceChem;
    private final ScArchive archive;
    @Getter
    private final List<Leaderboard<?, ?, ScPuzzle, ScRecord>> leaderboards;

    private static final Pattern SUBMISSION_REGEX = Pattern.compile(
            "!submit\\s+" +
            "(?<puzzle>.+)\\s+" +
            "\\((?<score>" + ScScore.REGEX_BP_SCORE + ")\\)\\s+" +
            "(?:by\\s+)?(?<author>.+?)\\s+" +
            "(?<link>\\S+)\\s*",
            Pattern.CASE_INSENSITIVE);

    @NotNull
    @Override
    public Mono<Tuple2<ScPuzzle, ScRecord>> parseSubmission(@NotNull List<? extends ApplicationCommandInteractionOption> options, @NotNull User user) {
        return null;
    }

    @NotNull
    @Override
    public ApplicationCommandOptionData buildData() {
        return null;
    }

    /*@NotNull
    @Override
    public Result<Pair<ScPuzzle, ScRecord>> parseSubmission(@NotNull Message message) {
        Matcher m = SUBMISSION_REGEX.matcher(message.getContentRaw());
        if (!m.matches())
            return Result.parseFailure("couldn't parse request");

        return spaceChem.parsePuzzle(m.group("puzzle")).flatMap(puzzle -> {
            ScScore score = ScScore.parseBPScore(m.group("score"));
            if (score == null)
                return Result.parseFailure("couldn't parse score");
            // we also archive the score here
            ScSolution solution = new ScSolution(puzzle, score);
            archive.archive(solution);
            ScRecord record = new ScRecord(score, m.group("author"), m.group("link"), m.group("oldRNG") != null);
            return Result.success(new Pair<>(puzzle, record));
        });
    }*/

}
