package com.faendir.zachtronics.bot.sc.discord;

import com.faendir.discord4j.command.annotation.ApplicationCommand;
import com.faendir.discord4j.command.annotation.Converter;
import com.faendir.zachtronics.bot.generic.discord.AbstractSubmitArchiveCommand;
import com.faendir.zachtronics.bot.generic.discord.LinkConverter;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.ScRecord;
import com.faendir.zachtronics.bot.sc.model.ScScore;
import com.faendir.zachtronics.bot.sc.model.ScSolution;
import discord4j.core.event.domain.interaction.SlashCommandEvent;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

@Component
@RequiredArgsConstructor
public class ScSubmitArchiveCommand extends AbstractSubmitArchiveCommand<ScPuzzle, ScRecord, ScSolution> {
    @Getter
    private final ScSubmitCommand submitCommand;
    @Getter
    private final ScArchiveCommand archiveCommand;

    @NotNull
    @Override
    public Mono<Tuple3<ScPuzzle, ScRecord, ScSolution>> parseToPRS(@NotNull SlashCommandEvent event) {
        return ScSubmitArchiveCommand$DataParser.parse(event).map(data -> {
            ScSolution solution = ScSolution.makeSolution(data.puzzle, data.score, data.export);
            ScRecord record = new ScRecord(solution.getScore(), data.author, data.video, false);
            return Tuples.of(solution.getPuzzle(), record, solution);
        });
    }

    @NotNull
    @Override
    public ApplicationCommandOptionData buildData() {
        return ScSubmitArchiveCommand$DataParser.buildData();
    }

    @ApplicationCommand(name = "submit-archive", subCommand = true)
    @Value
    public static class Data {
        @NotNull String video;
        @NotNull String export;
        @NotNull String author;
        ScPuzzle puzzle;
        ScScore score;

        public Data(@NotNull @Converter(LinkConverter.class) String video,
                    @NotNull @Converter(LinkConverter.class) String export, @NotNull String author,
                    @Converter(ScPuzzleConverter.class) ScPuzzle puzzle,
                    @Converter(ScBPScoreConverter.class) ScScore score) {
            this.video = video;
            this.export = export;
            this.author = author;
            this.puzzle = puzzle;
            this.score = score;
        }
    }
}
