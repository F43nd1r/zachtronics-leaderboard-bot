package com.faendir.zachtronics.bot.sc.discord;

import com.faendir.discord4j.command.annotation.ApplicationCommand;
import com.faendir.discord4j.command.annotation.Converter;
import com.faendir.zachtronics.bot.generic.discord.AbstractListCommand;
import com.faendir.zachtronics.bot.model.Leaderboard;
import com.faendir.zachtronics.bot.sc.model.ScCategory;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.ScRecord;
import discord4j.core.event.domain.interaction.SlashCommandEvent;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import kotlin.Pair;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class ScListCommand extends AbstractListCommand<ScCategory, ScPuzzle, ScRecord> {
    @Getter
    private final List<Leaderboard<ScCategory, ScPuzzle, ScRecord>> leaderboards;

    @NotNull
    @Override
    public ApplicationCommandOptionData buildData() {
        return ScListCommand$DataParser.buildData();
    }

    @NotNull
    @Override
    public Pair<ScPuzzle, List<ScCategory>> findPuzzleAndCategories(@NotNull SlashCommandEvent interaction) {
        Data data = ScListCommand$DataParser.parse(interaction);
        return new Pair<>(data.puzzle, Arrays.stream(ScCategory.values())
                .filter(c -> c.supportsPuzzle(data.puzzle))
                .collect(Collectors.toList()));
    }

    @ApplicationCommand(name = "list", subCommand = true)
    @Value
    public static class Data {
        @NonNull ScPuzzle puzzle;

        public Data(@Converter(ScPuzzleConverter.class) @NonNull ScPuzzle puzzle) {
            this.puzzle = puzzle;
        }
    }
}
