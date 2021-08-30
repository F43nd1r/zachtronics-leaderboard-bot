package com.faendir.zachtronics.bot.om.discord

import com.faendir.discord4j.command.annotation.ApplicationCommand
import com.faendir.discord4j.command.annotation.Converter
import com.faendir.discord4j.command.annotation.Description
import com.faendir.zachtronics.bot.discord.LinkConverter
import com.faendir.zachtronics.bot.discord.command.AbstractSubmitArchiveCommand
import com.faendir.zachtronics.bot.discord.command.Secured
import com.faendir.zachtronics.bot.om.OmQualifier
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmRecord
import com.faendir.zachtronics.bot.om.model.OmSolution
import discord4j.core.event.domain.interaction.SlashCommandEvent
import discord4j.discordjson.json.ApplicationCommandOptionData
import org.springframework.stereotype.Component

@Component
@OmQualifier
class OmSubmitArchiveCommand(override val archiveCommand: OmArchiveCommand, override val submitCommand: OmSubmitCommand) :
    AbstractSubmitArchiveCommand<OmPuzzle, OmRecord, OmSolution>(), Secured by OmSecured {

    override fun buildData(): ApplicationCommandOptionData = SubmitArchiveParser.buildData()

    override fun parseToPRS(event: SlashCommandEvent): Triple<OmPuzzle, OmRecord, OmSolution> {
        val submitArchive = SubmitArchiveParser.parse(event)
        val solution = archiveCommand.parseSolution(archiveCommand.findScoreIdentifier(submitArchive), submitArchive.solution)
        return Triple(
            solution.puzzle,
            OmRecord(solution.score, submitArchive.gif, event.interaction.user.username),
            solution
        )
    }
}

@ApplicationCommand(description = "Submit and archive a solution", subCommand = true)
data class SubmitArchive(
    @Converter(LinkConverter::class)
    @Description("Link to your solution file, can be `m1` to scrape it from your last message")
    override val solution: String,
    @Converter(LinkConverter::class)
    @Description("Link to your solution gif/mp4, can be `m1` to scrape it from your last message")
    val gif: String,
    @Description("Score part for nonstandard metrics. E.g. `4h`, `3.5w`")
    override val score: String?
) : IArchive