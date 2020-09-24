package com.faendir.zachtronics.bot.discord.commands

import com.faendir.zachtronics.bot.model.*
import com.faendir.zachtronics.bot.utils.Result
import com.faendir.zachtronics.bot.utils.message
import net.dv8tion.jda.api.entities.Message
import org.springframework.stereotype.Component

@Component
class ShowCommand : Command {
    val regex = Regex("!show\\s+(?<category>\\S+)\\s+(?<puzzle>.+)")
    override val name: String = "show"
    override fun helpText(game: Game<*, *, *, *>): String = "!show <category> <puzzle>"

    override fun <C : Category<C, S, P>, S : Score, P : Puzzle, R : Record<S>> handleMessage(game: Game<C, S, P, R>, message: Message): String {
        return message.match(regex).flatMap { command ->
            val categoryString = command.groups["category"]!!.value
            val categories = game.parseCategory(categoryString).ifEmpty {
                return@flatMap Result.Failure("sorry, could not find the category \"$categoryString\"")
            }
            game.parsePuzzle(command.groups["puzzle"]!!.value).map puzzle@{ puzzle ->
                val category = categories.find { it.supportsPuzzle(puzzle) }
                    ?: return@puzzle "sorry, the category \"${categories.first().displayName}\" does not support the puzzle ${puzzle.displayName}."
                for (leaderboard in game.leaderboards) {
                    val record = leaderboard.get(puzzle, category)
                    if (record != null) return@puzzle "here you go: ${puzzle.displayName} ${record.toDisplayString()}"
                }
                return@puzzle "sorry, there is no score for ${puzzle.displayName} ${category.displayName}."
            }
        }.message
    }
}