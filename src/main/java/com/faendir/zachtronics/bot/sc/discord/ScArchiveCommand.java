package com.faendir.zachtronics.bot.sc.discord;

import com.faendir.discord4j.command.annotation.ApplicationCommand;
import com.faendir.discord4j.command.annotation.Converter;
import com.faendir.discord4j.command.annotation.Description;
import com.faendir.zachtronics.bot.discord.LinkConverter;
import com.faendir.zachtronics.bot.discord.command.AbstractArchiveCommand;
import com.faendir.zachtronics.bot.sc.ScQualifier;
import com.faendir.zachtronics.bot.sc.archive.ScArchive;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.ScSolution;
import discord4j.core.event.domain.interaction.SlashCommandEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
@ScQualifier
public class ScArchiveCommand extends AbstractArchiveCommand<ScSolution> {
    @Getter
    private final ScArchive archive;

    @NotNull
    @Override
    public List<ScSolution> parseSolutions(@NotNull SlashCommandEvent interaction) {
        Data data = ScArchiveCommand$DataParser.parse(interaction);
        return ScSolution.fromExportLink(data.export, data.puzzle);
    }

    @NotNull
    @Override
    public ApplicationCommandOptionData buildData() {
        return ScArchiveCommand$DataParser.buildData();
    }

    @ApplicationCommand(name = "archive", description = "Archive any number of solutions in an export file", subCommand = true)
    @Value
    public static class Data {
        @NotNull String export;
        ScPuzzle puzzle;

        public Data(@NotNull
                    @Description("Link to your export file, can be `m1` to scrape it from your last message\n" +
                                 "If the solution name starts with `/B`, `/P` or `/BP`, the corresponding flags are set in the score")
                    @Converter(LinkConverter.class) String export,
                    @Converter(ScPuzzleConverter.class) ScPuzzle puzzle) {
            this.export = export;
            this.puzzle = puzzle;
        }
    }

    @Override
    public boolean hasExecutionPermission(@NotNull User user) {
        if (user instanceof Member)
            return ((Member)user).getRoles().any(r -> r.getName().equals("trusted-leaderboard-poster")).block();
        else return false;
    }
}
