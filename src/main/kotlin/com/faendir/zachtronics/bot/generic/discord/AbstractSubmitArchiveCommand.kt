package com.faendir.zachtronics.bot.generic.discord

import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Record
import com.faendir.zachtronics.bot.model.Solution
import com.faendir.zachtronics.bot.utils.asMultipartRequest
import discord4j.core.event.domain.interaction.SlashCommandEvent
import discord4j.discordjson.json.WebhookExecuteRequest
import discord4j.rest.util.MultipartRequest
import org.springframework.stereotype.Component
import java.util.*

@Component
abstract class AbstractSubmitArchiveCommand<P : Puzzle, R : Record, S : Solution> : AbstractCommand() {
    override val isReadOnly: Boolean = false
    protected abstract val submitCommand: AbstractSubmitCommand<P, R>
    protected abstract val archiveCommand: AbstractArchiveCommand<S>

    override fun handle(event: SlashCommandEvent): MultipartRequest<WebhookExecuteRequest>  {
        val (puzzle, record, solution) = parseToPRS(event)
        val submitOut = submitCommand.submitToLeaderboards(puzzle, record)
        val archiveOut = archiveCommand.archiveAll(Collections.singleton(solution))
        return WebhookExecuteRequest.builder()
            .from(submitOut)
            .addEmbed(archiveOut)
            .build()
            .asMultipartRequest()
    }

    abstract fun parseToPRS(event: SlashCommandEvent): Triple<P, R, S>
}