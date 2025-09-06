/*
 * Copyright (c) 2025
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

package com.faendir.zachtronics.bot.om.model

import com.faendir.zachtronics.bot.om.model.MeasurePoint.*
import com.faendir.zachtronics.bot.om.model.OmMetric.*
import com.faendir.zachtronics.bot.om.model.OmType.*
import com.faendir.zachtronics.bot.utils.newEnumSet

private val FREESPACE_TYPES = setOf(NORMAL, POLYMER)

enum class OmScoreManifold(
    val displayName: String,
    val supportedTypes: Set<OmType>,
    val scoreParts: List<ScorePart<*>>
) {
    VICTORY_AREA("@aV", FREESPACE_TYPES, listOf(OVERLAP, COST, CYCLES, AREA, LOOPING, INSTRUCTIONS, TRACKLESS)),
    VICTORY_PROD("@iV", setOf(PRODUCTION), listOf(OVERLAP, COST, CYCLES, INSTRUCTIONS, LOOPING, AREA, TRACKLESS)),
    VICTORY_HEIGHT("@hV", FREESPACE_TYPES, listOf(OVERLAP, COST, CYCLES, HEIGHT, LOOPING, INSTRUCTIONS, TRACKLESS)),
    VICTORY_WIDTH("@wV", setOf(NORMAL), listOf(OVERLAP, COST, CYCLES, WIDTH, LOOPING, INSTRUCTIONS, TRACKLESS)),
    VICTORY_BHEX("@bV", setOf(NORMAL), listOf(OVERLAP, COST, CYCLES, BOUNDING_HEX, LOOPING, INSTRUCTIONS, TRACKLESS)),

    INFINITY_AREA("@a∞", FREESPACE_TYPES, listOf(OVERLAP, COST, RATE, AREA_INF, INSTRUCTIONS, TRACKLESS)),
    INFINITY_PROD("@i∞", setOf(PRODUCTION), listOf(OVERLAP, COST, RATE, INSTRUCTIONS, AREA_INF, TRACKLESS)),
    INFINITY_HEIGHT("@h∞", FREESPACE_TYPES, listOf(OVERLAP, COST, RATE, HEIGHT_INF, INSTRUCTIONS, TRACKLESS)),
    INFINITY_WIDTH("@w∞", setOf(NORMAL), listOf(OVERLAP, COST, RATE, WIDTH_INF, INSTRUCTIONS, TRACKLESS)),
    INFINITY_BHEX("@b∞", setOf(NORMAL), listOf(OVERLAP, COST, RATE, BOUNDING_HEX_INF, INSTRUCTIONS, TRACKLESS)),
    ;

    /** shown ingame and in gifs (kind of) */
    val ingameMetrics = scoreParts.subList(1, 4)
    /** rest of the metrics after [OVERLAP] and [ingameMetrics] */
    val tiebreakers = scoreParts.subList(4, scoreParts.size)
    /** Either [VICTORY] or [INFINITY] */
    val measurePoint: MeasurePoint = scoreParts.mapTo(newEnumSet(), ScorePart<*>::measurePoint).single { it != START }

    /**
     *  list of metric-by-metric comparation results, with meaning:
     *  * `<0`: s1 is strictly better
     *  * `0`: equal
     *  * `>0`: s2 is strictly better
     */
    fun frontierCompare(s1: OmScore, s2: OmScore): List<Int> {
        return scoreParts.map { it.comparator.compare(s1, s2) }
    }

    fun supportsScore(score: OmScore) = scoreParts.all { it.getValueFrom(score) != null }
}