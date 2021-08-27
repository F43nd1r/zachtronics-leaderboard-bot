package com.faendir.zachtronics.bot.generic.discord

import com.faendir.zachtronics.bot.generic.archive.Archive
import com.faendir.zachtronics.bot.model.Solution
import discord4j.core.`object`.command.Interaction
import discord4j.discordjson.json.EmbedData
import discord4j.discordjson.json.WebhookExecuteRequest
import discord4j.rest.util.MultipartRequest
import reactor.core.publisher.Mono

abstract class AbstractArchiveCommand<S : Solution> : Command {
    override val name: String = "archive"
    override val isReadOnly: Boolean = false
    protected abstract val archive: Archive<S>

    override fun handle(interaction: Interaction): Mono<MultipartRequest<WebhookExecuteRequest>> {
        return parseSolution(interaction)
            .flatMap { solution -> archive(solution) }
            .map { WebhookExecuteRequest.builder().addEmbed(it).build() }
            .map { MultipartRequest.ofRequest(it) }
    }

    fun archive(solution: S): Mono<EmbedData> =
        archive.archive(solution).map { (title, description) -> getResultMessage(title, description, solution) }

    private fun getResultMessage(title:String, description:String, solution: S): EmbedData {
        return if (title.isNotEmpty()) {
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

    abstract fun parseSolution(interaction: Interaction): Mono<S>
}