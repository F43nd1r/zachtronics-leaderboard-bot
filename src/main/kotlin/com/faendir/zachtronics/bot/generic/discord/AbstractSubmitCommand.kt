package com.faendir.zachtronics.bot.generic.discord

import com.faendir.zachtronics.bot.model.*
import com.faendir.zachtronics.bot.utils.Result
import com.faendir.zachtronics.bot.utils.findInstance
import com.faendir.zachtronics.bot.utils.ifNotEmpty
import com.faendir.zachtronics.bot.utils.message
import net.dv8tion.jda.api.entities.Message

abstract class AbstractSubmitCommand<C : Category<S, P>, S : Score, P : Puzzle, R : Record<S>>(private val leaderboards: List<Leaderboard<C, S, P, R>>) :
    Command {
    override val name: String = "submit"
    override val isReadOnly: Boolean = false

    override fun handleMessage(message: Message): String {
        return parseSubmission(message).map { (puzzle, record) ->
            val results = leaderboards.map { it.update(puzzle, record) }
            results.filterIsInstance<UpdateResult.Success<C, S>>().ifNotEmpty { successes ->
                return@map "thanks, the site will be updated shortly with ${puzzle.displayName} ${
                    successes.flatMap { it.oldScores.keys }.map { it.displayName }
                } ${record.score.toDisplayString()} (previously ${
                    successes.flatMap { it.oldScores.entries }.joinToString { "`${it.key.displayName} ${it.value?.toDisplayString() ?: "none"}`" }
                })."
            }
            results.findInstance<UpdateResult.ParetoUpdate<C, S>> {
                return@map "thanks, your submission for ${puzzle.displayName} (${record.score.toDisplayString()}) was included in the pareto frontier."
            }
            results.filterIsInstance<UpdateResult.BetterExists<C, S>>().ifNotEmpty { betterExists ->
                return@map "sorry, your submission did not beat any of the existing scores for ${puzzle.displayName} ${
                    betterExists.flatMap { it.scores.entries }.joinToString { "`${it.key.displayName} ${it.value.toDisplayString()}`" }
                }"
            }
            results.findInstance<UpdateResult.NotSupported<C, S>> { return@map "sorry, no leaderboard supporting your submission found" }
            "sorry, something went wrong"
        }.message
    }

    abstract fun parseSubmission(message: Message): Result<Pair<P, R>>
}