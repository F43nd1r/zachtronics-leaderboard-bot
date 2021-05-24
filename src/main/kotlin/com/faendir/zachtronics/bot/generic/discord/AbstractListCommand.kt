package com.faendir.zachtronics.bot.generic.discord

import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.model.Leaderboard
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Record
import discord4j.core.`object`.command.Interaction
import discord4j.discordjson.json.EmbedData
import discord4j.discordjson.json.EmbedFieldData
import discord4j.discordjson.json.WebhookExecuteRequest
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import reactor.util.function.Tuple2
import java.util.*

abstract class AbstractListCommand<C : Category, P : Puzzle, R : Record> : Command {
    abstract val leaderboards: List<Leaderboard<C, P, R>>
    override val name: String = "list"
    override val isReadOnly: Boolean = true

    override fun handle(interaction: Interaction): Mono<WebhookExecuteRequest> {
        return findPuzzleAndCategories(interaction).flatMap { (puzzle, categories) ->
            categories.toFlux().flatMap { category ->
                Mono.zip(
                    category.toMono(),
                    getRecord(puzzle, category).map { Optional.of(it) }.switchIfEmpty(Optional.empty<R>().toMono())
                )
            }.collectList()
                .map { records ->
                    WebhookExecuteRequest.builder()
                        .addEmbed(EmbedData.builder()
                            .title("*${puzzle.displayName}*")
                            .addAllFields(
                                records
                                    .groupBy ({ it.t2.orElse(null) }, {it.t1})
                                    .map { entry -> entry.key to entry.value.sortedBy<C, Comparable<*>> { it as? Comparable<*> } }
                                    .sortedBy<Pair<R?, List<C>>, Comparable<*>> { it.second.first() as? Comparable<*> }
                                    .map { (record, categories) ->
                                    EmbedFieldData.builder()
                                        .name(categories.joinToString("/") { it.displayName })
                                        .value(record?.let { "[${it.score.toDisplayString()}](${it.link})" } ?: "None")
                                        .inline(true)
                                        .build()
                                }
                            )
                            .build())
                        .build()
                }
        }
    }

    private fun getRecord(puzzle: P, category: C): Mono<R> = leaderboards.toFlux().flatMap { it.get(puzzle, category) }.next()

    abstract fun findPuzzleAndCategories(interaction: Interaction): Mono<Tuple2<P, List<C>>>
}