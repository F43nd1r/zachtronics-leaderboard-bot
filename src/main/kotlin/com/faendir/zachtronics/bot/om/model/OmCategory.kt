package com.faendir.zachtronics.bot.om.model

import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.om.model.OmMetric.*
import com.faendir.zachtronics.bot.om.model.OmModifier.OVERLAP
import com.faendir.zachtronics.bot.om.model.OmModifier.TRACKLESS
import com.faendir.zachtronics.bot.om.model.OmType.*


private val NORMAL_TYPES = setOf(NORMAL, INFINITE)
private val PRODUCTION_TYPES = setOf(PRODUCTION)

enum class OmCategory(
    private val supportedTypes: Set<OmType> = OmType.values().toSet(),
    vararg val metrics: OmMetric,
    val modifier: OmModifier = OmModifier.NORMAL,
    override val displayName: String = modifier.displayName + metrics.take(2).joinToString("") { it.displayName }
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

    OGC(NORMAL_TYPES, COST, CYCLES, AREA, modifier = OVERLAP),
    OGA(NORMAL_TYPES, COST, AREA, CYCLES, modifier = OVERLAP),
    OGX(NORMAL_TYPES, COST, PRODUCT_CA, modifier = OVERLAP),
    OCG(NORMAL_TYPES, CYCLES, COST, AREA, modifier = OVERLAP),
    OCA(NORMAL_TYPES, CYCLES, AREA, COST, modifier = OVERLAP),
    OCX(NORMAL_TYPES, CYCLES, PRODUCT_GA, modifier = OVERLAP),
    OAG(NORMAL_TYPES, AREA, COST, CYCLES, modifier = OVERLAP),
    OAC(NORMAL_TYPES, AREA, CYCLES, COST, modifier = OVERLAP),
    OAX(NORMAL_TYPES, AREA, PRODUCT_GC, modifier = OVERLAP),

    TIG(NORMAL_TYPES, INSTRUCTIONS, COST, PRODUCT_CA, modifier = TRACKLESS),
    TIC(NORMAL_TYPES, INSTRUCTIONS, CYCLES, PRODUCT_GA, modifier = TRACKLESS),
    TIA(NORMAL_TYPES, INSTRUCTIONS, AREA, PRODUCT_GC, modifier = TRACKLESS),

    IGNP(NORMAL_TYPES, INSTRUCTIONS, COST, PRODUCT_CA),
    ICNP(NORMAL_TYPES, INSTRUCTIONS, CYCLES, PRODUCT_GA),
    IANP(NORMAL_TYPES, INSTRUCTIONS, AREA, PRODUCT_GC),

    CINP(NORMAL_TYPES, CYCLES, INSTRUCTIONS, PRODUCT_GA),

    S4G(NORMAL_TYPES, SUM4, COST),
    S4C(NORMAL_TYPES, SUM4, CYCLES),
    S4A(NORMAL_TYPES, SUM4, AREA),
    S4I(NORMAL_TYPES, SUM4, INSTRUCTIONS),
    ;

    internal val requiredParts: Set<OmScorePart> = metrics.flatMap { it.scoreParts }.toSet()
    val scoreComparator: Comparator<OmScore> = metrics.map { it.comparator }.reduce { acc, comparator -> acc.thenComparing(comparator) }

    fun supportsPuzzle(puzzle: OmPuzzle) = supportedTypes.contains(puzzle.type)

    fun supportsScore(score: OmScore) = score.parts.keys.containsAll(requiredParts) && score.modifier <= modifier

    fun normalizeScore(score: OmScore): OmScore = OmScore(sortScoreParts(score.parts.asIterable()).map { it.key to it.value }, score.modifier)

    fun sortScoreParts(parts: Iterable<Map.Entry<OmScorePart, Double>>): Iterable<Map.Entry<OmScorePart, Double>> {
        check(parts.map { it.key }.containsAll(requiredParts))
        return requiredParts.map { scorePart -> parts.first { it.key == scorePart } }
    }
}
