package com.faendir.zachtronics.bot.sc.discord;

import com.faendir.discord4j.command.annotation.ApplicationCommand;
import com.faendir.discord4j.command.annotation.Converter;
import com.faendir.zachtronics.bot.generic.discord.AbstractSubmitArchiveCommand;
import com.faendir.zachtronics.bot.generic.discord.LinkConverter;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.ScRecord;
import com.faendir.zachtronics.bot.sc.model.ScScore;
import com.faendir.zachtronics.bot.sc.model.ScSolution;
import discord4j.core.object.command.Interaction;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

@Component
@RequiredArgsConstructor
public class ScSubmitOnlyScoreCommand extends AbstractSubmitArchiveCommand<ScPuzzle, ScRecord, ScSolution> {
    @Getter
    private final String name = "submit-only-score";
    @Getter
    private final ScSubmitCommand submitCommand;
    @Getter
    private final ScArchiveCommand archiveCommand;

    @NotNull
    @Override
    public Mono<Tuple3<ScPuzzle, ScRecord, ScSolution>> parseToPRS(@NotNull Interaction interaction) {
        return ScSubmitOnlyScoreCommand$DataParser.parse(interaction).map(data -> {
            ScSolution solution = new ScSolution(data.puzzle, data.score);
            ScRecord record = new ScRecord(solution.getScore(), data.author, data.video, false);
            return Tuples.of(solution.getPuzzle(), record, solution);
        });
    }

    @NotNull
    @Override
    public ApplicationCommandOptionData buildData() {
        return ScSubmitOnlyScoreCommand$DataParser.buildData();
    }

    @ApplicationCommand(name = "submit-only-score", subCommand = true)
    @Value
    public static class Data {
        @NonNull ScPuzzle puzzle;
        @NotNull ScScore score;
        @NotNull String author;
        @NotNull String video;

        public Data(@Converter(ScPuzzleConverter.class) @NonNull ScPuzzle puzzle,
                    @Converter(ScBPScoreConverter.class) @NonNull ScScore score,
                    @NotNull String author, @NotNull @Converter(LinkConverter.class) String video) {
            this.puzzle = puzzle;
            this.score = score;
            this.video = video;
            this.author = author;
        }
    }
}
