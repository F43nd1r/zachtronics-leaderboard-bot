/*
 * Copyright (c) 2023
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

enum class OmScoreManifold(
    val displayName: String,
    vararg scorePartVararg: OmMetric.ScorePart<*>
) {
    /** GITO@0 + CAWH@V + L@INF */
    VICTORY(
        "@V",
        OmMetric.COST,
        OmMetric.CYCLES,
        OmMetric.AREA,
        OmMetric.INSTRUCTIONS,
        OmMetric.HEIGHT,
        OmMetric.WIDTH,
        OmMetric.OVERLAP,
        OmMetric.TRACKLESS,
        OmMetric.LOOPING,
    ),
    /** GITO@0 + RAWH@INF */
    INFINITY(
        "@∞",
        OmMetric.COST,
        OmMetric.RATE,
        OmMetric.AREA_INF,
        OmMetric.INSTRUCTIONS,
        OmMetric.HEIGHT_INF,
        OmMetric.WIDTH_INF,
        OmMetric.OVERLAP,
        OmMetric.TRACKLESS,
    ),
    ;

    /** sorted by the subscore ordering */
    val scoreParts = scorePartVararg.toList()

    /**
     *  list of metric-by-metric comparation results, with meaning:
     *  * `<0`: s1 is strictly better
     *  * `0`: equal
     *  * `>0`: s2 is strictly better
     */
    fun frontierCompare(s1: OmScore, s2: OmScore): List<Int> {
        return scoreParts.map { it.comparator.compare(s1, s2) }
    }
}