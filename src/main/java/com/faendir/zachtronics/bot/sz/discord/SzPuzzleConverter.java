package com.faendir.zachtronics.bot.sz.discord;

import com.faendir.discord4j.command.annotation.OptionConverter;
import com.faendir.zachtronics.bot.sz.model.SzPuzzle;
import discord4j.core.event.domain.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;

public class SzPuzzleConverter implements OptionConverter<SzPuzzle> {
    @NotNull
    @Override
    public SzPuzzle fromString(@NotNull SlashCommandEvent context, @NotNull String s) {
        return SzPuzzle.parsePuzzle(s);
    }

}
