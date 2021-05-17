package com.faendir.zachtronics.bot.sc.discord;

import com.faendir.discord4j.command.annotation.ApplicationCommand;
import com.faendir.zachtronics.bot.generic.discord.AbstractArchiveCommand;
import com.faendir.zachtronics.bot.sc.archive.ScArchive;
import com.faendir.zachtronics.bot.sc.model.ScScore;
import com.faendir.zachtronics.bot.sc.model.ScSolution;
import com.faendir.zachtronics.bot.sc.model.SpaceChem;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.regex.Pattern;

@RequiredArgsConstructor
//@Component
public class ScArchiveCommand extends AbstractArchiveCommand<ScSolution> {
    private final SpaceChem spaceChem;
    @Getter
    private final ScArchive archive;

    private static final Pattern SOLUTION_REGEX = Pattern.compile(
            "!archive\\s+" +
            "(?<puzzle>.+?)" +
            "(?:\\s+\\((?<score>" + ScScore.REGEX_BP_SCORE + ")\\))?\\s*",
            Pattern.CASE_INSENSITIVE);

    @NotNull
    @Override
    public Mono<ScSolution> parseSolution(@NotNull List<? extends ApplicationCommandInteractionOption> options, @NotNull User user) {
        return null;
    }

    @NotNull
    @Override
    public ApplicationCommandOptionData buildData() {
        return ScArchiveCommand$ScArchiveCommandDataParser.buildData();
    }

    /*@NotNull
    @Override
    public Result<ScSolution> parseSolution(@NotNull Message message) {
        Matcher m = SOLUTION_REGEX.matcher(message.getContentRaw());
        if (!m.matches())
            return Result.parseFailure("couldn't parse request");

        return spaceChem.parsePuzzle(m.group("puzzle")).flatMap(puzzle -> {
            ScScore score = null;
            ScSolution solution;

            if (m.group("score") != null) {
                score = ScScore.parseBPScore(m.group("score"));
                if (score == null)
                    return Result.parseFailure("couldn't parse score");
            }

            if (message.getAttachments().size() == 1) {
                try (InputStream is = message.getAttachments().get(0).retrieveInputStream().get()) {
                    String content = new String(is.readAllBytes());
                    solution = new ScSolution(puzzle, score, content);
                } catch (IOException | InterruptedException | ExecutionException e) {
                    return Result.failure("Discord said the attachment existed but we couldn't read it");
                } catch (IllegalArgumentException e) {
                    return Result.failure("Could not parse a valid solution");
                }
            }
            else {
                if (score == null) {
                    return Result.failure("Need one (and only one) attachment or a score (or both), I found neither");
                }
                solution = new ScSolution(puzzle, score);
            }
            return Result.success(solution);
        });
    }*/

    @ApplicationCommand(name = "archive", subCommand = true)
    @Value
    public static class ScArchiveCommandData {
        @NonNull String puzzle;
        @NonNull String score;

        public ScArchiveCommandData(@NonNull String puzzle, @NonNull String score) {
            this.puzzle = puzzle;
            this.score = score;
        }
    }
}
