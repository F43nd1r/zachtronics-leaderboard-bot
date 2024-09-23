/*
 * Copyright (c) 2024
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

package com.faendir.zachtronics.bot.om.validation

import com.faendir.om.parser.solution.SolutionParser
import com.faendir.om.parser.solution.model.SolvedSolution
import com.faendir.om.parser.solution.model.part.Arm
import com.faendir.om.parser.solution.model.part.ArmType
import com.faendir.om.parser.solution.model.part.Glyph
import com.faendir.om.parser.solution.model.part.GlyphType
import com.faendir.om.parser.solution.model.part.IO
import com.faendir.zachtronics.bot.om.JNISolutionVerifier
import com.faendir.zachtronics.bot.om.OmSimException
import com.faendir.zachtronics.bot.om.OmSimMetric
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmScore
import com.faendir.zachtronics.bot.om.model.OmSubmission
import com.faendir.zachtronics.bot.om.model.OmType
import com.faendir.zachtronics.bot.utils.InfinInt
import com.faendir.zachtronics.bot.utils.InfinInt.Companion.toInfinInt
import com.faendir.zachtronics.bot.utils.LevelValue
import okio.buffer
import okio.sink
import okio.source
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.math.ceil


private fun JNISolutionVerifier.getMetricSafe(metric: OmSimMetric) = try {
    getMetric(metric)
} catch (e: OmSimException) {
    null
}

private fun JNISolutionVerifier.getAreaINF(outputsAtINF: Int?): LevelValue? {
    if (outputsAtINF == null)
        return null
    val a2 = getApproximateMetric(OmSimMetric.PER_REPETITION_SQUARED_AREA)
    if (a2 != 0.0) return LevelValue(2, a2 / (outputsAtINF * outputsAtINF))
    val a1 = getMetric(OmSimMetric.PER_REPETITION_AREA)
    if (a1 != 0) return LevelValue(1, a1.toDouble() / outputsAtINF)
    val a0 = getMetric(OmSimMetric.STEADY_STATE(OmSimMetric.AREA))
    return LevelValue(0, a0.toDouble())
}

fun JNISolutionVerifier.getScore(type: OmType): OmScore {
    // null or 0 PER_REPETITION_OUTPUTS means the solution doesn't output infinite products, hence cannot have a @INF point
    val outputsAtINF: Int? = getMetricSafe(OmSimMetric.PER_REPETITION_OUTPUTS)?.takeIf { it != 0 }

    return OmScore(
        cost = getMetric(OmSimMetric.COST).also {
            if (it != getMetric(OmSimMetric.PARSED_COST))
                throw IllegalArgumentException("Stored cost value does not match simulation. Run your solution to completion before submitting.")
        },
        instructions = getMetric(OmSimMetric.INSTRUCTIONS).also {
            if (it != getMetric(OmSimMetric.PARSED_INSTRUCTIONS))
                throw IllegalArgumentException("Stored instructions value does not match simulation. Run your solution to completion before submitting.")
        },

        overlap = getMetric(OmSimMetric.OVERLAP) != 0,
        trackless = getMetric(OmSimMetric.NUMBER_OF_TRACK_SEGMENTS) == 0,

        cycles = getMetric(OmSimMetric.CYCLES).also {
            if (it != getMetric(OmSimMetric.PARSED_CYCLES))
                throw IllegalArgumentException("Stored cycles value does not match simulation. Run your solution to completion before submitting.")
        },
        area = getMetric(OmSimMetric.AREA).also {
            if (it != getMetric(OmSimMetric.PARSED_AREA))
                throw IllegalArgumentException("Stored area value does not match simulation. Run your solution to completion before submitting.")
        },
        height = if (type != OmType.PRODUCTION) getMetric(OmSimMetric.HEIGHT) else null,
        width = if (type == OmType.NORMAL) getMetric(OmSimMetric.WIDTH_TIMES_TWO).toDouble() / 2 else null,
        boundingHex = if (type == OmType.NORMAL) getMetric(OmSimMetric.MINIMUM_HEXAGON) else null,

        rate = outputsAtINF?.run {
            val precision = 100.0
            ceil((precision * getMetric(OmSimMetric.PER_REPETITION_CYCLES).toDouble() / this)) / precision
        },
        areaINF = getAreaINF(outputsAtINF),
        heightINF = if (outputsAtINF == null || type == OmType.PRODUCTION) null
        else getMetricSafe(OmSimMetric.STEADY_STATE(OmSimMetric.HEIGHT))?.toInfinInt() ?: InfinInt.INFINITY,
        widthINF = if (outputsAtINF == null || type != OmType.NORMAL) null
        else getMetricSafe(OmSimMetric.STEADY_STATE(OmSimMetric.WIDTH_TIMES_TWO))?.toDouble()?.div(2)
            ?: Double.POSITIVE_INFINITY,
        boundingHexINF = if (outputsAtINF == null || type != OmType.NORMAL) null
        else getMetricSafe(OmSimMetric.STEADY_STATE(OmSimMetric.MINIMUM_HEXAGON))?.toInfinInt() ?: InfinInt.INFINITY,
    )
}

fun createSubmission(gif: String?, author: String, inputBytes: ByteArray): OmSubmission {
    val solution = try {
        SolutionParser.parse(ByteArrayInputStream(inputBytes).source().buffer())
    } catch (e: Exception) {
        throw IllegalArgumentException("I could not parse your solution", e)
    }
    if (solution !is SolvedSolution) {
        throw IllegalArgumentException("Only solved solutions are accepted")
    }
    if (solution.instructions >= 65536) {
        throw IllegalArgumentException("Using more than 2^16 instructions is banned.")
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
    if (solution.parts.any { (it.position.x >= 65536) or (it.position.y >= 65536) }) {
        throw IllegalArgumentException("Parts farther than 2^16 from the origin are banned.")
    }
    val (puzzle, solutionBytes) = OmPuzzle.entries.find { it.id == solution.puzzle }?.let { it to inputBytes }
        ?: OmPuzzle.entries.find { it.altIds.contains(solution.puzzle) }?.let {
            solution.puzzle = it.id
            val out = ByteArrayOutputStream()
            SolutionParser.write(solution, out.sink().buffer(), writeSolved = true)
            it to out.toByteArray()
        }
        ?: throw IllegalArgumentException("I do not know the puzzle \"${solution.puzzle}\"")
    val puzzleFile = puzzle.file
    JNISolutionVerifier.open(puzzleFile.readBytes(), solutionBytes).use { verifier ->
        if (verifier.getMetric(OmSimMetric.MAXIMUM_TRACK_GAP_POW_2) > 1) {
            throw IllegalArgumentException("Quantum Tracks are banned.")
        }
        if (verifier.getMetric(OmSimMetric.MAXIMUM_ABSOLUTE_ARM_ROTATION) >= 4096) {
            throw IllegalArgumentException("Maximum arm rotations over 4096 are banned.")
        }
        return OmSubmission(
            puzzle,
            verifier.getScore(puzzle.type),
            author,
            gif,
            solutionBytes
        )
    }
}