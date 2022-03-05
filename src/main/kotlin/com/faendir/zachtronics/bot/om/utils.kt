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

import com.faendir.zachtronics.bot.om.model.OmCategory
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmRecord
import com.faendir.zachtronics.bot.om.model.OmScore
import com.faendir.zachtronics.bot.om.model.OmSubmission
import com.faendir.zachtronics.bot.repository.CategoryRecord
import com.faendir.zachtronics.bot.utils.ceil
import org.slf4j.LoggerFactory
import java.lang.Integer.max
import java.lang.Integer.min


private val logger = LoggerFactory.getLogger("OM Utils")

fun JNISolutionVerifier.getAllAvailableMetrics(): Map<JNISolutionVerifier.Metrics, Int?> =
    JNISolutionVerifier.Metrics.values().associateWith { metric ->
        try {
            getMetric(metric).takeIf { it != -1 }
        } catch (e: Exception) {
            logger.info("Verifier threw exception for $metric", e)
            null
        }
    }

fun Map<JNISolutionVerifier.Metrics, Int?>.toScore() =
    OmScore(
        cost = getValue(JNISolutionVerifier.Metrics.PARSED_COST).also {
            if (it != getValue(JNISolutionVerifier.Metrics.COST)) throw IllegalArgumentException("Stored cost value does not match simulation. Run your solution to completion before submitting.")
        },
        cycles = getValue(JNISolutionVerifier.Metrics.PARSED_CYCLES).also {
            if (it != getValue(JNISolutionVerifier.Metrics.CYCLES)) throw IllegalArgumentException("Stored cycles value does not match simulation. Run your solution to completion before submitting.")
        },
        area = getValue(JNISolutionVerifier.Metrics.PARSED_AREA).also {
            val approx = getValue(JNISolutionVerifier.Metrics.AREA_APPROXIMATE)
            if (it != null && approx != null && min(it, approx).toDouble() / max(it, approx) < 0.95) {
                throw IllegalArgumentException("Stored area value does not match simulation. Run your solution to completion before submitting.")
            }
        },
        areaAtInf = if (getValue(JNISolutionVerifier.Metrics.THROUGHPUT_WASTE) == 0) getValue(JNISolutionVerifier.Metrics.PARSED_AREA)?.toDouble() else Double.POSITIVE_INFINITY,
        instructions = getValue(JNISolutionVerifier.Metrics.PARSED_INSTRUCTIONS).also {
            if (it != getValue(JNISolutionVerifier.Metrics.INSTRUCTIONS)) throw IllegalArgumentException("Stored instructions value does not match simulation. Run your solution to completion before submitting.")
        },
        height = getValue(JNISolutionVerifier.Metrics.HEIGHT),
        width = getValue(JNISolutionVerifier.Metrics.WIDTH_TIMES_TWO)?.let { it.toDouble() / 2 },
        rate = getValue(JNISolutionVerifier.Metrics.THROUGHPUT_CYCLES)?.let {
            (it.toDouble() / (getValue(JNISolutionVerifier.Metrics.THROUGHPUT_OUTPUTS) ?: 0)).ceil(precision = 2)
        },
        trackless = getValue(JNISolutionVerifier.Metrics.PARTS_OF_TYPE_TRACK) == 0,
        overlap = getValue(JNISolutionVerifier.Metrics.OVERLAP) != 0
    )

fun createSubmission(gif: String?, author: String, solution: ByteArray): OmSubmission {
    val puzzleName = JNISolutionVerifier.getPuzzleNameFromSolution(solution) ?: throw IllegalArgumentException("Cannot parse your solution")
    val puzzle = OmPuzzle.values().find { it.id == puzzleName } ?: throw IllegalArgumentException("I do not know the puzzle \"${puzzleName}\"")
    val metrics = JNISolutionVerifier.open(puzzle.file.readBytes(), solution).use { it.getAllAvailableMetrics() }
    if ((metrics.getValue(JNISolutionVerifier.Metrics.PARTS_OF_TYPE_BARON) ?: 0) > 1) {
        throw IllegalArgumentException("Multiple Van Berlo's Wheels are banned.")
    }
    if ((metrics.getValue(JNISolutionVerifier.Metrics.PARTS_OF_TYPE_GLYPH_DISPOSAL) ?: 0) > 1) {
        throw IllegalArgumentException("Multiple Disposal glyphs are banned.")
    }
    if ((metrics.getValue(JNISolutionVerifier.Metrics.DUPLICATE_REAGENTS) ?: 0) > 1) {
        throw IllegalArgumentException("Duplicated Reagents are banned.")
    }
    if ((metrics.getValue(JNISolutionVerifier.Metrics.DUPLICATE_PRODUCTS) ?: 0) > 1) {
        throw IllegalArgumentException("Duplicated Products are banned.")
    }
    if ((metrics.getValue(JNISolutionVerifier.Metrics.MAXIMUM_TRACK_GAP_POW_2) ?: 0) > 1) {
        throw IllegalArgumentException("Quantum Tracks are banned.")
    }
    if ((metrics.getValue(JNISolutionVerifier.Metrics.MAXIMUM_ABSOLUTE_ARM_ROTATION) ?: 0) >= 4096) {
        throw IllegalArgumentException("Maximum arm rotations over 4096 are banned.")
    }
    return OmSubmission(puzzle, metrics.toScore(), author, gif, solution)
}

fun OmRecord.withCategory(category: OmCategory) = CategoryRecord(this, setOf(category))