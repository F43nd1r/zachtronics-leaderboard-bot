package com.faendir.zachtronics.bot.generic.discord

import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.model.Leaderboard
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Record
import discord4j.core.event.domain.interaction.SlashCommandEvent
import discord4j.discordjson.json.WebhookExecuteRequest
import discord4j.rest.util.MultipartRequest
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import reactor.util.function.Tuple2

abstract class AbstractShowCommand<C : Category, P : Puzzle, R : Record> : AbstractCommand() {
    abstract val leaderboards: List<Leaderboard<C, P, R>>
    override val isReadOnly: Boolean = true

    override fun handle(event: SlashCommandEvent): Mono<MultipartRequest<WebhookExecuteRequest>> = mono {
        val (puzzle, category) = findPuzzleAndCategory(event).awaitSingle()
        val record = leaderboards.asFlow().map { it.get(puzzle, category).awaitSingle() }.firstOrNull()
            ?: throw IllegalArgumentException("sorry, there is no score for ${puzzle.displayName} ${category.displayName}.")
        MultipartRequest.ofRequestAndFiles(
            WebhookExecuteRequest.builder().content("*${puzzle.displayName}* **${category.displayName}**\n${record.toDisplayString()}").build(),
            record.attachments()
        )
    }

    abstract fun findPuzzleAndCategory(interaction: SlashCommandEvent): Mono<Tuple2<P, C>>
}