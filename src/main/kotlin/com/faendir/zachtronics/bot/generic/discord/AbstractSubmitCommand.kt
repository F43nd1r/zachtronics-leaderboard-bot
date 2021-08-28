package com.faendir.zachtronics.bot.generic.discord

import com.faendir.zachtronics.bot.model.*
import com.faendir.zachtronics.bot.utils.asMultipartRequest
import com.faendir.zachtronics.bot.utils.findInstance
import com.faendir.zachtronics.bot.utils.ifNotEmpty
import discord4j.core.event.domain.interaction.SlashCommandEvent
import discord4j.discordjson.json.*
import discord4j.rest.util.MultipartRequest
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import reactor.util.function.Tuple2

abstract class AbstractSubmitCommand<P : Puzzle, R : Record> : AbstractCommand() {
    override val isReadOnly: Boolean = false
    protected abstract val leaderboards: List<Leaderboard<*, P, R>>

    override fun handle(event: SlashCommandEvent): Mono<MultipartRequest<WebhookExecuteRequest>> = mono {
        val (puzzle, record) = parseSubmission(event).awaitSingle()
        submitToLeaderboards(puzzle, record).awaitSingle().asMultipartRequest()
    }

    fun submitToLeaderboards(puzzle: P, record: R): Mono<WebhookExecuteRequest> = mono {
        val results = leaderboards.map { it.update(puzzle, record).awaitSingle() }
        results.filterIsInstance<UpdateResult.Success>().ifNotEmpty { successes ->
            return@mono WebhookExecuteRequest.builder().addEmbed(
                EmbedData.builder()
                    .title("Success: *${puzzle.displayName}* ${successes.flatMap { it.oldScores.keys }.joinToString { it.displayName }}")
                    .description("`${record.score.toDisplayString()}`\npreviously:")
                    .addAllFields(successes.flatMap { it.oldScores.entries }
                        .map { EmbedFieldData.builder().name(it.key.displayName).value("`${it.value?.toDisplayString() ?: "none"}`").inline(true).build() })
                    .link(record.link)
                    .build()
            )
                .build()
        }
        results.findInstance<UpdateResult.ParetoUpdate> {
            return@mono WebhookExecuteRequest.builder()
                .addEmbed(
                    EmbedData.builder()
                        .title("Pareto *${puzzle.displayName}*")
                        .description("${record.score.toDisplayString()} was included in the pareto frontier.")
                        .link(record.link)
                        .build()
                )
                .build()
        }
        results.filterIsInstance<UpdateResult.BetterExists>().ifNotEmpty { betterExists ->
            return@mono WebhookExecuteRequest.builder()
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

    private val allowedImageTypes = setOf("gif", "png", "jpg")

    private fun ImmutableEmbedData.Builder.link(link: String) = apply {
        if (allowedImageTypes.contains(link.substringAfterLast(".", ""))) {
            image(EmbedImageData.builder().url(link).build())
        } else {
            addField(EmbedFieldData.builder().name("Link").value("[$link]($link)").inline(false).build())
        }
    }

    abstract fun parseSubmission(interaction: SlashCommandEvent): Mono<Tuple2<P, R>>
}