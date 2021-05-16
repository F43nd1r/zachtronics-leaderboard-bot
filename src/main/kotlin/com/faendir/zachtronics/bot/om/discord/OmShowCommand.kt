package com.faendir.zachtronics.bot.om.discord

import com.faendir.discord4j.command.annotation.ApplicationCommand
import com.faendir.discord4j.command.annotation.Description
import com.faendir.zachtronics.bot.generic.discord.AbstractShowCommand
import com.faendir.zachtronics.bot.model.Leaderboard
import com.faendir.zachtronics.bot.om.model.*
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.discordjson.json.ApplicationCommandOptionData
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.util.function.Tuple2
import reactor.util.function.Tuples

@Component
class OmShowCommand(private val opusMagnum: OpusMagnum, leaderboards: List<Leaderboard<OmCategory, OmScore, OmPuzzle, OmRecord>>) :
    AbstractShowCommand<OmCategory, OmScore, OmPuzzle, OmRecord>(opusMagnum, leaderboards) {

    override fun buildData(): ApplicationCommandOptionData = ShowParser.buildData()

    override fun findPuzzleAndCategory(options: List<ApplicationCommandInteractionOption>): Mono<Tuple2<OmPuzzle, OmCategory>> {
        return Mono.fromCallable { ShowParser.parse(options) }.map { show ->
            val puzzle = findPuzzle(show)
            val categories = findCategoryCandidates(show)
            if (categories.isEmpty()) throw IllegalArgumentException("${show.primaryMetric}/${show.tiebreaker ?: ""} is not a tracked category.")
            val filtered = categories.filter { it.supportsPuzzle(puzzle) }
            if (filtered.isEmpty()) throw IllegalArgumentException("Category ${categories.first().displayName} does not support ${puzzle.displayName}")
            Tuples.of(puzzle, filtered.first())
        }
    }

    private fun findCategoryCandidates(show: Show): List<OmCategory> {
        return OmCategory.values().filter { show.primaryMetric == it.primaryMetric && (show.tiebreaker == null || show.tiebreaker == it.tiebreaker) }
    }

    private fun findPuzzle(show: Show): OmPuzzle {
        return opusMagnum.parsePuzzle(show.puzzle)
    }
}

@ApplicationCommand(subCommand = true)
data class Show(@Description("Puzzle name. Can be shortened or abbreviated. E.g. `stab water`, `PMO`")
                val puzzle: String,
                @Description("Primary Metric")
                val primaryMetric: OmMetric,
                @Description("Tiebreaker")
                val tiebreaker: OmMetric?,
                @Description("Metric Modifier")
                val modifier: OmModifier?)