package com.faendir.zachtronics.bot.om.discord

import com.faendir.discord4j.command.annotation.ApplicationCommand
import com.faendir.discord4j.command.annotation.Description
import com.faendir.zachtronics.bot.generic.discord.Command
import com.faendir.zachtronics.bot.om.model.OmModifier
import com.faendir.zachtronics.bot.om.model.OmRecord
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.entity.User
import discord4j.discordjson.json.ApplicationCommandOptionData
import discord4j.discordjson.json.WebhookExecuteRequest
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2

@Component
class OmSubmitArchiveCommand(private val archiveCommand: OmArchiveCommand, private val submitCommand: OmSubmitCommand) : Command {
    override val name: String = "submit-archive"
    override val isReadOnly: Boolean = false

    override fun handle(options: List<ApplicationCommandInteractionOption>, user: User): Mono<WebhookExecuteRequest> {
        return Mono.fromCallable { SubmitArchiveParser.parse(options) }.flatMap { submitArchive ->
            archiveCommand.parseSolution(archiveCommand.findScoreIdentifier(submitArchive), submitArchive.solution).flatMap { solution ->
                archiveCommand.archive(solution).zipWith(
                    submitCommand.submitToLeaderboards(solution.puzzle, OmRecord(solution.score, submitArchive.gif, user.username))
                ).map { (archiveOut, submitOut) -> WebhookExecuteRequest.builder().from(submitOut).content(archiveOut).build() }
            }
        }
    }

    override fun buildData(): ApplicationCommandOptionData = SubmitArchiveParser.buildData()
}

@ApplicationCommand(description = "Submit and archive a solution", subCommand = true)
data class SubmitArchive(
    @Description("Link to your solution file")
    override val solution: String,
    @Description("Link to your solution gif/mp4")
    val gif: String,
    @Description("Metric Modifier")
    override val modifier: OmModifier?,
    @Description("Score part for nonstandard metrics. E.g. `4h`, `3.5w`")
    override val score: String?
) : IArchive