package com.faendir.zachtronics.bot.sc.discord;

import com.faendir.discord4j.command.annotation.OptionConverter;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.utils.UtilsKt;
import discord4j.core.event.domain.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class ScPuzzleConverter implements OptionConverter<ScPuzzle> {
    @NotNull
    @Override
    public ScPuzzle fromString(@NotNull SlashCommandEvent context, @NotNull String s) {
        return parsePuzzle(s);
    }

    @NotNull
    public static ScPuzzle parsePuzzle(@NotNull String name) {
        return Arrays.stream(ScPuzzle.values())
                     .filter(p -> p.getDisplayName().equalsIgnoreCase(name))
                     .findFirst()
                     .orElseGet(() -> UtilsKt.getSingleMatchingPuzzle(ScPuzzle.values(), name));
    }
}
