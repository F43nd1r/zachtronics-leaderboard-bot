package com.faendir.zachtronics.bot.generic.discord

import com.faendir.zachtronics.bot.model.*
import com.faendir.zachtronics.bot.utils.findInstance
import com.faendir.zachtronics.bot.utils.ifNotEmpty
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.entity.User
import discord4j.discordjson.json.EmbedData
import discord4j.discordjson.json.EmbedFieldData
import discord4j.discordjson.json.EmbedImageData
import discord4j.discordjson.json.WebhookExecuteRequest
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import reactor.util.function.Tuple2

abstract class AbstractSubmitCommand<P : Puzzle, R : Record> : Command {
    override val name: String = "submit"
    override val isReadOnly: Boolean = false
    protected abstract val leaderboards: List<Leaderboard<*, P, R>>

    override fun handle(options: List<ApplicationCommandInteractionOption>, user: User): Mono<WebhookExecuteRequest> {
        return parseSubmission(options, user).flatMap { (puzzle, record) -> submitToLeaderboards(puzzle, record) }
    }

    fun submitToLeaderboards(puzzle: P, record: R): Mono<WebhookExecuteRequest> =
        leaderboards.toFlux().flatMap { it.update(puzzle, record) }.collectList().map { results: List<UpdateResult> ->
            results.filterIsInstance<UpdateResult.Success>().ifNotEmpty { successes ->
                return@map WebhookExecuteRequest.builder().addEmbed(
                    EmbedData.builder()
                        .title("Success: *${puzzle.displayName}* ${successes.flatMap { it.oldScores.keys }.joinToString { it.displayName }}")
                        .description("`${record.score.toDisplayString()}`\npreviously:")
                        .image(EmbedImageData.builder().url(record.link).build())
                        .addAllFields(successes.flatMap { it.oldScores.entries }
                            .map { EmbedFieldData.builder().name(it.key.displayName).value("`${it.value?.toDisplayString() ?: "none"}`").inline(true).build() })
                        .build()
                )
                    .build()
            }
            results.findInstance<UpdateResult.ParetoUpdate> {
                return@map WebhookExecuteRequest.builder()
                    .addEmbed(
                        EmbedData.builder()
                            .title("Pareto *${puzzle.displayName}*")
                            .description("${record.score.toDisplayString()} was included in the pareto frontier.")
                            .image(EmbedImageData.builder().url(record.link).build())
                            .build()
                    )
                    .build()
            }
            results.filterIsInstance<UpdateResult.BetterExists>().ifNotEmpty { betterExists ->
                return@map WebhookExecuteRequest.builder()
                    .addEmbed(
                        EmbedData.builder()
                            .title("No Scores beaten by *${puzzle.displayName}* `${record.score.toDisplayString()}`")
                            .description("Existing scores:")
                            .addAllFields(betterExists.flatMap { it.scores.entries }
                                .sortedBy<Map.Entry<Category, Score>, Comparable<*>> { it.key as? Comparable<*> }
                                .map { EmbedFieldData.builder().name(it.key.displayName).value("\n`${it.value.toDisplayString()}`").inline(true).build() })
                            .build()
                    )
                    .build()
            }
            results.findInstance<UpdateResult.NotSupported> {
                throw IllegalArgumentException("No leaderboard supporting your submission found")
            }
            throw IllegalArgumentException("sorry, something went wrong")
        }

    abstract fun parseSubmission(options: List<ApplicationCommandInteractionOption>, user: User): Mono<Tuple2<P, R>>
}