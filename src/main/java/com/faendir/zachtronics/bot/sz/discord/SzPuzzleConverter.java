package com.faendir.zachtronics.bot.sz.discord;

import com.faendir.discord4j.command.annotation.OptionConverter;
import com.faendir.zachtronics.bot.sz.model.SzPuzzle;
import com.faendir.zachtronics.bot.utils.UtilsKt;
import discord4j.core.event.domain.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;

public class SzPuzzleConverter implements OptionConverter<SzPuzzle> {
    @NotNull
    @Override
    public SzPuzzle fromString(@NotNull SlashCommandEvent context, @NotNull String s) {
        return parsePuzzle(s);
    }

    @NotNull
    public static SzPuzzle parsePuzzle(@NotNull String name) {
        return UtilsKt.getSingleMatchingPuzzle(SzPuzzle.values(), name);
    }
}
