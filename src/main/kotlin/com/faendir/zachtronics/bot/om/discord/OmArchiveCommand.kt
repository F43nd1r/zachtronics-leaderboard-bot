package com.faendir.zachtronics.bot.om.discord

import com.faendir.om.dsl.DslGenerator
import com.faendir.om.sp.SolutionParser
import com.faendir.om.sp.solution.SolvedSolution
import com.faendir.zachtronics.bot.generic.discord.AbstractArchiveCommand
import com.faendir.zachtronics.bot.model.Archive
import com.faendir.zachtronics.bot.om.model.OmScore
import com.faendir.zachtronics.bot.om.model.OmScorePart
import com.faendir.zachtronics.bot.om.model.OmScorePart.*
import com.faendir.zachtronics.bot.om.model.OmSolution
import com.faendir.zachtronics.bot.om.model.OpusMagnum
import com.faendir.zachtronics.bot.utils.Result
import com.faendir.zachtronics.bot.utils.and
import com.faendir.zachtronics.bot.utils.match
import com.roxstudio.utils.CUrl
import kotlinx.io.streams.asInput
import net.dv8tion.jda.api.entities.Message
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream

@Component
class OmArchiveCommand(archive: Archive<OmSolution>, private val opusMagnum: OpusMagnum) : AbstractArchiveCommand<OmSolution>(archive) {
    private val regex = Regex("(?<score>\\S+)\\s*(?<link>\\S+)?")

    override fun parseSolution(message: Message): Result<OmSolution> {
        return message.match(regex).flatMap { command ->
            findScorePart(command).and { findLink(command, message) }.flatMap handle@{ (scorePart, link) ->
                val solution = try {
                    SolutionParser.parse(ByteArrayInputStream(CUrl(link).exec()).asInput())
                } catch (e: Exception) {
                    return@handle Result.parseFailure("I could not parse your solution")
                }
                if (solution !is SolvedSolution) return@handle Result.parseFailure("only solved solution are accepted")
                opusMagnum.parsePuzzle(solution.puzzle).map { puzzle ->
                    val parts = linkedMapOf(COST to solution.cost.toDouble(),
                        CYCLES to solution.cycles.toDouble(),
                        AREA to solution.area.toDouble(),
                        INSTRUCTIONS to solution.instructions.toDouble())
                    if (scorePart != null) {
                        parts[scorePart.first] = scorePart.second
                        if(scorePart.first == OVERLAP_CYCLES) parts.remove(CYCLES)
                    }
                    OmSolution(puzzle, OmScore(parts), DslGenerator.toDsl(solution))
                }
            }
        }
    }

    private fun findLink(command: MatchResult, message: Message): Result<String> {
        return (command.groups["link"]?.value ?: message.attachments.firstOrNull()?.url)?.let { Result.success(it) }
            ?: Result.parseFailure("I could not find a valid link or attachment in your message.")
    }

    private fun findScorePart(command: MatchResult): Result<Pair<OmScorePart, Double>?> {
        val scoreString = command.groups["score"]!!.value
        if (scoreString == "#") return Result.success(null)
        return OmScorePart.parse(scoreString)?.let { Result.success(it) } ?: Result.parseFailure("I didn't understand \"$scoreString\".")
    }

    override val helpText: String = "<primary score(e.g. 3.5w or 45o) -or- #(load from file)> <solution link>(or attach file to message)"
}