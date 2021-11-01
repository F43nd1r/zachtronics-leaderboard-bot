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

package com.faendir.zachtronics.bot.om

import com.faendir.om.parser.solution.SolutionParser
import com.faendir.om.parser.solution.model.Position
import com.faendir.om.parser.solution.model.Solution
import com.faendir.om.parser.solution.model.SolvedSolution
import com.faendir.om.parser.solution.model.part.Arm
import com.faendir.om.parser.solution.model.part.ArmType
import com.faendir.om.parser.solution.model.part.Conduit
import com.faendir.om.parser.solution.model.part.Glyph
import com.faendir.om.parser.solution.model.part.IO
import com.faendir.om.parser.solution.model.part.IOType
import com.faendir.om.parser.solution.model.part.Part
import com.faendir.om.parser.solution.model.part.Track
import com.faendir.zachtronics.bot.om.discord.plus
import com.faendir.zachtronics.bot.om.discord.rotate
import com.faendir.zachtronics.bot.om.model.FULL_CIRCLE
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmScore
import com.faendir.zachtronics.bot.om.model.OmScorePart
import com.faendir.zachtronics.bot.om.model.OmSubmission
import com.faendir.zachtronics.bot.om.model.SINGLE
import okio.buffer
import okio.sink
import okio.source
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.File


private val verifier = JNISolutionVerifier()
private val logger = LoggerFactory.getLogger("OM Utils")

fun Solution.getWidthAndHeight(puzzle: OmPuzzle): Pair<Double?, Double?>? {
    return getMetrics(puzzle, OmScorePart.WIDTH, OmScorePart.HEIGHT)?.let { it[OmScorePart.WIDTH] to it[OmScorePart.HEIGHT] }
}

fun Solution.getMetrics(puzzle: OmPuzzle, vararg metrics: OmScorePart): Map<OmScorePart, Double?>? {
    val puzzleFile = puzzle.file?.takeIf { it.exists() } ?: return null
    val solution = File.createTempFile(puzzle.id, ".solution").also { SolutionParser.write(this, it.outputStream().sink().buffer()) }
    return metrics.associateWith { metric ->
        try {
            when (metric) {
                OmScorePart.HEIGHT -> verifier.getHeight(puzzleFile, solution).takeIf { it != -1 }?.toDouble()
                OmScorePart.WIDTH -> verifier.getWidth(puzzleFile, solution).let {
                    when (it) {
                        -1 -> null
                        Int.MAX_VALUE -> Int.MAX_VALUE.toDouble()
                        else -> it.toDouble() / 2
                    }
                }
                OmScorePart.COST -> verifier.getCost(puzzleFile, solution).takeIf { it != -1 }?.toDouble()
                OmScorePart.CYCLES -> verifier.getCycles(puzzleFile, solution).takeIf { it != -1 }?.toDouble()
                OmScorePart.AREA -> verifier.getArea(puzzleFile, solution).takeIf { it != -1 }?.toDouble()
                OmScorePart.INSTRUCTIONS -> verifier.getInstructions(puzzleFile, solution).takeIf { it != -1 }?.toDouble()
            }
        } catch (e: Exception) {
            logger.info("Verifier threw exception", e)
            null
        }
    }
}

fun Solution.isTrackless(): Boolean = parts.none { it is Track }

fun Solution.isOverlap(puzzle: OmPuzzle): Boolean =
    parts.flatMapIndexed { index, part -> parts.subList(0, index).map { it to part } }.any { (p1, p2) -> puzzle.overlap(p1, p2) }


private fun OmPuzzle.overlap(p1: Part, p2: Part): Boolean {
    return when {
        p1 is Arm && p2 is Arm -> {
            val s1 = shape(p1)
            shape(p2).any { s1.contains(it) }
        }
        p1 is Arm && p2 is Track || p2 is Arm && p1 is Track -> false
        p1 is Arm -> shape(p2).contains(p1.position)
        p2 is Arm -> shape(p1).contains(p2.position)
        else -> {
            val s1 = shape(p1)
            shape(p2).any { s1.contains(it) }
        }
    }
}

private fun OmPuzzle.shape(part: Part): List<Position> {
    return when (part) {
        is Arm -> if (part.type == ArmType.VAN_BERLOS_WHEEL) {
            FULL_CIRCLE
        } else {
            SINGLE
        }
        is Conduit -> part.positions
        is Glyph -> part.type.shape
        is IO -> if (part.type == IOType.INPUT) {
            this.getReagentShape(part)
        } else {
            this.getProductShape(part)
        }
        is Track -> part.positions
        else -> throw IllegalArgumentException("Unknown part type ${part.name}")
    }.map { it.rotate(part.rotation) }.map { it + part.position }
}

fun createSubmission(gif: String, author: String, bytes: ByteArray): OmSubmission {
    val solution = try {
        SolutionParser.parse(ByteArrayInputStream(bytes).source().buffer())
    } catch (e: Exception) {
        throw IllegalArgumentException("I could not parse your solution")
    }
    if (solution !is SolvedSolution) throw IllegalArgumentException("only solved solutions are accepted")
    val puzzle = OmPuzzle.values().find { it.id == solution.puzzle } ?: throw IllegalArgumentException("I do not know the puzzle \"${solution.puzzle}\"")
    val (width, height) = solution.getWidthAndHeight(puzzle) ?: (null to null)
    val score = OmScore(
        cost = solution.cost,
        cycles = solution.cycles,
        area = solution.area,
        instructions = solution.instructions,
        height = height?.toInt(),
        width = width,
        trackless = solution.isTrackless(),
        overlap = solution.isOverlap(puzzle),
    )
    return OmSubmission(puzzle, score, author, gif, bytes)
}