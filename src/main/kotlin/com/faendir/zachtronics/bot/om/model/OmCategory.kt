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

import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.om.model.OmMetric.*
import com.faendir.zachtronics.bot.om.model.OmScoreManifold.*
import com.faendir.zachtronics.bot.om.model.OmType.*


private val NORMAL_TYPES = setOf(NORMAL, POLYMER)
private val PRODUCTION_TYPES = setOf(PRODUCTION)

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
    vararg metricsVararg: OmMetric,
    override val displayName: String = metricsVararg.take(if (metricsVararg.first() is Modifier) 3 else 2).joinToString("") { it.displayName }
) : Category {
    GC(NORMAL_TYPES, COST, CYCLES, AREA),
    GA(NORMAL_TYPES, COST, AREA, CYCLES),
    GINP(NORMAL_TYPES, COST, INSTRUCTIONS, CYCLES),
    GX(NORMAL_TYPES, COST, PRODUCT_CA),
    GCP(PRODUCTION_TYPES, COST, CYCLES, INSTRUCTIONS),
    GI(PRODUCTION_TYPES, COST, INSTRUCTIONS, CYCLES),
    GXP(PRODUCTION_TYPES, COST, PRODUCT_CI),

    CG(NORMAL_TYPES, CYCLES, COST, AREA),
    CA(NORMAL_TYPES, CYCLES, AREA, COST),
    CX(NORMAL_TYPES, CYCLES, PRODUCT_GA),
    CINP(NORMAL_TYPES, CYCLES, INSTRUCTIONS, COST),
    CGP(PRODUCTION_TYPES, CYCLES, COST, INSTRUCTIONS),
    CI(PRODUCTION_TYPES, CYCLES, INSTRUCTIONS, COST),
    CXP(PRODUCTION_TYPES, CYCLES, PRODUCT_GI),

    AG(NORMAL_TYPES, AREA, COST, CYCLES),
    AC(NORMAL_TYPES, AREA, CYCLES, COST),
    AX(NORMAL_TYPES, AREA, PRODUCT_GC),
    AI(NORMAL_TYPES, AREA, INSTRUCTIONS, COST),

    IGNP(NORMAL_TYPES, INSTRUCTIONS, COST, CYCLES),
    ICNP(NORMAL_TYPES, INSTRUCTIONS, CYCLES, COST),
    IANP(NORMAL_TYPES, INSTRUCTIONS, AREA, COST),
    IG(PRODUCTION_TYPES, INSTRUCTIONS, COST, CYCLES),
    IC(PRODUCTION_TYPES, INSTRUCTIONS, CYCLES, COST),
    IX(PRODUCTION_TYPES, INSTRUCTIONS, PRODUCT_GC),

    SUM_G(NORMAL_TYPES, SUM3A, COST, displayName = "Sum"),
    SUM_GP(PRODUCTION_TYPES, SUM3I, COST, displayName = "Sum"),

    SUM4_G(NORMAL_TYPES, SUM4, COST, displayName = "Sum4"),

    HG(NORMAL_TYPES, HEIGHT, COST, CYCLES),
    HC(NORMAL_TYPES, HEIGHT, CYCLES, COST),
    WG(setOf(NORMAL), WIDTH, COST, CYCLES),
    HW(setOf(NORMAL), WIDTH, CYCLES, COST),

    OGC(NORMAL_TYPES, OVERLAP, COST, CYCLES, AREA),
    OCX(NORMAL_TYPES, OVERLAP, CYCLES, PRODUCT_GA),
    OAC(NORMAL_TYPES, OVERLAP, AREA, CYCLES, COST),
    OIC(NORMAL_TYPES, OVERLAP, INSTRUCTIONS, CYCLES, COST),

    TIG(NORMAL_TYPES, TRACKLESS_INSTRUCTION, COST, CYCLES),
    TIC(NORMAL_TYPES, TRACKLESS_INSTRUCTION, CYCLES, COST),
    TIA(NORMAL_TYPES, TRACKLESS_INSTRUCTION, AREA, COST),

    TG(NORMAL_TYPES, TRACKLESS, COST, CYCLES, displayName = "TG"),
    TC(NORMAL_TYPES, TRACKLESS, CYCLES, COST, displayName = "TC"),

    RG(NORMAL_TYPES, RATE, COST, AREA_INF),
    RA(NORMAL_TYPES, RATE, AREA_INF, COST, displayName = "RA"),
    RI(NORMAL_TYPES, RATE, INSTRUCTIONS, COST),

    HR(NORMAL_TYPES, HEIGHT_INF, RATE, COST, displayName = "HR"),
    WR(setOf(NORMAL), WIDTH_INF, RATE, COST, displayName = "WR"),

    OR(NORMAL_TYPES, OVERLAP, RATE, COST),
    ;

    override val metrics: List<OmMetric> = metricsVararg.toList()
    val requiredParts: Set<ScorePart<*>> = metrics.flatMapTo(HashSet()) { it.scoreParts }
    val associatedManifold = OmScoreManifold.entries.single { it.scoreParts.containsAll(requiredParts) }
    val scoreComparators: Map<OmType, Comparator<OmScore>> =
        DEFAULT_TIEBREAKERS[associatedManifold]!!.mapValues { (_, tiebreakers) ->
            (metrics + (tiebreakers - metrics)).map(OmMetric::comparator).reduce(Comparator<OmScore>::thenComparing)
        }

    fun supportsPuzzle(puzzle: OmPuzzle) = supportedTypes.contains(puzzle.type)

    fun supportsScore(score: OmScore) =
        (requiredParts.contains(OVERLAP) || !score.overlap) &&
                (!requiredParts.contains(TRACKLESS) || score.trackless) &&
                (!(requiredParts.contains(LOOPING) || associatedManifold == INFINITY) || score.looping)
}
