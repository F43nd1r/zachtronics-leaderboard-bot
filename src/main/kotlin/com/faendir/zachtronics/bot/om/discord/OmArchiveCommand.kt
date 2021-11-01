/*
 * Copyright (c) 2021
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.faendir.zachtronics.bot.om.discord

import com.faendir.discord4j.command.annotation.ApplicationCommand
import com.faendir.discord4j.command.annotation.Converter
import com.faendir.discord4j.command.annotation.Description
import com.faendir.discord4j.command.parse.ApplicationCommandParser
import com.faendir.om.dsl.DslGenerator
import com.faendir.om.parser.solution.SolutionParser
import com.faendir.om.parser.solution.model.Position
import com.faendir.om.parser.solution.model.SolvedSolution
import com.faendir.zachtronics.bot.archive.Archive
import com.faendir.zachtronics.bot.discord.LinkConverter
import com.faendir.zachtronics.bot.discord.command.AbstractArchiveCommand
import com.faendir.zachtronics.bot.discord.command.Secured
import com.faendir.zachtronics.bot.om.OmQualifier
import com.faendir.zachtronics.bot.om.getWidthAndHeight
import com.faendir.zachtronics.bot.om.isOverlap
import com.faendir.zachtronics.bot.om.isTrackless
import com.faendir.zachtronics.bot.om.model.*
import com.faendir.zachtronics.bot.om.model.OmScorePart.*
import com.roxstudio.utils.CUrl
import discord4j.discordjson.json.ApplicationCommandOptionData
import okio.buffer
import okio.source
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.util.*

@Component
@OmQualifier
class OmArchiveCommand(override val archive: Archive<*, OmSolution>) : AbstractArchiveCommand<ArchiveParams, OmSolution>(),
    Secured by OmSecured,
    ApplicationCommandParser<ArchiveParams, ApplicationCommandOptionData> by ArchiveParamsParser {

    override fun parseSolutions(parameters: ArchiveParams): List<OmSolution> {
        return listOf(parseSolution(findScoreIdentifier(parameters), parameters.solution))
    }

    fun parseSolution(scoreIdentifier: ScoreIdentifier, link: String): OmSolution {
        val solution = try {
            SolutionParser.parse(ByteArrayInputStream(CUrl(link).exec()).source().buffer())
        } catch (e: Exception) {
            throw IllegalArgumentException("I could not parse your solution")
        }
        if (solution !is SolvedSolution) throw IllegalArgumentException("only solved solutions are accepted")
        val puzzle = OmPuzzle.values().find { it.id == solution.puzzle } ?: throw IllegalArgumentException("I do not know the puzzle \"${solution.puzzle}\"")
        val parts = linkedMapOf(
            COST to solution.cost.toDouble(),
            CYCLES to solution.cycles.toDouble(),
            AREA to solution.area.toDouble(),
            INSTRUCTIONS to solution.instructions.toDouble()
        )
        solution.getWidthAndHeight(puzzle)?.let { (width, height) ->
            width?.let { parts[WIDTH] = it }
            height?.let { parts[HEIGHT] = it }
        }
        val score = when (scoreIdentifier) {
            is ScoreIdentifier.Part -> {
                parts[scoreIdentifier.scorePart] = scoreIdentifier.value
                OmScore(parts)
            }
            is ScoreIdentifier.Normal -> OmScore(
                parts, when {
                    solution.isOverlap(puzzle) -> OmModifier.OVERLAP
                    solution.isTrackless() -> OmModifier.TRACKLESS
                    else -> OmModifier.NORMAL
                }
            )
        }
        return OmSolution(puzzle, score, DslGenerator.toDsl(solution))
    }

    fun findScoreIdentifier(archive: IArchive): ScoreIdentifier {
        return when {
            archive.score != null -> {
                OmScorePart.parse(archive.score!!)?.let { ScoreIdentifier.Part(it.first, it.second) }
                    ?: throw IllegalArgumentException("I didn't understand \"${archive.score}\".")
            }
            else -> {
                ScoreIdentifier.Normal
            }
        }
    }
}

sealed class ScoreIdentifier {
    object Normal : ScoreIdentifier()
    class Part(val scorePart: OmScorePart, val value: Double) : ScoreIdentifier()
}

interface IArchive {
    val solution: String
    val score: String?
}

@ApplicationCommand(name = "archive", description = "Archive a solution", subCommand = true)
data class ArchiveParams(
    @Converter(LinkConverter::class)
    @Description("Link to your solution file, can be `m1` to scrape it from your last message")
    override val solution: String,
    @Description("Score part for nonstandard metrics. E.g. `4h`, `3.5w`")
    override val score: String?
) : IArchive

infix operator fun Position.plus(other: Position) = Position(this.x + other.x, this.y + other.y)

data class CubicPosition(val x: Int, val y: Int, val z: Int) {

    fun rotate(times: Int): CubicPosition {
        val t = Math.floorMod(times, 6)
        val coords = mutableListOf(x, y, z)
        if (t % 2 != 0) {
            coords.replaceAll { -it }
        }
        Collections.rotate(coords, t % 3)
        return CubicPosition(coords[0], coords[1], coords[2])
    }

    fun toAxial(): Position = Position(x, z)
}

fun Position.toCubic(): CubicPosition = CubicPosition(x, -x - y, y)

fun Position.rotate(times: Int) = this.toCubic().rotate(times).toAxial()