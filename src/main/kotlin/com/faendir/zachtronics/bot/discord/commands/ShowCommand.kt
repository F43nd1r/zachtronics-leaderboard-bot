package com.faendir.zachtronics.bot.discord.commands

import com.faendir.zachtronics.bot.leaderboards.Leaderboard
import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.model.Game
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Score
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import org.springframework.stereotype.Component

@Component
class ShowCommand : Command {
    override val regex = Regex("!show\\s+(?<category>\\S+)\\s+(?<puzzle>.+)")
    override val name: String = "show"
    override val helpText: String = "!show <category> <puzzle>"

    override fun <S : Score<S, *>, P : Puzzle> handleMessage(game: Game<S, P>, leaderboards: List<Leaderboard<*, S, P>>, author: User, channel: TextChannel, message: Message,
                                                             command: MatchResult): String {
        val categoryString = command.groups["category"]!!.value
        return findPuzzle(game, command.groups["puzzle"]!!.value) { puzzle ->
            val leaderBoardCategoryPair = leaderboards.flatMap { it.pairWithEachCategory() }
                .filter { pair -> pair.category.displayName.equals(categoryString, ignoreCase = true) }
                .ifEmpty { return@findPuzzle "sorry, I could not find the category \"$categoryString\"" }
                .filter { it.category.supportedTypes.contains(puzzle.type) }
                .ifEmpty { return@findPuzzle "sorry, the category \"${categoryString.toLowerCase()}\" does not support puzzles of type ${puzzle.type.displayName}." }
                .find { it.category.supportedGroups.contains(puzzle.group) }
                ?: return@findPuzzle "sorry, the category \"${categoryString.toLowerCase()}\" does not support puzzles in ${puzzle.group.displayName}."
            return@findPuzzle handleMessageImpl(puzzle, leaderBoardCategoryPair)
        }
    }

    private fun <C : Category<C, *, *>, P : Puzzle> handleMessageImpl(puzzle: P, leaderBoardCategoryPair: LeaderBoardCategoryPair<C, P>): String {
        val (leaderboard, category) = leaderBoardCategoryPair
        return leaderboard.get(puzzle, category)?.let {
            "here you go: ${puzzle.displayName} ${category.displayName} ${
                it.score.reorderToStandard().toString("/")
            } ${it.link}"
        } ?: "sorry, there is no score for ${puzzle.displayName} ${category.displayName}."
    }
}

private data class LeaderBoardCategoryPair<CATEGORY : Category<CATEGORY, *, *>, PUZZLE : Puzzle>(val leaderboard: Leaderboard<CATEGORY, *, PUZZLE>, val category: CATEGORY)

private fun <CATEGORY : Category<CATEGORY, *, *>, PUZZLE : Puzzle> Leaderboard<CATEGORY, *, PUZZLE>.pairWithEachCategory() =
    supportedCategories.map { LeaderBoardCategoryPair(this, it) }