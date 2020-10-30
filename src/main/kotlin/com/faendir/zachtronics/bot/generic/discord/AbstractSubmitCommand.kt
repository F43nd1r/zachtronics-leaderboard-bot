package com.faendir.zachtronics.bot.generic.discord

import com.faendir.zachtronics.bot.model.*
import com.faendir.zachtronics.bot.utils.Result
import com.faendir.zachtronics.bot.utils.findInstance
import com.faendir.zachtronics.bot.utils.ifNotEmpty
import com.faendir.zachtronics.bot.utils.message
import net.dv8tion.jda.api.entities.Message

abstract class AbstractSubmitCommand<P : Puzzle, R : Record<*>> :
    Command {
    override val name: String = "submit"
    override val isReadOnly: Boolean = false
    protected abstract val leaderboards: List<Leaderboard<*, *, P, R>>

    override fun handleMessage(message: Message): String {
        return parseSubmission(message).map { (puzzle, record) ->
            val results = leaderboards.map { it.update(puzzle, record) }
            results.filterIsInstance<UpdateResult.Success>().ifNotEmpty { successes ->
                return@map "thanks, the site will be updated shortly with ${puzzle.displayName} ${
                    successes.flatMap { it.oldScores.keys }.map { it.displayName }
                } ${record.score.toDisplayString()} (previously ${
                    successes.flatMap { it.oldScores.entries }.joinToString { "`${it.key.displayName} ${it.value?.toDisplayString() ?: "none"}`" }
                })."
            }
            results.findInstance<UpdateResult.ParetoUpdate> {
                return@map "thanks, your submission for ${puzzle.displayName} (${record.score.toDisplayString()}) was included in the pareto frontier."
            }
            results.filterIsInstance<UpdateResult.BetterExists>().ifNotEmpty { betterExists ->
                return@map "sorry, your submission did not beat any of the existing scores for ${puzzle.displayName} ${
                    betterExists.flatMap { it.scores.entries }.joinToString { "`${it.key.displayName} ${it.value.toDisplayString()}`" }
                }"
            }
            results.findInstance<UpdateResult.NotSupported> { return@map "sorry, no leaderboard supporting your submission found" }
            "sorry, something went wrong"
        }.message
    }

    abstract fun parseSubmission(message: Message): Result<Pair<P, R>>
}