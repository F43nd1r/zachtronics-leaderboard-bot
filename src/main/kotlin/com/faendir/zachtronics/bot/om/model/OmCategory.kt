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


enum class OmCategory(
    val manifold: OmScoreManifold,
    private val admission: OmMetric<Boolean>,
    vararg metricsVararg: OmMetric<*>,
    override val displayName: String = admission.displayName + metricsVararg.take(2).joinToString("") { it.displayName },
) : Category {
    GC(VICTORY_AREA, NOVERLAP, COST, CYCLES, AREA),
    GC_P(VICTORY_PROD, NOVERLAP, COST, CYCLES, INSTRUCTIONS),
    GA(VICTORY_AREA, NOVERLAP, COST, AREA, CYCLES),
    GI(VICTORY_AREA, NOVERLAP, COST, INSTRUCTIONS, CYCLES, AREA),
    GI_P(VICTORY_PROD, NOVERLAP, COST, INSTRUCTIONS, CYCLES),
    GX(VICTORY_AREA, NOVERLAP, COST, PRODUCT_CA),
    GX_P(VICTORY_PROD, NOVERLAP, COST, PRODUCT_CI),

    CG(VICTORY_AREA, NOVERLAP, CYCLES, COST, AREA),
    CG_P(VICTORY_PROD, NOVERLAP, CYCLES, COST, INSTRUCTIONS),
    CA(VICTORY_AREA, NOVERLAP, CYCLES, AREA, COST),
    CI(VICTORY_AREA, NOVERLAP, CYCLES, INSTRUCTIONS, COST, AREA),
    CI_P(VICTORY_PROD, NOVERLAP, CYCLES, INSTRUCTIONS, COST),
    CX(VICTORY_AREA, NOVERLAP, CYCLES, PRODUCT_GA),
    CX_P(VICTORY_PROD, NOVERLAP, CYCLES, PRODUCT_GI),

    AG(VICTORY_AREA, NOVERLAP, AREA, COST, CYCLES),
    AC(VICTORY_AREA, NOVERLAP, AREA, CYCLES, COST),
    AX(VICTORY_AREA, NOVERLAP, AREA, PRODUCT_GC),
    AI(VICTORY_AREA, NOVERLAP, AREA, INSTRUCTIONS, COST, CYCLES),

    IG(VICTORY_AREA, NOVERLAP, INSTRUCTIONS, COST, CYCLES, AREA),
    IG_P(VICTORY_PROD, NOVERLAP, INSTRUCTIONS, COST, CYCLES),
    IC(VICTORY_AREA, NOVERLAP, INSTRUCTIONS, CYCLES, COST, AREA),
    IC_P(VICTORY_PROD, NOVERLAP, INSTRUCTIONS, CYCLES, COST),
    IA(VICTORY_AREA, NOVERLAP, INSTRUCTIONS, AREA, COST, CYCLES),
    IX_P(VICTORY_PROD, NOVERLAP, INSTRUCTIONS, PRODUCT_GC),

    SUM(VICTORY_AREA, NOVERLAP, SUM3A, displayName = "Sum"),
    SUM_P(VICTORY_PROD, NOVERLAP, SUM3I, displayName = "Sum"),

    SUM4(VICTORY_AREA, NOVERLAP, OmMetric.SUM4, displayName = "Sum4"),

    HG(VICTORY_HEIGHT, NOVERLAP, HEIGHT, COST, CYCLES),
    HC(VICTORY_HEIGHT, NOVERLAP, HEIGHT, CYCLES, COST),
    WG(VICTORY_WIDTH, NOVERLAP, WIDTH, COST, CYCLES),
    WC(VICTORY_WIDTH, NOVERLAP, WIDTH, CYCLES, COST),
    BG(VICTORY_BHEX, NOVERLAP, BOUNDING_HEX, COST, CYCLES),
    BC(VICTORY_BHEX, NOVERLAP, BOUNDING_HEX, CYCLES, COST),

    OGC(VICTORY_AREA, ANYTHING_GOES, COST, CYCLES, AREA, NOVERLAP),
    OCX(VICTORY_AREA, ANYTHING_GOES, CYCLES, PRODUCT_GA, NOVERLAP),
    OAC(VICTORY_AREA, ANYTHING_GOES, AREA, CYCLES, COST, NOVERLAP),
    OIC(VICTORY_AREA, ANYTHING_GOES, INSTRUCTIONS, CYCLES, COST, AREA, NOVERLAP),

    OGC_P(VICTORY_PROD, ANYTHING_GOES, COST, CYCLES, INSTRUCTIONS, NOVERLAP),
    OCX_P(VICTORY_PROD, ANYTHING_GOES, CYCLES, PRODUCT_GI, NOVERLAP),
    OIC_P(VICTORY_PROD, ANYTHING_GOES, INSTRUCTIONS, CYCLES, COST, NOVERLAP),

    TIG(VICTORY_AREA, NOVERLAP_TRACKLESS, INSTRUCTIONS, COST, CYCLES, AREA),
    TIC(VICTORY_AREA, NOVERLAP_TRACKLESS, INSTRUCTIONS, CYCLES, COST, AREA),
    TIA(INFINITY_AREA, NOVERLAP_TRACKLESS, INSTRUCTIONS, AREA_INF, COST, RATE),

    TG(VICTORY_AREA, NOVERLAP_TRACKLESS, COST, CYCLES, AREA, displayName = "TG"),
    TC(VICTORY_AREA, NOVERLAP_TRACKLESS, CYCLES, COST, AREA, displayName = "TC"),

    RG(INFINITY_AREA, NOVERLAP, RATE, COST, AREA_INF),
    RG_P(INFINITY_PROD, NOVERLAP, RATE, COST, INSTRUCTIONS),
    RA(INFINITY_AREA, NOVERLAP, RATE, AREA_INF, COST),
    RI(INFINITY_AREA, NOVERLAP, RATE, INSTRUCTIONS, COST, AREA_INF),
    RI_P(INFINITY_PROD, NOVERLAP, RATE, INSTRUCTIONS, COST),

    HR(INFINITY_HEIGHT, NOVERLAP, HEIGHT_INF, RATE, COST),
    WR(INFINITY_WIDTH, NOVERLAP, WIDTH_INF, RATE, COST),
    BR(INFINITY_BHEX, NOVERLAP, BOUNDING_HEX_INF, RATE, COST),

    ORG(INFINITY_AREA, ANYTHING_GOES, RATE, COST, AREA_INF, NOVERLAP),
    ORX(INFINITY_AREA, ANYTHING_GOES, RATE, PRODUCT_GI, AREA_INF, NOVERLAP),

    ORG_P(INFINITY_PROD, ANYTHING_GOES, RATE, COST, INSTRUCTIONS, NOVERLAP),
    ORX_P(INFINITY_PROD, ANYTHING_GOES, RATE, PRODUCT_GI, NOVERLAP),
    ;

    override val supportedTypes: Set<OmType> by manifold::supportedTypes
    override val metrics: List<OmMetric<*>> = listOf(admission) + metricsVararg
    val requiredParts: Set<ScorePart<*>> = metrics.flatMapTo(HashSet()) { it.scoreParts }
    val scoreComparator: Comparator<OmScore> =
        (metrics + (manifold.scoreParts - metrics)).map(OmMetric<*>::comparator).reduce(Comparator<OmScore>::thenComparing)

    fun supportsPuzzle(puzzle: OmPuzzle) = supportedTypes.contains(puzzle.type)

    fun supportsScore(score: OmScore) =
        manifold.supportsScore(score) && admission.getValueFrom(score) == true
}
