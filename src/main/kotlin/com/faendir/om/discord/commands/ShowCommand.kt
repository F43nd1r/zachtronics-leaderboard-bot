package com.faendir.om.discord.commands

import com.faendir.om.discord.leaderboards.Leaderboard
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import org.springframework.stereotype.Component

@Component
class ShowCommand(private val leaderboards: List<Leaderboard>) : Command {
    override val regex = Regex("!show\\s+(?<category>\\S+)\\s+(?<puzzle>.+)")
    override val name: String = "show"
    override val helpText: String = "!show <category> <puzzle>"

    override fun handleMessage(author: User, channel: TextChannel, message: Message, command: MatchResult): String {
        val categoryString = command.groups["category"]!!.value
        val (leaderboard, category) = leaderboards.flatMap { leaderboard -> leaderboard.supportedCategories.map { leaderboard to it } }
            .find { pair -> pair.second.displayName.equals(categoryString, ignoreCase = true) } ?: return "sorry, I could not find the category \"$categoryString\""
        return findPuzzle(command.groups["puzzle"]!!.value) { puzzle ->
            if (!category.supportedTypes.contains(puzzle.type)) {
                "sorry, the category ${category.displayName} does not support puzzles of type ${puzzle.type.name.toLowerCase()}."
            } else if (!category.supportedGroups.contains(puzzle.group)) {
                "sorry, the category ${category.displayName} does not support puzzles in ${
                    puzzle.group.name.replace(
                        "_",
                        " "
                    ).toLowerCase().capitalize()
                }."
            } else {
                leaderboard.get(puzzle, category)?.let {
                    "here you go: ${puzzle.displayName} ${category.displayName} ${
                        it.score.reorderToStandard().toString("/")
                    } ${it.link}"
                } ?: "sorry, there is no score for ${puzzle.displayName} ${category.displayName}."
            } }
    }
}