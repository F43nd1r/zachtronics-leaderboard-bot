package com.faendir.om.discord.commands

import com.faendir.om.discord.leaderboards.GetResult
import com.faendir.om.discord.leaderboards.Leaderboard
import com.faendir.om.discord.puzzle.Puzzle
import com.faendir.om.discord.utils.reply
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import org.springframework.stereotype.Component

@Component
class ShowCommand(private val leaderboards: List<Leaderboard>) : Command {
    override val regex = Regex("!show\\s+(?<category>\\S+)\\s+(?<puzzle>.+)")
    override val name: String = "show"
    override val helpText: String = "!show <category> <puzzle>"

    override fun handleMessage(author: User, channel: TextChannel, message: Message, command: MatchResult) {
        val categoryString = command.groups["category"]!!.value
        val (leaderboard, category) = leaderboards.flatMap { leaderboard -> leaderboard.supportedCategories.map { leaderboard to it } }
            .find { pair -> pair.second.displayName.equals(categoryString, ignoreCase = true) } ?: return Unit.also {
            channel.reply(author, "sorry, I could not find the category \"$categoryString\"")
        }
        val puzzleName = command.groups["puzzle"]!!.value
        val puzzles = Puzzle.findByName(puzzleName)

        channel.reply(author, when (puzzles.size) {
            1 -> {
                val puzzle = puzzles.first()
                if(!category.supportedTypes.contains(puzzle.type)) {
                    "sorry, the category ${category.displayName} does not support puzzles of type ${puzzle.type.name.toLowerCase()}."
                } else if(!category.supportedGroups.contains(puzzle.group)){
                    "sorry, the category ${category.displayName} does not support puzzles in ${puzzle.group.name.replace("_", " ").toLowerCase().capitalize()}."
                } else {
                    when (val getResult = leaderboard.get(puzzle, category)) {
                        is GetResult.Success -> "here you go: ${puzzle.displayName} ${category.displayName} ${
                            getResult.score.reorderToStandard().toString("/")
                        } ${getResult.link}"
                        is GetResult.NoScore -> "sorry, there is no score for ${puzzle.displayName} ${category.displayName}."
                    }
                }
            }
            0 -> "sorry, I did not recognize the puzzle \"$puzzleName\"."
            else -> "sorry, your request for \"$puzzleName\" was not accurate enough. " +
                    if (puzzles.size <= 5) "Use one of:\n${
                        puzzles.joinToString("\n") { it.displayName }
                    }" else "${puzzles.size} matches."
        })
    }
}