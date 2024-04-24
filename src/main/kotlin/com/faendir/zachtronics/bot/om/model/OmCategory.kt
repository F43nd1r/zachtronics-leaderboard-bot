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

package com.faendir.zachtronics.bot.om.model

import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.om.model.OmMetric.*
import com.faendir.zachtronics.bot.om.model.OmScoreManifold.*
import com.faendir.zachtronics.bot.om.model.OmType.*


private val FREESPACE_TYPES = setOf(NORMAL, POLYMER)

/** shown ingame and in gifs (kind of) */
internal val INGAME_METRICS = mapOf(
    VICTORY to mapOf(
        NORMAL to listOf(COST, CYCLES, AREA),
        POLYMER to listOf(COST, CYCLES, AREA),
        PRODUCTION to listOf(COST, CYCLES, INSTRUCTIONS),
    ),
    INFINITY to mapOf(
        NORMAL to listOf(COST, RATE, AREA_INF),
        POLYMER to listOf(COST, RATE, AREA_INF),
        PRODUCTION to listOf(COST, RATE, INSTRUCTIONS),
    )
)

/** after [OVERLAP] and the [INGAME_METRICS] */
private val DEFAULT_TIEBREAKERS = mapOf(
    VICTORY to mapOf(
        NORMAL to listOf(LOOPING, INSTRUCTIONS, TRACKLESS, HEIGHT, WIDTH),
        POLYMER to listOf(LOOPING, INSTRUCTIONS, TRACKLESS, HEIGHT, WIDTH),
        PRODUCTION to listOf(LOOPING, AREA, TRACKLESS),
    ),
    INFINITY to mapOf(
        NORMAL to listOf(INSTRUCTIONS, TRACKLESS, HEIGHT_INF, WIDTH_INF),
        POLYMER to listOf(INSTRUCTIONS, TRACKLESS, HEIGHT_INF),
        PRODUCTION to listOf(AREA_INF, TRACKLESS),
    )
)

enum class OmCategory(
    override val supportedTypes: Set<OmType>,
    val associatedManifold: OmScoreManifold,
    private val admission: OmMetric<Boolean>,
    vararg metricsVararg: OmMetric<*>,
    override val displayName: String = admission.displayName + metricsVararg.joinToString("") { it.displayName },
) : Category {
    GC(FREESPACE_TYPES, VICTORY, NOVERLAP, COST, CYCLES, AREA),
    GC_P(setOf(PRODUCTION), VICTORY, NOVERLAP, COST, CYCLES, INSTRUCTIONS),
    GA(FREESPACE_TYPES, VICTORY, NOVERLAP, COST, AREA, CYCLES),
    GI(FREESPACE_TYPES, VICTORY, NOVERLAP, COST, INSTRUCTIONS, CYCLES, AREA),
    GI_P(setOf(PRODUCTION), VICTORY, NOVERLAP, COST, INSTRUCTIONS, CYCLES),
    GX(FREESPACE_TYPES, VICTORY, NOVERLAP, COST, PRODUCT_CA),
    GX_P(setOf(PRODUCTION), VICTORY, NOVERLAP, COST, PRODUCT_CI),

    CG(FREESPACE_TYPES, VICTORY, NOVERLAP, CYCLES, COST, AREA),
    CG_P(setOf(PRODUCTION), VICTORY, NOVERLAP, CYCLES, COST, INSTRUCTIONS),
    CA(FREESPACE_TYPES, VICTORY, NOVERLAP, CYCLES, AREA, COST),
    CI(FREESPACE_TYPES, VICTORY, NOVERLAP, CYCLES, INSTRUCTIONS, COST, AREA),
    CI_P(setOf(PRODUCTION), VICTORY, NOVERLAP, CYCLES, INSTRUCTIONS, COST),
    CX(FREESPACE_TYPES, VICTORY, NOVERLAP, CYCLES, PRODUCT_GA),
    CX_P(setOf(PRODUCTION), VICTORY, NOVERLAP, CYCLES, PRODUCT_GI),

    AG(FREESPACE_TYPES, VICTORY, NOVERLAP, AREA, COST, CYCLES),
    AC(FREESPACE_TYPES, VICTORY, NOVERLAP, AREA, CYCLES, COST),
    AX(FREESPACE_TYPES, VICTORY, NOVERLAP, AREA, PRODUCT_GC),
    AI(FREESPACE_TYPES, VICTORY, NOVERLAP, AREA, INSTRUCTIONS, COST, CYCLES),

    IG(FREESPACE_TYPES, VICTORY, NOVERLAP, INSTRUCTIONS, COST, CYCLES, AREA),
    IG_P(setOf(PRODUCTION), VICTORY, NOVERLAP, INSTRUCTIONS, COST, CYCLES),
    IC(FREESPACE_TYPES, VICTORY, NOVERLAP, INSTRUCTIONS, CYCLES, COST, AREA),
    IC_P(setOf(PRODUCTION), VICTORY, NOVERLAP, INSTRUCTIONS, CYCLES, COST),
    IA(FREESPACE_TYPES, VICTORY, NOVERLAP, INSTRUCTIONS, AREA, COST, CYCLES),
    IX_P(setOf(PRODUCTION), VICTORY, NOVERLAP, INSTRUCTIONS, PRODUCT_GC),

    SUM(FREESPACE_TYPES, VICTORY, NOVERLAP, SUM3A, displayName = "Sum"),
    SUM_P(setOf(PRODUCTION), VICTORY, NOVERLAP, SUM3I, displayName = "Sum"),

    SUM4(FREESPACE_TYPES, VICTORY, NOVERLAP, OmMetric.SUM4, displayName = "Sum4"),

    HG(FREESPACE_TYPES, VICTORY, NOVERLAP, HEIGHT, COST, CYCLES, AREA),
    HC(FREESPACE_TYPES, VICTORY, NOVERLAP, HEIGHT, CYCLES, COST, AREA),
    WG(setOf(NORMAL), VICTORY, NOVERLAP, WIDTH, COST, CYCLES, AREA),
    WC(setOf(NORMAL), VICTORY, NOVERLAP, WIDTH, CYCLES, COST, AREA),

    OGC(FREESPACE_TYPES, VICTORY, ANYTHING_GOES, COST, CYCLES, AREA, OVERLAP),
    OCX(FREESPACE_TYPES, VICTORY, ANYTHING_GOES, CYCLES, PRODUCT_GA, OVERLAP),
    OAC(FREESPACE_TYPES, VICTORY, ANYTHING_GOES, AREA, CYCLES, COST, OVERLAP),
    OIC(FREESPACE_TYPES, VICTORY, ANYTHING_GOES, INSTRUCTIONS, CYCLES, COST, AREA, OVERLAP),

    TIG(FREESPACE_TYPES, VICTORY, NOVERLAP_TRACKLESS, INSTRUCTIONS, COST, CYCLES, AREA),
    TIC(FREESPACE_TYPES, VICTORY, NOVERLAP_TRACKLESS, INSTRUCTIONS, CYCLES, COST, AREA),
    TIA(FREESPACE_TYPES, VICTORY, NOVERLAP_TRACKLESS, INSTRUCTIONS, AREA, COST, CYCLES),

    TG(FREESPACE_TYPES, VICTORY, NOVERLAP_TRACKLESS, COST, CYCLES, AREA),
    TC(FREESPACE_TYPES, VICTORY, NOVERLAP_TRACKLESS, CYCLES, COST, AREA),

    RG(FREESPACE_TYPES, INFINITY, NOVERLAP, RATE, COST, AREA_INF),
    RG_P(setOf(PRODUCTION), INFINITY, NOVERLAP, RATE, COST, INSTRUCTIONS),
    RA(FREESPACE_TYPES, INFINITY, NOVERLAP, RATE, AREA_INF, COST),
    RI(FREESPACE_TYPES, INFINITY, NOVERLAP, RATE, INSTRUCTIONS, COST, AREA_INF),
    RI_P(setOf(PRODUCTION), INFINITY, NOVERLAP, RATE, INSTRUCTIONS, COST),

    HR(FREESPACE_TYPES, INFINITY, NOVERLAP, HEIGHT_INF, RATE, COST, AREA_INF),
    WR(setOf(NORMAL), INFINITY, NOVERLAP, WIDTH_INF, RATE, COST, AREA_INF),

    ORG(FREESPACE_TYPES, INFINITY, ANYTHING_GOES, RATE, COST, AREA_INF, OVERLAP),
    ;

    override val metrics: List<OmMetric<*>> = listOf(admission) + metricsVararg
    val requiredParts: Set<ScorePart<*>> = metrics.flatMapTo(HashSet()) { it.scoreParts }
    val scoreComparators: Map<OmType, Comparator<OmScore>> =
        DEFAULT_TIEBREAKERS[associatedManifold]!!.mapValues { (_, tiebreakers) ->
            (metrics + (tiebreakers - metrics)).map(OmMetric<*>::comparator).reduce(Comparator<OmScore>::thenComparing)
        }

    fun supportsPuzzle(puzzle: OmPuzzle) = supportedTypes.contains(puzzle.type)

    fun supportsScore(score: OmScore) =
        score.manifolds.contains(associatedManifold) && admission.getValueFrom(score) == true
}
