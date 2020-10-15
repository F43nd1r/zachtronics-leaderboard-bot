package com.faendir.zachtronics.bot.generic.discord

import com.faendir.zachtronics.bot.model.*
import com.faendir.zachtronics.bot.utils.Result
import com.faendir.zachtronics.bot.utils.Result.Companion.parseFailure
import com.faendir.zachtronics.bot.utils.Result.Companion.success
import com.faendir.zachtronics.bot.utils.and
import com.faendir.zachtronics.bot.utils.match
import com.faendir.zachtronics.bot.utils.message
import net.dv8tion.jda.api.entities.Message
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class ShowCommand<C : Category<S, P>, S : Score, P : Puzzle, R : Record<S>>(@Lazy private val game: Game<C, S, P, R>,
                                                                            private val leaderboards: List<Leaderboard<C, S, P, R>>) : Command {
    val regex = Regex("!show\\s+(?<category>\\S+)\\s+(?<puzzle>.+)")
    val altRegex = Regex("!show\\s+(?<puzzle>.+)\\s+(?<category>\\S+)")
    override val name: String = "show"
    override val helpText: String = "<category> <puzzle>"
    override val isReadOnly: Boolean = true

    override fun handleMessage(message: Message): String {
        return message.match(regex).flatMap { game.parseCommand(it) }.onFailureTry {
            //try permuted arguments on failure
            message.match(altRegex).flatMap { game.parseCommand(it) }
        }.message
    }

    private fun Game<C, S, P, R>.parseCommand(command: MatchResult): Result<String> {
        return parseCategoryCandidates(command.groups["category"]!!.value).and { parsePuzzle(command.groups["puzzle"]!!.value) }
            .flatMap { (categories, puzzle) -> getSingleCategory(categories, puzzle) }
            .flatMap { (category, puzzle) -> findRecord(puzzle, category) }
    }

    private fun findRecord(puzzle: P, category: C): Result<String> {
        return leaderboards.asSequence().mapNotNull { it.get(puzzle, category) }.firstOrNull()?.let {
            success("here you go: ${puzzle.displayName} ${category.displayName} ${it.toDisplayString()}")
        } ?: Result.failure("sorry, there is no score for ${puzzle.displayName} ${category.displayName}.")
    }

    private fun getSingleCategory(categories: List<C>, puzzle: P): Result<Pair<C, P>> {
        return categories.find { it.supportsPuzzle(puzzle) }?.let { success(it to puzzle) }
            ?: parseFailure("the category \"${categories.first().displayName}\" does not support the puzzle ${puzzle.displayName}.", "choose another category")
    }

    private fun Game<C, S, P, R>.parseCategoryCandidates(name: String): Result<List<C>> {
        return parseCategory(name).takeIf { it.isNotEmpty() }?.let { success(it) } ?: parseFailure("I could not find the category \"$name\".")
    }
}