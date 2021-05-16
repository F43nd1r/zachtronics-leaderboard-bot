package com.faendir.zachtronics.bot.om.discord

import com.faendir.discord4j.command.annotation.ApplicationCommand
import com.faendir.discord4j.command.annotation.Description
import com.faendir.om.dsl.DslGenerator
import com.faendir.om.parser.solution.SolutionParser
import com.faendir.om.parser.solution.model.SolvedSolution
import com.faendir.zachtronics.bot.generic.archive.Archive
import com.faendir.zachtronics.bot.generic.discord.AbstractArchiveCommand
import com.faendir.zachtronics.bot.om.model.*
import com.faendir.zachtronics.bot.om.model.OmScorePart.*
import com.faendir.zachtronics.bot.utils.filterIsInstance
import com.faendir.zachtronics.bot.utils.throwIfEmpty
import com.roxstudio.utils.CUrl
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.entity.User
import discord4j.discordjson.json.ApplicationCommandOptionData
import kotlinx.io.streams.asInput
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import reactor.util.function.Tuples
import java.io.ByteArrayInputStream

@Component
class OmArchiveCommand(override val archive: Archive<OmSolution>) : AbstractArchiveCommand<OmSolution>() {
    private val regex = Regex("!archive\\s+(?<score>\\S+)(\\s+(?<link>\\S+))?")

    override fun buildData(): ApplicationCommandOptionData = ArchiveParser.buildData()

    override fun parseSolution(options: List<ApplicationCommandInteractionOption>, user: User): Mono<OmSolution> {
        return Mono.just(ArchiveParser.parse(options))
            .map { Tuples.of(findScoreIdentifier(it), it.solution) }
            .flatMap { (identifier, link) -> parseSolution(identifier, link) }
    }

    fun parseSolution(scoreIdentifier: ScoreIdentifier, link: String): Mono<OmSolution> {
        return link.toMono().map {
            SolutionParser.parse(ByteArrayInputStream(CUrl(it).exec()).asInput())
        }.onErrorMap { IllegalArgumentException("I could not parse your solution") }
            .filterIsInstance<SolvedSolution>()
            .throwIfEmpty { "only solved solutions are accepted" }
            .map { solution ->
                val puzzle =
                    OmPuzzle.values().find { it.id == solution.puzzle } ?: throw IllegalArgumentException("I do not know the puzzle \"${solution.puzzle}\"")
                val parts = linkedMapOf(
                    COST to solution.cost.toDouble(),
                    CYCLES to solution.cycles.toDouble(),
                    AREA to solution.area.toDouble(),
                    INSTRUCTIONS to solution.instructions.toDouble()
                )
                val score = when (scoreIdentifier) {
                    is ScoreIdentifier.Part -> {
                        parts[scoreIdentifier.scorePart] = scoreIdentifier.value
                        OmScore(parts)
                    }
                    is ScoreIdentifier.Modifier -> OmScore(parts, scoreIdentifier.modifier)
                    is ScoreIdentifier.Normal -> OmScore(parts)
                }
                OmSolution(puzzle, score, DslGenerator.toDsl(solution))
            }
    }

    fun findScoreIdentifier(archive: IArchive): ScoreIdentifier {
        return when {
            archive.score != null -> {
                OmScorePart.parse(archive.score!!)?.let { ScoreIdentifier.Part(it.first, it.second) }
                    ?: throw IllegalArgumentException("I didn't understand \"${archive.score}\".")
            }
            archive.modifier != null -> {
                ScoreIdentifier.Modifier(archive.modifier!!)
            }
            else -> {
                ScoreIdentifier.Normal
            }
        }
    }

}

sealed class ScoreIdentifier {
    object Normal : ScoreIdentifier()
    class Modifier(val modifier: OmModifier) : ScoreIdentifier()
    class Part(val scorePart: OmScorePart, val value: Double) : ScoreIdentifier()
}

interface IArchive {
    val solution: String
    val modifier: OmModifier?
    val score: String?
}

@ApplicationCommand(description = "Archive a solution", subCommand = true)
data class Archive(
    @Description("Link to your solution file")
    override val solution: String,
    @Description("Metric Modifier")
    override val modifier: OmModifier?,
    @Description("Score part for nonstandard metrics. E.g. `4h`, `3.5w`")
    override val score: String?
) : IArchive