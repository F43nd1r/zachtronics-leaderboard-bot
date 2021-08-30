package com.faendir.zachtronics.bot.generic.discord

import com.faendir.zachtronics.bot.generic.archive.Archive
import com.faendir.zachtronics.bot.model.Solution
import discord4j.core.event.domain.interaction.SlashCommandEvent
import discord4j.discordjson.json.EmbedData
import discord4j.discordjson.json.EmbedFieldData
import discord4j.discordjson.json.WebhookExecuteRequest
import discord4j.rest.util.MultipartRequest

abstract class AbstractArchiveCommand<S : Solution> : AbstractCommand() {
    override val isReadOnly: Boolean = false
    protected abstract val archive: Archive<S>

    override fun handle(event: SlashCommandEvent): MultipartRequest<WebhookExecuteRequest> {
        val solutions = parseSolutions(event)
        val embed = archiveAll(solutions)
        return MultipartRequest.ofRequest(WebhookExecuteRequest.builder().addEmbed(embed).build())
    }

    fun archiveAll(solutions: Collection<S>): EmbedData {
        val results = archive.archiveAll(solutions)
        val successes = results.count { it.first.isNotEmpty() }
        return EmbedData.builder()
            .title(if (successes != 0) "Success: $successes solution(s) archived" else "Failure: no solutions archived")
            .addAllFields(
                solutions.zip(results).map { (solution, result) ->
                    if (result.first.isNotEmpty()) {
                        EmbedFieldData.builder()
                            .name("*${solution.puzzle.displayName}* ${result.first}")
                            .value(
                                "`${solution.score.toDisplayString()}` has been archived.\n" +
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

    abstract fun parseSolutions(interaction: SlashCommandEvent): List<S>
}