package com.faendir.zachtronics.bot.sc.discord;

import com.faendir.discord4j.command.annotation.ApplicationCommand;
import com.faendir.discord4j.command.annotation.Converter;
import com.faendir.zachtronics.bot.generic.discord.AbstractSubmitCommand;
import com.faendir.zachtronics.bot.generic.discord.LinkConverter;
import com.faendir.zachtronics.bot.model.Leaderboard;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.ScRecord;
import com.faendir.zachtronics.bot.sc.model.ScScore;
import discord4j.core.event.domain.interaction.SlashCommandEvent;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import kotlin.Pair;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ScSubmitCommand extends AbstractSubmitCommand<ScPuzzle, ScRecord> {
    @Getter
    private final boolean enabled = false; // purposefully hidden, used only as a component of submit-archive

    @Getter
    private final List<Leaderboard<?, ScPuzzle, ScRecord>> leaderboards;

    @NotNull
    @Override
    public Pair<ScPuzzle, ScRecord> parseSubmission(@NotNull SlashCommandEvent interaction) {
        Data data = ScSubmitCommand$DataParser.parse(interaction);
        ScRecord record = new ScRecord(data.score, data.author, data.video, false);
        return new Pair<>(data.puzzle, record);
    }

    @NotNull
    @Override
    public ApplicationCommandOptionData buildData() {
        return ScSubmitCommand$DataParser.buildData();
    }

    @ApplicationCommand(name = "", subCommand = true)
    @Value
    public static class Data {
        @NonNull ScPuzzle puzzle;
        @NotNull
        ScScore score;
        @NotNull
        String author;
        @NotNull
        String video;

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
