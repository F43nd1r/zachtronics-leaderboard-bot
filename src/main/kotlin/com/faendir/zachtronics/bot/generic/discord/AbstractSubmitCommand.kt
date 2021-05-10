package com.faendir.zachtronics.bot.generic.discord

import com.faendir.zachtronics.bot.model.*
import com.faendir.zachtronics.bot.utils.Result
import com.faendir.zachtronics.bot.utils.findInstance
import com.faendir.zachtronics.bot.utils.ifNotEmpty
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message

abstract class AbstractSubmitCommand<P : Puzzle, R : Record<*>> : Command {
    override val name: String = "submit"
    override val isReadOnly: Boolean = false
    protected abstract val leaderboards: List<Leaderboard<*, *, P, R>>

    override fun handleMessageEmbed(message: Message): EmbedBuilder {
        return parseSubmission(message).map { (puzzle, record) ->
            val results = leaderboards.map { it.update(puzzle, record) }
            results.filterIsInstance<UpdateResult.Success>().ifNotEmpty { successes ->
                return@map EmbedBuilder().setTitle("Success: ${puzzle.displayName} ${successes.flatMap { it.oldScores.keys }.joinToString { it.displayName }}")
                    .setDescription("`${record.score.toDisplayString()}`")
                    .appendDescription("\npreviously:")
                    .apply {
                        successes.flatMap { it.oldScores.entries }.forEach { addField(it.key.displayName, "`${it.value?.toDisplayString() ?: "none"}`", true) }
                    }
            }
            results.findInstance<UpdateResult.ParetoUpdate> {
                return@map EmbedBuilder().setTitle("Pareto: ${puzzle.displayName}")
                    .setDescription("${record.score.toDisplayString()} was included in the pareto frontier.")
            }
            results.filterIsInstance<UpdateResult.BetterExists>().ifNotEmpty { betterExists ->
                return@map EmbedBuilder().setTitle("No Scores beaten by ${puzzle.displayName} ${record.score.toDisplayString()}")
                    .setDescription("Existing scores:")
                    .apply {
                        betterExists.flatMap { it.scores.entries }
                            .sortedBy<Map.Entry<Category<*, *>, Score>, Comparable<*>> { it.key as? Comparable<*> }
                            .forEach { addField(it.key.displayName, it.value.toDisplayString(), true) }
                    }
            }
            results.findInstance<UpdateResult.NotSupported> {
                return@map EmbedBuilder().setTitle("Failure").setDescription("No leaderboard supporting your submission found")
            }
            EmbedBuilder().setTitle("Failure").setDescription("sorry, something went wrong")
        }.onFailure { EmbedBuilder().setTitle("Failure").setDescription(it) }
    }

    abstract fun parseSubmission(message: Message): Result<Pair<P, R>>
}