package com.faendir.zachtronics.bot.generic.discord

import com.faendir.zachtronics.bot.generic.archive.Archive
import com.faendir.zachtronics.bot.model.Solution
import discord4j.core.event.domain.interaction.SlashCommandEvent
import discord4j.discordjson.json.EmbedData
import discord4j.discordjson.json.WebhookExecuteRequest
import discord4j.rest.util.MultipartRequest
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Mono

abstract class AbstractArchiveCommand<S : Solution> : AbstractCommand() {
    override val isReadOnly: Boolean = false
    protected abstract val archive: Archive<S>

    override fun handle(event: SlashCommandEvent): Mono<MultipartRequest<WebhookExecuteRequest>> = mono {
        val solution = parseSolution(event).awaitSingle()
        val embed = archive(solution).awaitSingle()
        MultipartRequest.ofRequest(WebhookExecuteRequest.builder().addEmbed(embed).build())
    }

    fun archive(solution: S): Mono<EmbedData> = mono {
        val (title, description) = archive.archive(solution).awaitSingle()
        if (title.isNotEmpty()) {
            EmbedData.builder()
                .title("Success: *${solution.puzzle.displayName}* $title")
                .description("`${solution.score.toDisplayString()}` has been archived.\n\n$description")
                .build()
        } else {
            EmbedData.builder()
                .title("Failure: *${solution.puzzle.displayName}*")
                .description("`${solution.score.toDisplayString()}` did not qualify for archiving.")
                .build()
        }
    }

    abstract fun parseSolution(interaction: SlashCommandEvent): Mono<S>
}