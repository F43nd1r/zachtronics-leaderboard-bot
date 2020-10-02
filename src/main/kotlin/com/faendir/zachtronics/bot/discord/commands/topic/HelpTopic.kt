package com.faendir.zachtronics.bot.discord.commands.topic

import com.faendir.zachtronics.bot.model.*
import net.dv8tion.jda.api.entities.Message

interface HelpTopic {
    val id: String
    fun <C : Category<S, P>, S : Score, P : Puzzle, R : Record<S>> getHelpText(game: Game<C, S, P, R>, message: Message): String
}