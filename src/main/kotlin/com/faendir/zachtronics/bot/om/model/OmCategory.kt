package com.faendir.zachtronics.bot.om.model

import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.om.model.OmModifier.OVERLAP
import com.faendir.zachtronics.bot.om.model.OmModifier.TRACKLESS
import com.faendir.zachtronics.bot.om.model.OmScorePart.*
import com.faendir.zachtronics.bot.om.model.OmType.*


enum class OmCategory(
    override val displayName: String,
    val primaryMetric: OmMetric,
    val tiebreaker: OmMetric,
    private val requiredParts: List<OmScorePart>,
    comparator: Comparator<OmScore>,
    private val supportedTypes: Set<OmType> = OmType.values().toSet(),
    val modifier: OmModifier? = null
) : Category {
    GC("GC", OmMetric.COST, OmMetric.CYCLES, listOf(COST, CYCLES, AREA), normalComparator(), setOf(NORMAL, INFINITE)),
    GCP("GC", OmMetric.COST, OmMetric.CYCLES, listOf(COST, CYCLES, INSTRUCTIONS), normalComparator(), setOf(PRODUCTION)),
    GA("GA", OmMetric.COST, OmMetric.AREA, listOf(COST, AREA, CYCLES), normalComparator(), setOf(NORMAL, INFINITE)),
    GI("GI", OmMetric.COST, OmMetric.INSTRUCTIONS, listOf(COST, INSTRUCTIONS, CYCLES), normalComparator(), setOf(PRODUCTION)),
    GX("GX", OmMetric.COST, OmMetric.PRODUCT, listOf(COST, CYCLES, AREA), lastTwoProductComparator(), setOf(NORMAL, INFINITE)),
    GXP("GX", OmMetric.COST, OmMetric.PRODUCT, listOf(COST, CYCLES, INSTRUCTIONS), lastTwoProductComparator(), setOf(PRODUCTION)),
    CG("CG", OmMetric.CYCLES, OmMetric.COST, listOf(CYCLES, COST, AREA), normalComparator(), setOf(NORMAL, INFINITE)),
    CGP("CG", OmMetric.CYCLES, OmMetric.COST, listOf(CYCLES, COST, INSTRUCTIONS), normalComparator(), setOf(PRODUCTION)),
    CA("CA", OmMetric.CYCLES, OmMetric.AREA, listOf(CYCLES, AREA, COST), normalComparator(), setOf(NORMAL, INFINITE)),
    CI("CI", OmMetric.CYCLES, OmMetric.INSTRUCTIONS, listOf(CYCLES, INSTRUCTIONS, COST), normalComparator(), setOf(PRODUCTION)),
    CX("CX", OmMetric.CYCLES, OmMetric.PRODUCT, listOf(CYCLES, COST, AREA), lastTwoProductComparator(), setOf(NORMAL, INFINITE)),
    CXP("CX", OmMetric.CYCLES, OmMetric.PRODUCT, listOf(CYCLES, COST, INSTRUCTIONS), lastTwoProductComparator(), setOf(PRODUCTION)),
    AG("AG", OmMetric.AREA, OmMetric.COST, listOf(AREA, COST, CYCLES), normalComparator(), setOf(NORMAL, INFINITE)),
    AC("AC", OmMetric.AREA, OmMetric.CYCLES, listOf(AREA, CYCLES, COST), normalComparator(), setOf(NORMAL, INFINITE)),
    AX("AX", OmMetric.AREA, OmMetric.PRODUCT, listOf(AREA, COST, CYCLES), lastTwoProductComparator(), setOf(NORMAL, INFINITE)),
    IG("IG", OmMetric.INSTRUCTIONS, OmMetric.COST, listOf(INSTRUCTIONS, COST, CYCLES), normalComparator(), setOf(PRODUCTION)),
    IC("IC", OmMetric.INSTRUCTIONS, OmMetric.CYCLES, listOf(INSTRUCTIONS, CYCLES, COST), normalComparator(), setOf(PRODUCTION)),
    IX("IX", OmMetric.INSTRUCTIONS, OmMetric.PRODUCT, listOf(INSTRUCTIONS, COST, CYCLES), lastTwoProductComparator(), setOf(PRODUCTION)),
    SG("SUM-G", OmMetric.SUM, OmMetric.COST, listOf(COST, CYCLES, AREA), sumComparator(COST), setOf(NORMAL, INFINITE)),
    SGP("SUM-G", OmMetric.SUM, OmMetric.COST, listOf(COST, CYCLES, INSTRUCTIONS), sumComparator(COST), setOf(PRODUCTION)),
    SC("SUM-C", OmMetric.SUM, OmMetric.CYCLES, listOf(COST, CYCLES, AREA), sumComparator(CYCLES), setOf(NORMAL, INFINITE)),
    SCP("SUM-C", OmMetric.SUM, OmMetric.CYCLES, listOf(COST, CYCLES, INSTRUCTIONS), sumComparator(CYCLES), setOf(PRODUCTION)),
    SA("SUM-A", OmMetric.SUM, OmMetric.AREA, listOf(COST, CYCLES, AREA), sumComparator(AREA), setOf(NORMAL, INFINITE)),
    SI("SUM-I", OmMetric.SUM, OmMetric.INSTRUCTIONS, listOf(COST, CYCLES, INSTRUCTIONS), sumComparator(INSTRUCTIONS), setOf(PRODUCTION)),

    HEIGHT("Height", OmMetric.HEIGHT, OmMetric.CYCLES, listOf(OmScorePart.HEIGHT, CYCLES, COST), normalComparator(), setOf(NORMAL, INFINITE)),
    WIDTH("Width", OmMetric.Width, OmMetric.CYCLES, listOf(OmScorePart.WIDTH, CYCLES, COST), normalComparator(), setOf(NORMAL)),

    OGC("OGC", OmMetric.COST, OmMetric.CYCLES, listOf(COST, CYCLES, AREA), normalComparator(), setOf(NORMAL, INFINITE), OVERLAP),
    OGA("OGA", OmMetric.COST, OmMetric.AREA, listOf(COST, AREA, CYCLES), normalComparator(), setOf(NORMAL, INFINITE), OVERLAP),
    OGX("OGX", OmMetric.COST, OmMetric.PRODUCT, listOf(COST, CYCLES, AREA), lastTwoProductComparator(), setOf(NORMAL, INFINITE), OVERLAP),
    OCG("OCG", OmMetric.CYCLES, OmMetric.COST, listOf(CYCLES, COST, AREA), normalComparator(), setOf(NORMAL, INFINITE), OVERLAP),
    OCA("OCA", OmMetric.CYCLES, OmMetric.AREA, listOf(CYCLES, AREA, COST), normalComparator(), setOf(NORMAL, INFINITE), OVERLAP),
    OCX("OCX", OmMetric.CYCLES, OmMetric.PRODUCT, listOf(CYCLES, COST, AREA), lastTwoProductComparator(), setOf(NORMAL, INFINITE), OVERLAP),
    OAG("OAG", OmMetric.AREA, OmMetric.COST, listOf(AREA, COST, CYCLES), normalComparator(), setOf(NORMAL, INFINITE), OVERLAP),
    OAC("OAC", OmMetric.AREA, OmMetric.CYCLES, listOf(AREA, CYCLES, COST), normalComparator(), setOf(NORMAL, INFINITE), OVERLAP),
    OAX("OAX", OmMetric.AREA, OmMetric.PRODUCT, listOf(AREA, COST, CYCLES), lastTwoProductComparator(), setOf(NORMAL, INFINITE), OVERLAP),

    TIG("TIG", OmMetric.INSTRUCTIONS, OmMetric.COST, listOf(INSTRUCTIONS, COST, CYCLES, AREA), normalComparator(), setOf(NORMAL, INFINITE), TRACKLESS),
    TIC("TIC", OmMetric.INSTRUCTIONS, OmMetric.CYCLES, listOf(INSTRUCTIONS, CYCLES, COST, AREA), normalComparator(), setOf(NORMAL, INFINITE), TRACKLESS),
    TIA("TIA", OmMetric.INSTRUCTIONS, OmMetric.AREA, listOf(INSTRUCTIONS, AREA, COST, CYCLES), normalComparator(), setOf(NORMAL, INFINITE), TRACKLESS),

    IGNP("IG", OmMetric.INSTRUCTIONS, OmMetric.COST, listOf(INSTRUCTIONS, COST, CYCLES, AREA), normalComparator(), setOf(NORMAL, INFINITE)),
    ICNP("IC", OmMetric.INSTRUCTIONS, OmMetric.CYCLES, listOf(INSTRUCTIONS, CYCLES, COST, AREA), normalComparator(), setOf(NORMAL, INFINITE)),
    IANP("IA", OmMetric.INSTRUCTIONS, OmMetric.AREA, listOf(INSTRUCTIONS, AREA, COST, CYCLES), normalComparator(), setOf(NORMAL, INFINITE)),

    CINP("CI", OmMetric.CYCLES, OmMetric.INSTRUCTIONS, listOf(CYCLES, INSTRUCTIONS, COST, AREA), normalComparator(), setOf(NORMAL, INFINITE)),

    S4G("SUM4-G", OmMetric.SUM4, OmMetric.COST, listOf(COST, CYCLES, AREA, INSTRUCTIONS), sumComparator(COST), setOf(NORMAL, INFINITE)),
    S4C("SUM4-C", OmMetric.SUM4, OmMetric.CYCLES, listOf(COST, CYCLES, AREA, INSTRUCTIONS), sumComparator(CYCLES), setOf(NORMAL, INFINITE)),
    S4A("SUM4-A", OmMetric.SUM4, OmMetric.AREA, listOf(COST, CYCLES, AREA, INSTRUCTIONS), sumComparator(AREA), setOf(NORMAL, INFINITE)),
    S4I("SUM4-I", OmMetric.SUM4, OmMetric.INSTRUCTIONS, listOf(COST, CYCLES, AREA, INSTRUCTIONS), sumComparator(INSTRUCTIONS), setOf(NORMAL, INFINITE)),
    ;

    val contentDescription: String = requiredParts.joinToString("/") { it.key.toString() }
    val scoreComparator: Comparator<OmScore> = Comparator.comparing({ normalizeScore(it) }, comparator)

    fun supportsPuzzle(puzzle: OmPuzzle) = supportedTypes.contains(puzzle.type)

    fun supportsScore(score: OmScore) = score.parts.keys.containsAll(requiredParts) && modifier == score.modifier

    fun normalizeScore(score: OmScore): OmScore = OmScore(sortScoreParts(score.parts.asIterable()).map { it.key to it.value }, score.modifier)

    fun sortScoreParts(parts: Iterable<Map.Entry<OmScorePart, Double>>): Iterable<Map.Entry<OmScorePart, Double>> {
        check(parts.map { it.key }.containsAll(requiredParts))
        return requiredParts.map { scorePart -> parts.first { it.key == scorePart } }
    }
}

private fun normalComparator(): Comparator<OmScore> =
    Comparator { s1, s2 -> s1.parts.map { (part, value) -> value.compareTo(s2.parts[part]!!) }.firstOrNull { it != 0 } ?: 0 }

private fun lastTwoProductComparator(): Comparator<OmScore> = Comparator.comparing({ it.transformToLastTwoProduct() }, normalComparator())

private fun OmScore.transformToLastTwoProduct() =
    OmScore((parts.toList().dropLast(2) + parts.toList().takeLast(2).let { COMPUTED to it.first().second * it.last().second }))

private fun sumComparator(tieBreaker: OmScorePart): java.util.Comparator<OmScore> =
    Comparator.comparingDouble<OmScore> { score -> score.parts.map { it.value }.sum() }.thenComparingDouble { it.parts[tieBreaker]!! }
