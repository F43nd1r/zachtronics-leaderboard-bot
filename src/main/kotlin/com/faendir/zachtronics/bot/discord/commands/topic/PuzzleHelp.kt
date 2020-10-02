package com.faendir.zachtronics.bot.discord.commands.topic

import com.faendir.zachtronics.bot.model.*
import net.dv8tion.jda.api.entities.Message
import org.springframework.stereotype.Component

@Component
class PuzzleHelp : HelpTopic {
    override val id: String = "puzzle"

    override fun <C : Category<S, P>, S : Score, P : Puzzle, R : Record<S>> getHelpText(game: Game<C, S, P, R>, message: Message): String {
        return """
            |A Puzzle name can be given in a shortened or abbreviated manner, as long as it remains unique.
            |For example:
            | - `stab water` matches `Stabilized Water`
            | - `Face` matches `Face Powder`
            | - `PMO` matches `Precision Machine Oil`
        """.trimMargin()
    }
}