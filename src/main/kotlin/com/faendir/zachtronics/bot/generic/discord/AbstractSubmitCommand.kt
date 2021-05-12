package com.faendir.zachtronics.bot.generic.discord

import com.faendir.zachtronics.bot.model.*
import com.faendir.zachtronics.bot.utils.findInstance
import com.faendir.zachtronics.bot.utils.ifNotEmpty
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.entity.User
import discord4j.discordjson.json.WebhookExecuteRequest
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import reactor.util.function.Tuple2

abstract class AbstractSubmitCommand<P : Puzzle, R : Record<*>> : Command {
    override val name: String = "submit"
    override val isReadOnly: Boolean = false
    protected abstract val leaderboards: List<Leaderboard<*, *, P, R>>

    override fun handle(options: List<ApplicationCommandInteractionOption>, user: User): Mono<WebhookExecuteRequest> {
        return parseSubmission(options, user).flatMap { (puzzle, record) -> submitToLeaderboards(puzzle, record) }
    }

    fun submitToLeaderboards(puzzle: P, record: R) =
        leaderboards.toFlux().flatMap { it.update(puzzle, record) }.collectList().map { results: List<UpdateResult> ->
            results.filterIsInstance<UpdateResult.Success>().ifNotEmpty { successes ->
                return@map WebhookExecuteRequest.builder().content("""
                        |Success: *${puzzle.displayName}* **${successes.flatMap { it.oldScores.keys }.joinToString { it.displayName }}**
                        |`${record.score.toDisplayString()}`
                        |previously:
                        |${
                    successes.flatMap { it.oldScores.entries }
                        .joinToString("\n") { "**${it.key.displayName}**\n`${it.value?.toDisplayString() ?: "none"}`" }
                }
                        |""".trimMargin()
                )
                    .build()
            }
            results.findInstance<UpdateResult.ParetoUpdate> {
                return@map WebhookExecuteRequest.builder().content(
                    """
                        |**Pareto** *${puzzle.displayName}*
                        |${record.score.toDisplayString()} was included in the pareto frontier.
                        |""".trimMargin()
                )
                    .build()
            }
            results.filterIsInstance<UpdateResult.BetterExists>().ifNotEmpty { betterExists ->
                return@map WebhookExecuteRequest.builder().content("""
                        |No Scores beaten by *${puzzle.displayName}* `${record.score.toDisplayString()}`
                        |Existing scores:
                        |${betterExistsToString(betterExists)}
                        |""".trimMargin())
                    .build()
            }
            results.findInstance<UpdateResult.NotSupported> {
                throw IllegalArgumentException("No leaderboard supporting your submission found")
            }
            throw IllegalArgumentException("sorry, something went wrong")
        }

    private fun betterExistsToString(betterExists: Collection<UpdateResult.BetterExists>) =
        betterExists.flatMap { it.scores.entries }
            .sortedBy<Map.Entry<Category<*, *>, Score>, Comparable<*>> { it.key as? Comparable<*> }
            .joinToString("\n") { "**${it.key.displayName}**\n`${it.value.toDisplayString()}`" }

    abstract fun parseSubmission(options: List<ApplicationCommandInteractionOption>, user: User): Mono<Tuple2<P, R>>
}