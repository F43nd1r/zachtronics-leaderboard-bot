package com.faendir.zachtronics.bot.generic.discord

import com.faendir.zachtronics.bot.generic.archive.Archive
import com.faendir.zachtronics.bot.model.Solution
import discord4j.core.event.domain.interaction.SlashCommandEvent
import discord4j.discordjson.json.EmbedData
import discord4j.discordjson.json.EmbedFieldData
import discord4j.discordjson.json.WebhookExecuteRequest
import discord4j.rest.util.MultipartRequest
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

abstract class AbstractArchiveCommand<S : Solution> : AbstractCommand() {
    override val isReadOnly: Boolean = false
    protected abstract val archive: Archive<S>

    override fun handle(event: SlashCommandEvent): Mono<MultipartRequest<WebhookExecuteRequest>> = mono {
        val solutions = parseSolutions(event).collectList().awaitSingle()
        val embed = archiveAll(solutions).awaitSingle()
        MultipartRequest.ofRequest(WebhookExecuteRequest.builder().addEmbed(embed).build())
    }

    fun archiveAll(solutions: Collection<S>): Mono<EmbedData> = mono {
        val results = archive.archiveAll(solutions).awaitSingle()
        val successes = results.count { it.first.isNotEmpty() }
        EmbedData.builder()
            .title(if (successes != 0) "Success: $successes solution(s) archived" else "Failure: no solutions archived")
            .addAllFields(
                solutions.zip(results).map { (solution, result) ->
                    if (result.first.isNotEmpty()) {
                        EmbedFieldData.builder()
                            .name("*${solution.puzzle.displayName}* ${result.first}")
                            .value(
                                "`${solution.score.toDisplayString()}` has been archived.\n" +
                                        "\n" +
                                        result.second
                            ).build()
                    } else {
                        EmbedFieldData.builder()
                            .name("*${solution.puzzle.displayName}*")
                            .value("`${solution.score.toDisplayString()}` did not qualify for archiving.")
                            .build()
                    }
                }
            )
            .build()
    }

    abstract fun parseSolutions(interaction: SlashCommandEvent): Flux<S>
}