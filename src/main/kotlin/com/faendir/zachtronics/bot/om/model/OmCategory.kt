package com.faendir.zachtronics.bot.om.model

import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.om.model.OmModifier.OVERLAP
import com.faendir.zachtronics.bot.om.model.OmModifier.TRACKLESS
import com.faendir.zachtronics.bot.om.model.OmScorePart.*
import com.faendir.zachtronics.bot.om.model.OmType.*


enum class OmCategory(
    val displayNames: List<String>,
    val primaryMetric: OmMetric,
    val tiebreaker: OmMetric,
    private val requiredParts: List<OmScorePart>,
    private val compare: OmCategory.(OmScore, OmScore) -> Int,
    private val supportedTypes: Set<OmType> = OmType.values().toSet(),
    private val modifier: OmModifier? = null
) : Category<OmScore, OmPuzzle> {
    GC(listOf("GC"), OmMetric.COST, OmMetric.CYCLES, listOf(COST, CYCLES, AREA), OmCategory::normalCompare, setOf(NORMAL, INFINITE)),
    GCP(listOf("GC"), OmMetric.COST, OmMetric.CYCLES, listOf(COST, CYCLES, INSTRUCTIONS), OmCategory::normalCompare, setOf(PRODUCTION)),
    GA(listOf("GA"), OmMetric.COST, OmMetric.AREA, listOf(COST, AREA, CYCLES), OmCategory::normalCompare, setOf(NORMAL, INFINITE)),
    GI(listOf("GI"), OmMetric.COST, OmMetric.INSTRUCTIONS, listOf(COST, INSTRUCTIONS, CYCLES), OmCategory::normalCompare, setOf(PRODUCTION)),
    GX(listOf("GX"), OmMetric.COST, OmMetric.PRODUCT, listOf(COST, CYCLES, AREA), OmCategory::lastTwoProductCompare, setOf(NORMAL, INFINITE)),
    GXP(listOf("GX"), OmMetric.COST, OmMetric.PRODUCT, listOf(COST, CYCLES, INSTRUCTIONS), OmCategory::lastTwoProductCompare, setOf(PRODUCTION)),
    CG(listOf("CG"), OmMetric.CYCLES, OmMetric.COST, listOf(CYCLES, COST, AREA), OmCategory::normalCompare, setOf(NORMAL, INFINITE)),
    CGP(listOf("CG"), OmMetric.CYCLES, OmMetric.COST, listOf(CYCLES, COST, INSTRUCTIONS), OmCategory::normalCompare, setOf(PRODUCTION)),
    CA(listOf("CA"), OmMetric.CYCLES, OmMetric.AREA, listOf(CYCLES, AREA, COST), OmCategory::normalCompare, setOf(NORMAL, INFINITE)),
    CI(listOf("CI"), OmMetric.CYCLES, OmMetric.INSTRUCTIONS, listOf(CYCLES, INSTRUCTIONS, COST), OmCategory::normalCompare, setOf(PRODUCTION)),
    CX(listOf("CX"), OmMetric.CYCLES, OmMetric.PRODUCT, listOf(CYCLES, COST, AREA), OmCategory::lastTwoProductCompare, setOf(NORMAL, INFINITE)),
    CXP(listOf("CX"), OmMetric.CYCLES, OmMetric.PRODUCT, listOf(CYCLES, COST, INSTRUCTIONS), OmCategory::lastTwoProductCompare, setOf(PRODUCTION)),
    AG(listOf("AG"), OmMetric.AREA, OmMetric.COST, listOf(AREA, COST, CYCLES), OmCategory::normalCompare, setOf(NORMAL, INFINITE)),
    AC(listOf("AC"), OmMetric.AREA, OmMetric.CYCLES, listOf(AREA, CYCLES, COST), OmCategory::normalCompare, setOf(NORMAL, INFINITE)),
    AX(listOf("AX"), OmMetric.AREA, OmMetric.PRODUCT, listOf(AREA, COST, CYCLES), OmCategory::lastTwoProductCompare, setOf(NORMAL, INFINITE)),
    IG(listOf("IG"), OmMetric.INSTRUCTIONS, OmMetric.COST, listOf(INSTRUCTIONS, COST, CYCLES), OmCategory::normalCompare, setOf(PRODUCTION)),
    IC(listOf("IC"), OmMetric.INSTRUCTIONS, OmMetric.CYCLES, listOf(INSTRUCTIONS, CYCLES, COST), OmCategory::normalCompare, setOf(PRODUCTION)),
    IX(listOf("IX"), OmMetric.INSTRUCTIONS, OmMetric.PRODUCT, listOf(INSTRUCTIONS, COST, CYCLES), OmCategory::lastTwoProductCompare, setOf(PRODUCTION)),
    SG(listOf("SUM-G", "SUM"), OmMetric.SUM, OmMetric.COST, listOf(COST, CYCLES, AREA), { s1, s2 -> sumCompare(s1, s2, COST) }, setOf(NORMAL, INFINITE)),
    SGP(listOf("SUM-G", "SUM"), OmMetric.SUM, OmMetric.COST, listOf(COST, CYCLES, INSTRUCTIONS), { s1, s2 -> sumCompare(s1, s2, COST) }, setOf(PRODUCTION)),
    SC(listOf("SUM-C", "SUM"), OmMetric.SUM, OmMetric.CYCLES, listOf(COST, CYCLES, AREA), { s1, s2 -> sumCompare(s1, s2, CYCLES) }, setOf(NORMAL, INFINITE)),
    SCP(listOf("SUM-C", "SUM"), OmMetric.SUM, OmMetric.CYCLES, listOf(COST, CYCLES, INSTRUCTIONS), { s1, s2 -> sumCompare(s1, s2, CYCLES) }, setOf(PRODUCTION)),
    SA(listOf("SUM-A", "SUM"), OmMetric.SUM, OmMetric.AREA, listOf(COST, CYCLES, AREA), { s1, s2 -> sumCompare(s1, s2, AREA) }, setOf(NORMAL, INFINITE)),
    SI(
        listOf("SUM-I", "SUM"),
        OmMetric.SUM,
        OmMetric.INSTRUCTIONS,
        listOf(COST, CYCLES, INSTRUCTIONS),
        { s1, s2 -> sumCompare(s1, s2, INSTRUCTIONS) },
        setOf(PRODUCTION)
    ),

    HEIGHT(
        listOf("Height", "H"),
        OmMetric.HEIGHT,
        OmMetric.CYCLES,
        listOf(OmScorePart.HEIGHT, CYCLES, COST),
        OmCategory::normalCompare,
        setOf(NORMAL, INFINITE)
    ),
    WIDTH(listOf("Width", "W"), OmMetric.Width, OmMetric.CYCLES, listOf(OmScorePart.WIDTH, CYCLES, COST), OmCategory::normalCompare, setOf(NORMAL)),

    OGC(listOf("OGC"), OmMetric.COST, OmMetric.CYCLES, listOf(COST, CYCLES, AREA), OmCategory::normalCompare, setOf(NORMAL, INFINITE), OVERLAP),
    OGA(listOf("OGA"), OmMetric.COST, OmMetric.AREA, listOf(COST, AREA, CYCLES), OmCategory::normalCompare, setOf(NORMAL, INFINITE), OVERLAP),
    OGX(listOf("OGX"), OmMetric.COST, OmMetric.PRODUCT, listOf(COST, CYCLES, AREA), OmCategory::lastTwoProductCompare, setOf(NORMAL, INFINITE), OVERLAP),
    OCG(listOf("OCG"), OmMetric.CYCLES, OmMetric.COST, listOf(CYCLES, COST, AREA), OmCategory::normalCompare, setOf(NORMAL, INFINITE), OVERLAP),
    OCA(listOf("OCA"), OmMetric.CYCLES, OmMetric.AREA, listOf(CYCLES, AREA, COST), OmCategory::normalCompare, setOf(NORMAL, INFINITE), OVERLAP),
    OCX(listOf("OCX"), OmMetric.CYCLES, OmMetric.PRODUCT, listOf(CYCLES, COST, AREA), OmCategory::lastTwoProductCompare, setOf(NORMAL, INFINITE), OVERLAP),
    OAG(listOf("OAG"), OmMetric.AREA, OmMetric.COST, listOf(AREA, COST, CYCLES), OmCategory::normalCompare, setOf(NORMAL, INFINITE), OVERLAP),
    OAC(listOf("OAC"), OmMetric.AREA, OmMetric.CYCLES, listOf(AREA, CYCLES, COST), OmCategory::normalCompare, setOf(NORMAL, INFINITE), OVERLAP),
    OAX(listOf("OAX"), OmMetric.AREA, OmMetric.PRODUCT, listOf(AREA, COST, CYCLES), OmCategory::lastTwoProductCompare, setOf(NORMAL, INFINITE), OVERLAP),

    TIG(
        listOf("TIG", "TI"),
        OmMetric.INSTRUCTIONS,
        OmMetric.COST,
        listOf(INSTRUCTIONS, COST, CYCLES, AREA),
        OmCategory::normalCompare,
        setOf(NORMAL, INFINITE),
        TRACKLESS
    ),
    TIC(
        listOf("TIC", "TI"),
        OmMetric.INSTRUCTIONS,
        OmMetric.CYCLES,
        listOf(INSTRUCTIONS, CYCLES, COST, AREA),
        OmCategory::normalCompare,
        setOf(NORMAL, INFINITE),
        TRACKLESS
    ),
    TIA(
        listOf("TIA", "TI"),
        OmMetric.INSTRUCTIONS,
        OmMetric.AREA,
        listOf(INSTRUCTIONS, AREA, COST, CYCLES),
        OmCategory::lastTwoProductCompare,
        setOf(NORMAL, INFINITE),
        TRACKLESS
    ),

    IGNP(listOf("IG"), OmMetric.INSTRUCTIONS, OmMetric.COST, listOf(INSTRUCTIONS, COST, CYCLES, AREA), OmCategory::normalCompare, setOf(NORMAL, INFINITE)),
    ICNP(listOf("IC"), OmMetric.INSTRUCTIONS, OmMetric.CYCLES, listOf(INSTRUCTIONS, CYCLES, COST, AREA), OmCategory::normalCompare, setOf(NORMAL, INFINITE)),
    IANP(
        listOf("IA"),
        OmMetric.INSTRUCTIONS,
        OmMetric.AREA,
        listOf(INSTRUCTIONS, AREA, COST, CYCLES),
        OmCategory::lastTwoProductCompare,
        setOf(NORMAL, INFINITE)
    ),

    CINP(listOf("CI"), OmMetric.CYCLES, OmMetric.INSTRUCTIONS, listOf(CYCLES, INSTRUCTIONS, COST, AREA), OmCategory::normalCompare, setOf(NORMAL, INFINITE)),

    S4G(
        listOf("SUM4-G", "SUM4"),
        OmMetric.SUM4,
        OmMetric.COST,
        listOf(COST, CYCLES, AREA, INSTRUCTIONS),
        { s1, s2 -> sumCompare(s1, s2, COST) },
        setOf(NORMAL, INFINITE)
    ),
    S4C(
        listOf("SUM4-C", "SUM4"),
        OmMetric.SUM4,
        OmMetric.CYCLES,
        listOf(COST, CYCLES, AREA, INSTRUCTIONS),
        { s1, s2 -> sumCompare(s1, s2, CYCLES) },
        setOf(NORMAL, INFINITE)
    ),
    S4A(
        listOf("SUM4-A", "SUM4"),
        OmMetric.SUM4,
        OmMetric.AREA,
        listOf(COST, CYCLES, AREA, INSTRUCTIONS),
        { s1, s2 -> sumCompare(s1, s2, AREA) },
        setOf(NORMAL, INFINITE)
    ),
    S4I(
        listOf("SUM4-I", "SUM4"),
        OmMetric.SUM4,
        OmMetric.INSTRUCTIONS,
        listOf(COST, CYCLES, AREA, INSTRUCTIONS),
        { s1, s2 -> sumCompare(s1, s2, INSTRUCTIONS) },
        setOf(NORMAL, INFINITE)
    ),
    ;

    override val displayName: String = displayNames.first()
    override val contentDescription: String = requiredParts.joinToString("/") { it.key.toString() }
    override val scoreComparator = Comparator<OmScore> { s1, s2 -> compare(normalizeScore(s1), normalizeScore(s2)) }

    override fun supportsPuzzle(puzzle: OmPuzzle) = supportedTypes.contains(puzzle.type)

    override fun supportsScore(score: OmScore) = score.parts.keys.containsAll(requiredParts) && modifier == score.modifier

    private fun normalCompare(s1: OmScore, s2: OmScore): Int = s1.parts.map { (part, value) -> value.compareTo(s2.parts[part]!!) }.firstOrNull { it != 0 } ?: 0

    private fun lastTwoProductCompare(s1: OmScore, s2: OmScore): Int = normalCompare(s1.transformToLastTwoProduct(), s2.transformToLastTwoProduct())

    private fun OmScore.transformToLastTwoProduct() =
        OmScore((parts.toList().dropLast(2) + parts.toList().takeLast(2).let { COMPUTED to it.first().second * it.last().second }))

    private fun sumCompare(s1: OmScore, s2: OmScore, tieBreaker: OmScorePart): Int =
        Comparator.comparingDouble<OmScore> { score -> score.parts.map { it.value }.sum() }.thenComparingDouble { it.parts[tieBreaker]!! }.compare(s1, s2)

    fun normalizeScore(score: OmScore): OmScore = OmScore(sortScoreParts(score.parts.asIterable()).map { it.key to it.value }, score.modifier)

    fun sortScoreParts(parts: Iterable<Map.Entry<OmScorePart, Double>>): Iterable<Map.Entry<OmScorePart, Double>> {
        check(parts.map { it.key }.containsAll(requiredParts))
        return requiredParts.map { scorePart -> parts.first { it.key == scorePart } }
    }
}
