package com.faendir.zachtronics.bot.sc.discord;

import com.faendir.discord4j.command.annotation.ApplicationCommand;
import com.faendir.discord4j.command.annotation.Converter;
import com.faendir.zachtronics.bot.generic.discord.AbstractArchiveCommand;
import com.faendir.zachtronics.bot.generic.discord.LinkConverter;
import com.faendir.zachtronics.bot.sc.archive.ScArchive;
import com.faendir.zachtronics.bot.sc.model.ScScore;
import com.faendir.zachtronics.bot.sc.model.ScSolution;
import discord4j.core.event.domain.interaction.SlashCommandEvent;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class ScArchiveCommand extends AbstractArchiveCommand<ScSolution> {
    @Getter
    private final ScArchive archive;

    @NotNull
    @Override
    public List<ScSolution> parseSolutions(@NotNull SlashCommandEvent interaction) {
        Data data = ScArchiveCommand$DataParser.parse(interaction);
        return ScSolution.fromExportLink(data.export, data.score);
    }

    @NotNull
    @Override
    public ApplicationCommandOptionData buildData() {
        return ScArchiveCommand$DataParser.buildData();
    }

    @ApplicationCommand(name = "archive", subCommand = true)
    @Value
    public static class Data {
        @NotNull String export;
        ScScore score;

        public Data(@NotNull @Converter(LinkConverter.class) String export,
                    @Converter(ScBPScoreConverter.class) ScScore score) {
            this.export = export;
            this.score = score;
        }
    }
}
