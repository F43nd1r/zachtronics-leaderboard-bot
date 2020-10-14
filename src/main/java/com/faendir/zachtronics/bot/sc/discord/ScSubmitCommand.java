package com.faendir.zachtronics.bot.sc.discord;

import com.faendir.zachtronics.bot.generic.discord.AbstractSubmitCommand;
import com.faendir.zachtronics.bot.model.Leaderboard;
import com.faendir.zachtronics.bot.sc.model.*;
import com.faendir.zachtronics.bot.utils.Result;
import kotlin.Pair;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ScSubmitCommand extends AbstractSubmitCommand<ScCategory, ScScore, ScPuzzle, ScRecord> {
    private final SpaceChem spaceChem;

    public ScSubmitCommand(SpaceChem spaceChem, List<Leaderboard<ScCategory, ScScore, ScPuzzle, ScRecord>> leaderboards) {
        super(leaderboards);
        this.spaceChem = spaceChem;
    }

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

        return spaceChem.parsePuzzle(m.group("puzzle")).flatMap(puzzle -> {
            ScScore score = ScScore.parseBPScore(m.group("score"));
            if (score == null)
                return Result.failure("Couldn't parse score");
            ScRecord record = new ScRecord(score, m.group("author"), m.group("link"), m.group("oldRNG") != null);
            return Result.success(new Pair<>(puzzle, record));
        });
    }

    @NotNull
    @Override
    public String getHelpText() {
        return "<puzzle> (<cycles/reactors/symbols[/BP]>) by <author> <youtube link>";
    }
}
