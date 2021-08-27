package com.faendir.zachtronics.bot.generic.discord

import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Record
import com.faendir.zachtronics.bot.model.Solution
import discord4j.core.event.domain.interaction.SlashCommandEvent
import discord4j.discordjson.json.WebhookExecuteRequest
import discord4j.rest.util.MultipartRequest
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import reactor.kotlin.core.util.function.component3
import reactor.util.function.Tuple3

@Component
abstract class AbstractSubmitArchiveCommand<P: Puzzle, R: Record, S : Solution> : AbstractCommand() {
    override val isReadOnly: Boolean = false
    protected abstract val submitCommand: AbstractSubmitCommand<P, R>
    protected abstract val archiveCommand: AbstractArchiveCommand<S>

    override fun handle(interaction: SlashCommandEvent): Mono<MultipartRequest<WebhookExecuteRequest>> {
        return parseToPRS(interaction).flatMap { (puzzle, record, solution) ->
            Mono.zip(
                archiveCommand.archive(solution),
                submitCommand.submitToLeaderboards(puzzle, record)
            )
        }.map { (archiveOut, submitOut) ->
            WebhookExecuteRequest.builder().from(submitOut).addEmbed(archiveOut).build()
        }.map { MultipartRequest.ofRequest(it) }
    }

    abstract fun parseToPRS(interaction: SlashCommandEvent): Mono<Tuple3<P, R, S>>
}