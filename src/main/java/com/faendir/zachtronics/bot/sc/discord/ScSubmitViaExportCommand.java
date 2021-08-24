package com.faendir.zachtronics.bot.sc.discord;

import com.faendir.discord4j.command.annotation.ApplicationCommand;
import com.faendir.discord4j.command.annotation.Converter;
import com.faendir.zachtronics.bot.generic.discord.AbstractSubmitCommand;
import com.faendir.zachtronics.bot.generic.discord.LinkConverter;
import com.faendir.zachtronics.bot.model.Leaderboard;
import com.faendir.zachtronics.bot.sc.archive.ScArchive;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.ScRecord;
import com.faendir.zachtronics.bot.sc.model.ScScore;
import com.faendir.zachtronics.bot.sc.model.ScSolution;
import discord4j.core.object.command.Interaction;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ScSubmitViaExportCommand extends AbstractSubmitCommand<ScPuzzle, ScRecord> {
    @Getter
    private final String name = "submit-via-export";
    private final ScArchive archive;
    @Getter
    private final List<Leaderboard<?, ScPuzzle, ScRecord>> leaderboards;

    @NotNull
    @Override
    public Mono<Tuple2<ScPuzzle, ScRecord>> parseSubmission(@NotNull Interaction interaction) {
        return ScSubmitViaExportCommand$DataParser.parse(interaction).map(data -> {
            // we also archive the score here
            ScSolution solution = ScArchiveCommand.makeSolution(data.puzzle, data.score, data.export);
            archive.archive(solution).block();
            ScRecord record = new ScRecord(data.score, data.author, data.video, false);
            return Tuples.of(data.puzzle, record);
        });
    }

    @NotNull
    @Override
    public ApplicationCommandOptionData buildData() {
        return ScSubmitViaExportCommand$DataParser.buildData();
    }

    @ApplicationCommand(name = "submit-via-export", subCommand = true)
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
