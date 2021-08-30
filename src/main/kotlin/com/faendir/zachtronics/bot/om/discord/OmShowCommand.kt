package com.faendir.zachtronics.bot.om.discord

import com.faendir.discord4j.command.annotation.ApplicationCommand
import com.faendir.discord4j.command.annotation.Converter
import com.faendir.discord4j.command.annotation.Description
import com.faendir.zachtronics.bot.discord.command.AbstractShowCommand
import com.faendir.zachtronics.bot.model.Leaderboard
import com.faendir.zachtronics.bot.om.OmQualifier
import com.faendir.zachtronics.bot.om.model.OmCategory
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmRecord
import discord4j.core.event.domain.interaction.SlashCommandEvent
import discord4j.discordjson.json.ApplicationCommandOptionData
import org.springframework.stereotype.Component

@Component
@OmQualifier
class OmShowCommand(override val leaderboards: List<Leaderboard<OmCategory, OmPuzzle, OmRecord>>) :
    AbstractShowCommand<OmCategory, OmPuzzle, OmRecord>() {

    override fun buildData(): ApplicationCommandOptionData = ShowParser.buildData()

    override fun findPuzzleAndCategory(interaction: SlashCommandEvent): Pair<OmPuzzle, OmCategory> {
        val show = ShowParser.parse(interaction)
        val puzzle = show.puzzle
        val categories = findCategoryCandidates(show)
        if (categories.isEmpty()) throw IllegalArgumentException("${show.category} is not a tracked category.")
        val filtered = categories.filter { it.supportsPuzzle(puzzle) }
        if (filtered.isEmpty()) throw IllegalArgumentException("Category ${categories.first().displayName} does not support ${puzzle.displayName}")
        return Pair(puzzle, filtered.first())
    }

    private fun findCategoryCandidates(show: Show): List<OmCategory> {
        return OmCategory.values().filter { it.displayName.startsWith(show.category, ignoreCase = true) }
    }

}

@ApplicationCommand(description = "Show a record", subCommand = true)
data class Show(
    @Description("Puzzle name. Can be shortened or abbreviated. E.g. `stab water`, `PMO`")
    @Converter(PuzzleConverter::class)
    val puzzle: OmPuzzle,
    @Description("Category. E.g. `GC`, `sum`")
    val category: String
)

