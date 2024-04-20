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
private val ALL_TYPES = FREESPACE_TYPES + PRODUCTION

private val DEFAULT_TIEBREAKERS = mapOf(
    VICTORY to mapOf(
        NORMAL to listOf(OVERLAP, COST, CYCLES, AREA, LOOPING, INSTRUCTIONS, TRACKLESS, HEIGHT, WIDTH),
        POLYMER to listOf(OVERLAP, COST, CYCLES, AREA, LOOPING, INSTRUCTIONS, TRACKLESS, HEIGHT, WIDTH),
        PRODUCTION to listOf(OVERLAP, COST, CYCLES, INSTRUCTIONS, LOOPING, AREA, TRACKLESS),
    ),
    INFINITY to mapOf(
        NORMAL to listOf(OVERLAP, COST, RATE, AREA_INF, INSTRUCTIONS, TRACKLESS, HEIGHT_INF, WIDTH_INF),
        POLYMER to listOf(OVERLAP, COST, RATE, AREA_INF, INSTRUCTIONS, TRACKLESS, HEIGHT_INF),
        PRODUCTION to listOf(OVERLAP, COST, RATE, INSTRUCTIONS, AREA_INF, TRACKLESS),
    )
)

enum class OmCategory(
    override val supportedTypes: Set<OmType>,
    val associatedManifold: OmScoreManifold,
    private val admission: OmMetric<Boolean>,
    vararg metricsVararg: OmMetric<*>,
    override val displayName: String = admission.displayName + metricsVararg.joinToString("") { it.displayName },
) : Category {
    GC(ALL_TYPES, VICTORY, NOVERLAP, COST, CYCLES),
    GA(FREESPACE_TYPES, VICTORY, NOVERLAP, COST, AREA),
    GI(ALL_TYPES, VICTORY, NOVERLAP, COST, INSTRUCTIONS),
    GX(FREESPACE_TYPES, VICTORY, NOVERLAP, COST, PRODUCT_CA),
    GXP(setOf(PRODUCTION), VICTORY, NOVERLAP, COST, PRODUCT_CI),

    CG(ALL_TYPES, VICTORY, NOVERLAP, CYCLES, COST),
    CA(FREESPACE_TYPES, VICTORY, NOVERLAP, CYCLES, AREA),
    CI(ALL_TYPES, VICTORY, NOVERLAP, CYCLES, INSTRUCTIONS),
    CX(FREESPACE_TYPES, VICTORY, NOVERLAP, CYCLES, PRODUCT_GA),
    CXP(setOf(PRODUCTION), VICTORY, NOVERLAP, CYCLES, PRODUCT_GI),

    AG(FREESPACE_TYPES, VICTORY, NOVERLAP, AREA, COST),
    AC(FREESPACE_TYPES, VICTORY, NOVERLAP, AREA, CYCLES),
    AX(FREESPACE_TYPES, VICTORY, NOVERLAP, AREA, PRODUCT_GC),
    AI(FREESPACE_TYPES, VICTORY, NOVERLAP, AREA, INSTRUCTIONS),

    IG(ALL_TYPES, VICTORY, NOVERLAP, INSTRUCTIONS, COST),
    IC(ALL_TYPES, VICTORY, NOVERLAP, INSTRUCTIONS, CYCLES),
    IA(FREESPACE_TYPES, VICTORY, NOVERLAP, INSTRUCTIONS, AREA),
    IX(setOf(PRODUCTION), VICTORY, NOVERLAP, INSTRUCTIONS, PRODUCT_GC),

    SUM_G(FREESPACE_TYPES, VICTORY, NOVERLAP, SUM3A, COST, displayName = "Sum"),
    SUM_GP(setOf(PRODUCTION), VICTORY, NOVERLAP, SUM3I, COST, displayName = "Sum"),

    SUM4_G(FREESPACE_TYPES, VICTORY, NOVERLAP, SUM4, COST, displayName = "Sum4"),

    HG(FREESPACE_TYPES, VICTORY, NOVERLAP, HEIGHT, COST),
    HC(FREESPACE_TYPES, VICTORY, NOVERLAP, HEIGHT, CYCLES),
    WG(setOf(NORMAL), VICTORY, NOVERLAP, WIDTH, COST),
    WC(setOf(NORMAL), VICTORY, NOVERLAP, WIDTH, CYCLES),

    OGC(FREESPACE_TYPES, VICTORY, ANYTHING_GOES, COST, CYCLES),
    OCX(FREESPACE_TYPES, VICTORY, ANYTHING_GOES, CYCLES, PRODUCT_GA),
    OAC(FREESPACE_TYPES, VICTORY, ANYTHING_GOES, AREA, CYCLES),
    OIC(FREESPACE_TYPES, VICTORY, ANYTHING_GOES, INSTRUCTIONS, CYCLES),

    TIG(FREESPACE_TYPES, VICTORY, NOVERLAP_TRACKLESS, INSTRUCTIONS, COST),
    TIC(FREESPACE_TYPES, VICTORY, NOVERLAP_TRACKLESS, INSTRUCTIONS, CYCLES),
    TIA(FREESPACE_TYPES, VICTORY, NOVERLAP_TRACKLESS, INSTRUCTIONS, AREA),

    TG(FREESPACE_TYPES, VICTORY, NOVERLAP_TRACKLESS, COST),
    TC(FREESPACE_TYPES, VICTORY, NOVERLAP_TRACKLESS, CYCLES),

    GR(ALL_TYPES, INFINITY, NOVERLAP, COST, RATE),

    RG(ALL_TYPES, INFINITY, NOVERLAP, RATE, COST),
    RA(FREESPACE_TYPES, INFINITY, NOVERLAP, RATE, AREA_INF),
    RI(ALL_TYPES, INFINITY, NOVERLAP, RATE, INSTRUCTIONS),

    HR(FREESPACE_TYPES, INFINITY, NOVERLAP, HEIGHT_INF, RATE),
    WR(setOf(NORMAL), INFINITY, NOVERLAP, WIDTH_INF, RATE),

    ORG(FREESPACE_TYPES, INFINITY, ANYTHING_GOES, RATE, COST),

    TIR(FREESPACE_TYPES, INFINITY, NOVERLAP_TRACKLESS, INSTRUCTIONS, RATE),
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
