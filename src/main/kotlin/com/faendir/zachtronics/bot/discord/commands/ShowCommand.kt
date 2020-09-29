package com.faendir.zachtronics.bot.discord.commands

import com.faendir.zachtronics.bot.model.*
import com.faendir.zachtronics.bot.utils.Result
import com.faendir.zachtronics.bot.utils.Result.Companion.parseFailure
import com.faendir.zachtronics.bot.utils.match
import com.faendir.zachtronics.bot.utils.message
import net.dv8tion.jda.api.entities.Message
import org.springframework.stereotype.Component

@Component
class ShowCommand : Command {
    val regex = Regex("!show\\s+(?<category>\\S+)\\s+(?<puzzle>.+)")
    override val name: String = "show"
    override fun helpText(game: Game<*, *, *, *>): String = "!show <category> <puzzle>"
    override val isReadOnly: Boolean = true

    override fun <C : Category<C, S, P>, S : Score, P : Puzzle, R : Record<S>> handleMessage(game: Game<C, S, P, R>, message: Message): String {
        return message.match(regex).flatMap { command ->
            val categoryString = command.groups["category"]!!.value
            val categories = game.parseCategory(categoryString).ifEmpty {
                return@flatMap parseFailure("I could not find the category \"$categoryString\".")
            }
            game.parsePuzzle(command.groups["puzzle"]!!.value).flatMap puzzle@{ puzzle ->
                val category = categories.find { it.supportsPuzzle(puzzle) }
                    ?: return@puzzle parseFailure("the category \"${categories.first().displayName}\" does not support the puzzle ${
                        puzzle.displayName
                    }.", "choose another category")
                for (leaderboard in game.leaderboards) {
                    val record = leaderboard.get(puzzle, category)
                    if (record != null) return@puzzle Result.success("here you go: ${puzzle.displayName} ${category.displayName} ${
                        record.toDisplayString()
                    }")
                }
                return@puzzle Result.failure("sorry, there is no score for ${puzzle.displayName} ${category.displayName}.")
            }
        }.message
    }
}