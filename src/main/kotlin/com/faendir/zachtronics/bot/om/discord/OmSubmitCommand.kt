package com.faendir.zachtronics.bot.om.discord

import com.faendir.discord4j.command.annotation.ApplicationCommand
import com.faendir.discord4j.command.annotation.Converter
import com.faendir.discord4j.command.annotation.Description
import com.faendir.zachtronics.bot.generic.discord.AbstractSubmitCommand
import com.faendir.zachtronics.bot.generic.discord.LinkConverter
import com.faendir.zachtronics.bot.model.Leaderboard
import com.faendir.zachtronics.bot.om.model.OmModifier
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmRecord
import com.faendir.zachtronics.bot.om.model.OmScore
import discord4j.core.event.domain.interaction.SlashCommandEvent
import org.springframework.stereotype.Component

@Component
class OmSubmitCommand(override val leaderboards: List<Leaderboard<*, OmPuzzle, OmRecord>>) :
    AbstractSubmitCommand<OmPuzzle, OmRecord>() {

    override fun buildData() = SubmitParser.buildData()

    override fun parseSubmission(interaction: SlashCommandEvent): Pair<OmPuzzle, OmRecord> {
        val submit = SubmitParser.parse(interaction)
        return Pair(
            submit.puzzle,
            OmRecord(OmScore.parse(submit.puzzle, submit.score).apply { modifier = submit.modifier ?: OmModifier.NORMAL }, submit.gif, interaction.interaction.user.username)
        )
    }
}

@ApplicationCommand(description = "Submit a solution", subCommand = true)
data class Submit(
    @Description("Puzzle name. Can be shortened or abbreviated. E.g. `stab water`, `PMO`")
    @Converter(PuzzleConverter::class)
    val puzzle: OmPuzzle,
    @Description("Puzzle score. E.g. `100/32/14/22`, `3.5w/32c/100g`")
    val score: String,
    @Converter(LinkConverter::class)
    @Description("Link to your solution gif/mp4, can be `m1` to scrape it from your last message")
    val gif: String,
    @Description("Metric Modifier")
    val modifier: OmModifier?
)