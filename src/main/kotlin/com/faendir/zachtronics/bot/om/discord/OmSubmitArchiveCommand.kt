package com.faendir.zachtronics.bot.om.discord

import com.faendir.discord4j.command.annotation.ApplicationCommand
import com.faendir.discord4j.command.annotation.Description
import com.faendir.zachtronics.bot.generic.discord.Command
import com.faendir.zachtronics.bot.om.model.OmModifier
import com.faendir.zachtronics.bot.om.model.OmRecord
import com.faendir.zachtronics.bot.utils.findLink
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.User
import discord4j.discordjson.json.ApplicationCommandOptionData
import discord4j.discordjson.json.WebhookExecuteRequest
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2

@Component
class OmSubmitArchiveCommand(private val archiveCommand: OmArchiveCommand, private val submitCommand: OmSubmitCommand) : Command {
    override val name: String = "submit-archive"
    override val isReadOnly: Boolean = false

    override fun handle(options: List<ApplicationCommandInteractionOption>, user: User, previousMessages: Flux<Message>): Mono<WebhookExecuteRequest> {
        return Mono.fromCallable { SubmitArchiveParser.parse(options) }.flatMap { submitArchive ->
            findLink(submitArchive.solution, previousMessages)
                .flatMap { archiveCommand.parseSolution(archiveCommand.findScoreIdentifier(submitArchive), it) }
                .flatMap { solution ->
                    archiveCommand.archive(solution).zipWith(
                        findLink(submitArchive.gif, previousMessages)
                            .flatMap { submitCommand.submitToLeaderboards(solution.puzzle, OmRecord(solution.score, it, user.username)) }
                    ).map { (archiveOut, submitOut) -> WebhookExecuteRequest.builder().from(submitOut).addEmbed(archiveOut).build() }
                }
        }
    }

    override fun buildData(): ApplicationCommandOptionData = SubmitArchiveParser.buildData()
}

@ApplicationCommand(description = "Submit and archive a solution", subCommand = true)
data class SubmitArchive(
    @Description("Link to your solution file, can be `m1` to scrape it from your last message")
    override val solution: String,
    @Description("Link to your solution gif/mp4, can be `m1` to scrape it from your last message")
    val gif: String,
    @Description("Metric Modifier")
    override val modifier: OmModifier?,
    @Description("Score part for nonstandard metrics. E.g. `4h`, `3.5w`")
    override val score: String?
) : IArchive