package com.faendir.zachtronics.bot.sc.discord;

import com.faendir.discord4j.command.annotation.ApplicationCommand;
import com.faendir.discord4j.command.annotation.Converter;
import com.faendir.discord4j.command.annotation.Description;
import com.faendir.zachtronics.bot.discord.LinkConverter;
import com.faendir.zachtronics.bot.discord.command.AbstractSubmitArchiveCommand;
import com.faendir.zachtronics.bot.sc.ScQualifier;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.ScRecord;
import com.faendir.zachtronics.bot.sc.model.ScScore;
import com.faendir.zachtronics.bot.sc.model.ScSolution;
import discord4j.core.event.domain.interaction.SlashCommandEvent;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import kotlin.Triple;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ScQualifier
public class ScSubmitArchiveCommand extends AbstractSubmitArchiveCommand<ScPuzzle, ScRecord, ScSolution> implements ScSecured {
    @Getter
    private final ScSubmitCommand submitCommand;
    @Getter
    private final ScArchiveCommand archiveCommand;

    @NotNull
    @Override
    public Triple<ScPuzzle, ScRecord, ScSolution> parseToPRS(@NotNull SlashCommandEvent event) {
        Data data = ScSubmitArchiveCommand$DataParser.parse(event);
        ScSolution solution = ScSolution.fromExportLink(data.export, data.puzzle).get(0);
        ScRecord record = new ScRecord(solution.getScore(), data.author, data.video, false);
        return new Triple<>(solution.getPuzzle(), record, solution);
    }

    @NotNull
    @Override
    public ApplicationCommandOptionData buildData() {
        return ScSubmitArchiveCommand$DataParser.buildData();
    }

    @ApplicationCommand(name = "submit-archive", description = "Submit and archive a solution", subCommand = true)
    @Value
    public static class Data {
        @NotNull String video;
        @NotNull String export;
        @NotNull String author;
        ScPuzzle puzzle;

        public Data(@Description("Link to your video of the solution, can be `m1` to scrape it from your last message")
                    @NotNull @Converter(LinkConverter.class) String video,
                    @Description("Link to your export file, can be `m1` to scrape it from your last message\n" +
                                 "If the solution name starts with `/B`, `/P` or `/BP`, the corresponding flags are set in the score")
                    @NotNull @Converter(LinkConverter.class) String export,
                    @Description("Name to appear on the Reddit leaderboard")
                    @NotNull String author,
                    @Converter(ScPuzzleConverter.class) ScPuzzle puzzle) {
            this.video = video;
            this.export = export;
            this.author = author;
            this.puzzle = puzzle;
        }
    }
}
