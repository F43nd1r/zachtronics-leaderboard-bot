package com.faendir.zachtronics.bot.om.discord

import com.faendir.discord4j.command.annotation.OptionConverter
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import discord4j.core.`object`.command.Interaction
import reactor.core.publisher.Mono
import java.util.*

class PuzzleConverter : OptionConverter<OmPuzzle> {
    override fun fromString(context: Interaction, string: String?): Mono<Optional<OmPuzzle>> =
        Mono.fromCallable { Optional.ofNullable(string?.let { OmPuzzle.parse(it) }) }
}