package com.faendir.zachtronics.bot.om.discord

import com.faendir.discord4j.command.annotation.OptionConverter
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import discord4j.core.event.domain.interaction.SlashCommandEvent

class PuzzleConverter : OptionConverter<OmPuzzle> {
    override fun fromString(context: SlashCommandEvent, string: String): OmPuzzle =  OmPuzzle.parse(string)
}