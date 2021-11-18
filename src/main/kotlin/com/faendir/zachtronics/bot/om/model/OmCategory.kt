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

package com.faendir.zachtronics.bot.om.model

import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.om.model.OmMetric.AREA
import com.faendir.zachtronics.bot.om.model.OmMetric.COST
import com.faendir.zachtronics.bot.om.model.OmMetric.CYCLES
import com.faendir.zachtronics.bot.om.model.OmMetric.INSTRUCTIONS
import com.faendir.zachtronics.bot.om.model.OmMetric.OVERLAP
import com.faendir.zachtronics.bot.om.model.OmMetric.PRODUCT_CA
import com.faendir.zachtronics.bot.om.model.OmMetric.PRODUCT_CI
import com.faendir.zachtronics.bot.om.model.OmMetric.PRODUCT_GA
import com.faendir.zachtronics.bot.om.model.OmMetric.PRODUCT_GC
import com.faendir.zachtronics.bot.om.model.OmMetric.PRODUCT_GI
import com.faendir.zachtronics.bot.om.model.OmMetric.SUM3A
import com.faendir.zachtronics.bot.om.model.OmMetric.SUM3I
import com.faendir.zachtronics.bot.om.model.OmMetric.SUM4
import com.faendir.zachtronics.bot.om.model.OmMetric.TRACKLESS_INSTRUCTION
import com.faendir.zachtronics.bot.om.model.OmType.INFINITE
import com.faendir.zachtronics.bot.om.model.OmType.NORMAL
import com.faendir.zachtronics.bot.om.model.OmType.PRODUCTION


private val NORMAL_TYPES = setOf(NORMAL, INFINITE)
private val PRODUCTION_TYPES = setOf(PRODUCTION)

enum class OmCategory(
    private val supportedTypes: Set<OmType> = OmType.values().toSet(),
    vararg metricsVararg: OmMetric,
    override val displayName: String = metricsVararg.take(if (metricsVararg.first() is OmMetric.Modifier) 3 else 2).joinToString("") { it.displayName }
) : Category {
    GC(NORMAL_TYPES, COST, CYCLES, AREA),
    GCP(PRODUCTION_TYPES, COST, CYCLES, INSTRUCTIONS),
    GA(NORMAL_TYPES, COST, AREA, CYCLES),
    GI(PRODUCTION_TYPES, COST, INSTRUCTIONS, CYCLES),
    GX(NORMAL_TYPES, COST, PRODUCT_CA),
    GXP(PRODUCTION_TYPES, COST, PRODUCT_CI),
    CG(NORMAL_TYPES, CYCLES, COST, AREA),
    CGP(PRODUCTION_TYPES, CYCLES, COST, INSTRUCTIONS),
    CA(NORMAL_TYPES, CYCLES, AREA, COST),
    CI(PRODUCTION_TYPES, CYCLES, INSTRUCTIONS, COST),
    CX(NORMAL_TYPES, CYCLES, PRODUCT_GA),
    CXP(PRODUCTION_TYPES, CYCLES, PRODUCT_GI),
    AG(NORMAL_TYPES, AREA, COST, CYCLES),
    AC(NORMAL_TYPES, AREA, CYCLES, COST),
    AX(NORMAL_TYPES, AREA, PRODUCT_GC),
    IG(PRODUCTION_TYPES, INSTRUCTIONS, COST, CYCLES),
    IC(PRODUCTION_TYPES, INSTRUCTIONS, CYCLES, COST),
    IX(PRODUCTION_TYPES, INSTRUCTIONS, PRODUCT_GC),
    SG(NORMAL_TYPES, SUM3A, COST),
    SGP(PRODUCTION_TYPES, SUM3I, COST),
    SC(NORMAL_TYPES, SUM3A, CYCLES),
    SCP(PRODUCTION_TYPES, SUM3I, CYCLES),
    SA(NORMAL_TYPES, SUM3A, AREA),
    SI(PRODUCTION_TYPES, SUM3I, INSTRUCTIONS),

    HEIGHT(NORMAL_TYPES, OmMetric.HEIGHT, CYCLES, COST, displayName = "Height"),
    WIDTH(setOf(NORMAL), OmMetric.WIDTH, CYCLES, COST, displayName = "Width"),

    OGC(NORMAL_TYPES, OVERLAP, COST, CYCLES, AREA),
    OGA(NORMAL_TYPES, OVERLAP, COST, AREA, CYCLES),
    OGX(NORMAL_TYPES, OVERLAP, COST, PRODUCT_CA),
    OCG(NORMAL_TYPES, OVERLAP, CYCLES, COST, AREA),
    OCA(NORMAL_TYPES, OVERLAP, CYCLES, AREA, COST),
    OCX(NORMAL_TYPES, OVERLAP, CYCLES, PRODUCT_GA),
    OAG(NORMAL_TYPES, OVERLAP, AREA, COST, CYCLES),
    OAC(NORMAL_TYPES, OVERLAP, AREA, CYCLES, COST),
    OAX(NORMAL_TYPES, OVERLAP, AREA, PRODUCT_GC),

    TIG(NORMAL_TYPES, TRACKLESS_INSTRUCTION, COST, PRODUCT_CA),
    TIC(NORMAL_TYPES, TRACKLESS_INSTRUCTION, CYCLES, PRODUCT_GA),
    TIA(NORMAL_TYPES, TRACKLESS_INSTRUCTION, AREA, PRODUCT_GC),

    IGNP(NORMAL_TYPES, INSTRUCTIONS, COST, PRODUCT_CA),
    ICNP(NORMAL_TYPES, INSTRUCTIONS, CYCLES, PRODUCT_GA),
    IANP(NORMAL_TYPES, INSTRUCTIONS, AREA, PRODUCT_GC),

    CINP(NORMAL_TYPES, CYCLES, INSTRUCTIONS, PRODUCT_GA),

    S4G(NORMAL_TYPES, SUM4, COST),
    S4C(NORMAL_TYPES, SUM4, CYCLES),
    S4A(NORMAL_TYPES, SUM4, AREA),
    S4I(NORMAL_TYPES, SUM4, INSTRUCTIONS),
    ;

    override val metrics: List<OmMetric> = metricsVararg.toList()
    val requiredParts: List<OmScorePart> = metrics.flatMap { it.scoreParts }.distinct()
    val scoreComparator: Comparator<OmScore> = metrics.map { it.comparator }.reduce { acc, comparator -> acc.thenComparing(comparator) }

    fun supportsPuzzle(puzzle: OmPuzzle) = supportedTypes.contains(puzzle.type)

    fun supportsScore(score: OmScore) =
        requiredParts.all { it.getValue(score) != null } && (metrics.contains(OVERLAP) || !score.overlap) && (!metrics.contains(TRACKLESS_INSTRUCTION) || score.trackless)
}
