package com.faendir.zachtronics.bot.discord.commands

import com.faendir.zachtronics.bot.model.*
import net.dv8tion.jda.api.entities.Message

interface Command {

    val name: String

    fun helpText(game: Game<*, *, *, *>) : String

    val isReadOnly : Boolean

    fun <C : Category<C, S, P>, S : Score, P : Puzzle, R : Record<S>> handleMessage(game: Game<C, S, P, R>, message: Message): String
}