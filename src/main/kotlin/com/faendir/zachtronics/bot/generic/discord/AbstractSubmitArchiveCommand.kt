package com.faendir.zachtronics.bot.generic.discord

import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Record
import com.faendir.zachtronics.bot.model.Solution
import com.faendir.zachtronics.bot.utils.asMultipartRequest
import discord4j.core.event.domain.interaction.SlashCommandEvent
import discord4j.discordjson.json.WebhookExecuteRequest
import discord4j.rest.util.MultipartRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import reactor.kotlin.core.util.function.component3
import reactor.util.function.Tuple3

@Component
abstract class AbstractSubmitArchiveCommand<P : Puzzle, R : Record, S : Solution> : AbstractCommand() {
    override val isReadOnly: Boolean = false
    protected abstract val submitCommand: AbstractSubmitCommand<P, R>
    protected abstract val archiveCommand: AbstractArchiveCommand<S>

    override fun handle(event: SlashCommandEvent): Mono<MultipartRequest<WebhookExecuteRequest>> = mono {
        val (puzzle, record, solution) = parseToPRS(event).awaitSingle()
        val submitOut = async { submitCommand.submitToLeaderboards(puzzle, record).awaitSingle() }
        val archiveOut = async { archiveCommand.archive(solution).awaitSingle() }
        WebhookExecuteRequest.builder()
            .from(submitOut.await())
            .addEmbed(archiveOut.await())
            .build()
            .asMultipartRequest()
    }

    abstract fun parseToPRS(event: SlashCommandEvent): Mono<Tuple3<P, R, S>>
}