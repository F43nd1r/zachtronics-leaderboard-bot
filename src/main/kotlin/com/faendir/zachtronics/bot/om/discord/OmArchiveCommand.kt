package com.faendir.zachtronics.bot.om.discord

import com.faendir.om.dsl.DslGenerator
import com.faendir.om.parser.solution.SolutionParser
import com.faendir.om.parser.solution.model.SolvedSolution
import com.faendir.zachtronics.bot.generic.archive.Archive
import com.faendir.zachtronics.bot.generic.discord.AbstractArchiveCommand
import com.faendir.zachtronics.bot.om.model.*
import com.faendir.zachtronics.bot.om.model.OmScorePart.*
import com.faendir.zachtronics.bot.utils.Result
import com.faendir.zachtronics.bot.utils.and
import com.faendir.zachtronics.bot.utils.match
import com.roxstudio.utils.CUrl
import kotlinx.io.streams.asInput
import net.dv8tion.jda.api.entities.Message
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream

@Component
class OmArchiveCommand(override val archive: Archive<OmSolution>) : AbstractArchiveCommand<OmSolution>() {
    private val regex = Regex("!archive\\s+(?<score>\\S+)(\\s+(?<link>\\S+))?")

    override fun parseSolution(message: Message): Result<OmSolution> {
        return message.match(regex).flatMap { command ->
            findScoreIdentifier(command).and { findLink(command, message) }.flatMap { (scorePart, link) -> parseSolution(scorePart, link) }
        }
    }

    fun parseSolution(scoreIdentifier: ScoreIdentifier, link: String): Result<OmSolution> {
        val solution = try {
            SolutionParser.parse(ByteArrayInputStream(CUrl(link).exec()).asInput())
        } catch (e: Exception) {
            return Result.parseFailure("I could not parse your solution")
        }
        if (solution !is SolvedSolution) return Result.parseFailure("only solved solution are accepted")
        val puzzle = OmPuzzle.values().find { it.id == solution.puzzle } ?: return Result.parseFailure("I do not know the puzzle \"${solution.puzzle}\"")
        val parts = linkedMapOf(COST to solution.cost.toDouble(),
            CYCLES to solution.cycles.toDouble(),
            AREA to solution.area.toDouble(),
            INSTRUCTIONS to solution.instructions.toDouble())
        val score = when (scoreIdentifier) {
            is ScoreIdentifier.Part -> {
                parts[scoreIdentifier.scorePart] = scoreIdentifier.value
                OmScore(parts)
            }
            is ScoreIdentifier.Modifier -> OmScore(parts, scoreIdentifier.modifier)
            is ScoreIdentifier.Normal -> OmScore(parts)
        }
        return Result.success(OmSolution(puzzle, score, DslGenerator.toDsl(solution)))
    }

    private fun findLink(command: MatchResult, message: Message): Result<String> {
        return (command.groups["link"]?.value ?: message.attachments.firstOrNull()?.url)?.let { Result.success(it) }
            ?: Result.parseFailure("I could not find a valid link or attachment in your message.")
    }

    fun findScoreIdentifier(command: MatchResult, groupName: String = "score"): Result<ScoreIdentifier> {
        val scoreString = command.groups[groupName]!!.value
        if (scoreString == "?") return Result.success(ScoreIdentifier.Normal)
        if (scoreString.length == 1) OmModifier.values().find { it.key.toString() == scoreString }?.let { return Result.success(ScoreIdentifier.Modifier(it)) }
        OmScorePart.parse(scoreString)?.let { return Result.success(ScoreIdentifier.Part(it.first, it.second)) }
        return Result.parseFailure("I didn't understand \"$scoreString\".")
    }

    override val helpText: String = "<primary score(e.g. 3.5w or 45o) -or- ?(load from file)> <solution link>(or attach file to message)"
}

sealed class ScoreIdentifier {
    object Normal : ScoreIdentifier()
    class Modifier(val modifier: OmModifier) : ScoreIdentifier()
    class Part(val scorePart: OmScorePart, val value: Double) : ScoreIdentifier()
}