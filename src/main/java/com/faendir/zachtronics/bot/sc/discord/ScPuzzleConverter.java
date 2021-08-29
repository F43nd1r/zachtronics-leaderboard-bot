package com.faendir.zachtronics.bot.sc.discord;

import com.faendir.discord4j.command.annotation.OptionConverter;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.SpaceChem;
import discord4j.core.event.domain.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;

public class ScPuzzleConverter implements OptionConverter<ScPuzzle> {
    @NotNull
    @Override
    public ScPuzzle fromString(@NotNull SlashCommandEvent context, @NotNull String s) {
        return SpaceChem.parsePuzzle(s);
    }
}
