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
import com.faendir.om.parser.solution.model.part.Glyph
import com.faendir.om.parser.solution.model.part.GlyphType
import com.faendir.om.parser.solution.model.part.IO
import com.faendir.om.parser.solution.model.part.Track
import com.faendir.zachtronics.bot.om.model.OmCategory
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmRecord
import com.faendir.zachtronics.bot.om.model.OmScore
import com.faendir.zachtronics.bot.om.model.OmSubmission
import com.faendir.zachtronics.bot.repository.CategoryRecord
import com.faendir.zachtronics.bot.utils.ceil
import okio.buffer
import okio.sink
import okio.source
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.math.max
import kotlin.math.min


private val logger = LoggerFactory.getLogger("OM Utils")

fun Solution.isTrackless(): Boolean = parts.none { it is Track }

fun JNISolutionVerifier.getMetricSafe(metric: OmSimMetric) = try {
    getMetric(metric)
} catch (e: Exception) {
    logger.info("Verifier threw exception for $metric", e);
    null
}

fun SolvedSolution.getScore(verifier: JNISolutionVerifier) =
    OmScore(
        cost = verifier.getMetricSafe(OmSimMetric.COST).also {
            if (it != cost) throw IllegalArgumentException("Stored cost value does not match simulation. Run your solution to completion before submitting.")
        },
        cycles = verifier.getMetricSafe(OmSimMetric.CYCLES).also {
            if (it != cycles) throw IllegalArgumentException("Stored cycles value does not match simulation. Run your solution to completion before submitting.")
        },
        area = verifier.getMetricSafe(OmSimMetric.AREA_APPROXIMATE).also {
            if (it == null || min(it, area).toDouble() / max(it, area) < 0.95) {
                throw IllegalArgumentException("Stored area value does not match simulation. Run your solution to completion before submitting.")
            }
        },
        instructions = verifier.getMetricSafe(OmSimMetric.INSTRUCTIONS).also {
            if (it != instructions) throw IllegalArgumentException("Stored instructions value does not match simulation. Run your solution to completion before submitting.")
        },
        height = verifier.getMetricSafe(OmSimMetric.HEIGHT),
        width = verifier.getMetricSafe(OmSimMetric.WIDTH_TIMES_TWO)?.let { it.toDouble() / 2 },
        rate = verifier.getMetricSafe(OmSimMetric.THROUGHPUT_CYCLES)?.let {
            (it.toDouble() / (verifier.getMetricSafe(OmSimMetric.THROUGHPUT_OUTPUTS) ?: 0)).ceil(precision = 2)
        },
        trackless = isTrackless(),
        overlap = verifier.getMetricSafe(OmSimMetric.OVERLAP) != 0
    )

fun createSubmission(gif: String?, author: String, inputBytes: ByteArray): OmSubmission {
    val solution = try {
        SolutionParser.parse(ByteArrayInputStream(inputBytes).source().buffer())
    } catch (e: Exception) {
        throw IllegalArgumentException("I could not parse your solution")
    }
    if (solution !is SolvedSolution) {
        throw IllegalArgumentException("only solved solutions are accepted")
    }
    if (solution.parts.count { it is Arm && it.type == ArmType.VAN_BERLOS_WHEEL } > 1) {
        throw IllegalArgumentException("Multiple Van Berlo's Wheels are banned.")
    }
    if (solution.parts.count { it is Glyph && it.type == GlyphType.DISPOSAL } > 1) {
        throw IllegalArgumentException("Multiple Disposal glyphs are banned.")
    }
    if (solution.parts.filterIsInstance<IO>().groupBy { it.type to it.index }.values.any { it.size > 1 }) {
        throw IllegalArgumentException("Duplicated Inputs or Outputs are banned.")
    }
    val (puzzle, solutionBytes) = OmPuzzle.values().find { it.id == solution.puzzle }?.let { it to inputBytes }
        ?: OmPuzzle.values().find { it.altIds.contains(solution.puzzle) }?.let {
            solution.puzzle = it.id
            val out = ByteArrayOutputStream()
            SolutionParser.write(solution, out.sink().buffer())
            it to out.toByteArray()
        }
        ?: throw IllegalArgumentException("I do not know the puzzle \"${solution.puzzle}\"")
    val puzzleFile = puzzle.file
    JNISolutionVerifier.open(puzzleFile.readBytes(), solutionBytes).use { verifier ->
        if ((verifier.getMetricSafe(OmSimMetric.MAXIMUM_TRACK_GAP_POW_2) ?: 0) > 1) {
            throw IllegalArgumentException("Quantum Tracks are banned.")
        }
        if ((verifier.getMetricSafe(OmSimMetric.MAXIMUM_ABSOLUTE_ARM_ROTATION) ?: 0) >= 4096) {
            throw IllegalArgumentException("Maximum arm rotations over 4096 are banned.")
        }
        val gifStart = verifier.getMetricSafe(OmSimMetric.VISUAL_LOOP_START_CYCLE) ?: 0
        val gifEnd = (verifier.getMetricSafe(OmSimMetric.VISUAL_LOOP_END_CYCLE)
            ?: solution.cycles)
        return OmSubmission(
            puzzle,
            solution.getScore(verifier),
            author,
            gif,
            gifStart to gifEnd,
            solutionBytes
        )
    }
}

fun OmRecord.withCategory(category: OmCategory) = CategoryRecord(this, setOf(category))

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