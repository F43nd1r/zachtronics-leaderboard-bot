package com.faendir.om.discord.commands

import com.faendir.om.discord.leaderboards.GetResult
import com.faendir.om.discord.leaderboards.Leaderboard
import com.faendir.om.discord.leaderboards.PuzzleResult
import com.faendir.om.discord.utils.find
import com.faendir.om.discord.utils.reply
import com.faendir.om.discord.utils.toScoreString
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import org.springframework.stereotype.Component

@Component
class ShowCommand(private val leaderboards: List<Leaderboard<*>>) : Command {
    override val regex = Regex("!show\\s+(?<category>\\S+)\\s+(?<puzzle>.+)")
    override val name: String = "show"
    override val helpText: String = "!show <category> <puzzle>"

    override fun handleMessage(author: User, channel: TextChannel, message: Message, command: MatchResult) {
        val categoryString = command.groups["category"]!!.value
        val (leaderboard, category) = leaderboards.flatMap { leaderboard -> leaderboard.supportedCategories.map { leaderboard to it } }
            .find { pair -> pair.second.name.equals(categoryString, ignoreCase = true) } ?: return Unit.also {
            channel.reply(author, "sorry, I could not find the category \"$categoryString\"")
        }
        val puzzle = command.groups["puzzle"]!!.value

        fun <T> handle(leaderboard: Leaderboard<T>) {
            channel.reply(
                author, when (val puzzleResult = leaderboard.findPuzzle(puzzle)) {
                    is PuzzleResult.Success -> {
                        when (val getResult = leaderboard.get(puzzleResult.puzzle, category)) {
                            is GetResult.Success -> "here you go: ${getResult.puzzle} ${category.name} ${
                                getResult.score.toScoreString("/")
                            } ${getResult.link}"
                            is GetResult.NoScore -> "sorry, there is no score for ${getResult.puzzle} ${category.name}."
                            is GetResult.ScoreNotRecorded -> "sorry, ${getResult.reason} (${getResult.puzzle})."
                        }
                    }
                    is PuzzleResult.NotFound -> "sorry, I did not recognize the puzzle \"$puzzle\"."
                    is PuzzleResult.Ambiguous -> "sorry, your request for \"$puzzle\" was not accurate enough. " +
                            if (puzzleResult.puzzles.size <= 5) "Use one of:\n${
                                puzzleResult.puzzles.joinToString("\n")
                            }" else "${puzzleResult.puzzles.size} matches."
                }
            )
        }
        handle(leaderboard)
    }
}