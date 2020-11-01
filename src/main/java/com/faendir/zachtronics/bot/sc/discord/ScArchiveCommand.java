package com.faendir.zachtronics.bot.sc.discord;

import com.faendir.zachtronics.bot.generic.discord.AbstractArchiveCommand;
import com.faendir.zachtronics.bot.sc.archive.ScArchive;
import com.faendir.zachtronics.bot.sc.model.ScScore;
import com.faendir.zachtronics.bot.sc.model.ScSolution;
import com.faendir.zachtronics.bot.sc.model.SpaceChem;
import com.faendir.zachtronics.bot.utils.Result;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Component
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
    public Result<ScSolution> parseSolution(@NotNull Message message) {
        Matcher m = SOLUTION_REGEX.matcher(message.getContentRaw());
        if (!m.matches())
            return Result.parseFailure("Couldn't parse request");

        return spaceChem.parsePuzzle(m.group("puzzle")).flatMap(puzzle -> {
            ScScore score = null;
            ScSolution solution;

            if (m.group("score") != null) {
                score = ScScore.parseBPScore(m.group("score"));
                if (score == null)
                    return Result.parseFailure("Couldn't parse score");
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
    }

    @NotNull
    @Override
    public String getHelpText() {
        return "<puzzle> [(<cycles/reactors/symbols[/BP]>) - or - attach export to message]";
    }
}
