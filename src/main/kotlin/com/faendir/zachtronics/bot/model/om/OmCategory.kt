package com.faendir.zachtronics.bot.model.om

import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.model.om.OmScorePart.*
import com.faendir.zachtronics.bot.model.om.OmType.INFINITE
import com.faendir.zachtronics.bot.model.om.OmType.NORMAL


enum class OmCategory(
    override val displayName: String,
    private val requiredParts: List<OmScorePart>,
    private val compare: OmCategory.(OmScore, OmScore) -> Int,
    private val supportedTypes: Set<OmType> = OmType.values().toSet(),
    private val supportedGroups: Set<OmGroup> = OmGroup.values().toSet(),
) : Category<OmScore, OmPuzzle> {
    OCG("OCG", listOf(OVERLAP_CYCLES, COST, AREA), OmCategory::normalCompare, setOf(NORMAL, INFINITE)),
    OCA("OCA", listOf(OVERLAP_CYCLES, AREA, COST), OmCategory::normalCompare, setOf(NORMAL, INFINITE)),
    OCX("OCX", listOf(OVERLAP_CYCLES, COST, AREA), OmCategory::lastTwoProductCompare, setOf(NORMAL, INFINITE)),

    WIDTH("W", listOf(OmScorePart.WIDTH, CYCLES, COST), OmCategory::normalCompare, setOf(NORMAL)),
    HEIGHT("H", listOf(OmScorePart.HEIGHT, CYCLES, COST), OmCategory::normalCompare, setOf(NORMAL, INFINITE)),

    GC("GC", listOf(COST, CYCLES, AREA), OmCategory::normalCompare, setOf(NORMAL, INFINITE)),
    GA("GA", listOf(COST, AREA, CYCLES), OmCategory::normalCompare, setOf(NORMAL, INFINITE)),
    GX("GX", listOf(COST, CYCLES, AREA), OmCategory::lastTwoProductCompare, setOf(NORMAL, INFINITE)),
    GCP("GC", listOf(COST, CYCLES, INSTRUCTIONS), OmCategory::normalCompare, setOf(OmType.PRODUCTION)),
    GI("GI", listOf(COST, INSTRUCTIONS, CYCLES), OmCategory::normalCompare, setOf(OmType.PRODUCTION)),
    GXP("GX", listOf(COST, CYCLES, INSTRUCTIONS), OmCategory::lastTwoProductCompare, setOf(OmType.PRODUCTION)),
    CG("CG", listOf(CYCLES, COST, AREA), OmCategory::normalCompare, setOf(NORMAL, INFINITE)),
    CA("CA", listOf(CYCLES, AREA, COST), OmCategory::normalCompare, setOf(NORMAL, INFINITE)),
    CX("CX", listOf(CYCLES, COST, AREA), OmCategory::lastTwoProductCompare, setOf(NORMAL, INFINITE)),
    CGP("CG", listOf(CYCLES, COST, INSTRUCTIONS), OmCategory::normalCompare, setOf(OmType.PRODUCTION)),
    CI("CI", listOf(CYCLES, INSTRUCTIONS, COST), OmCategory::normalCompare, setOf(OmType.PRODUCTION)),
    CXP("CX", listOf(CYCLES, COST, INSTRUCTIONS), OmCategory::lastTwoProductCompare, setOf(OmType.PRODUCTION)),
    AG("AG", listOf(AREA, COST, CYCLES), OmCategory::normalCompare, setOf(NORMAL, INFINITE)),
    AC("AC", listOf(AREA, CYCLES, COST), OmCategory::normalCompare, setOf(NORMAL, INFINITE)),
    AX("AX", listOf(AREA, COST, CYCLES), OmCategory::lastTwoProductCompare, setOf(NORMAL, INFINITE)),
    IG("IG", listOf(INSTRUCTIONS, COST, CYCLES), OmCategory::normalCompare, setOf(OmType.PRODUCTION)),
    IC("IC", listOf(INSTRUCTIONS, CYCLES, COST), OmCategory::normalCompare, setOf(OmType.PRODUCTION)),
    IX("IX", listOf(INSTRUCTIONS, COST, CYCLES), OmCategory::lastTwoProductCompare, setOf(OmType.PRODUCTION)),
    SG("SUM", listOf(COST, CYCLES, AREA), { s1, s2 -> sumCompare(s1, s2, COST) }, setOf(NORMAL, INFINITE)),
    SGP("SUM", listOf(COST, CYCLES, INSTRUCTIONS), { s1, s2 -> sumCompare(s1, s2, COST) }, setOf(OmType.PRODUCTION)),
    SC("SUM", listOf(COST, CYCLES, AREA), { s1, s2 -> sumCompare(s1, s2, CYCLES) }, setOf(NORMAL, INFINITE)),
    SCP("SUM", listOf(COST, CYCLES, INSTRUCTIONS), { s1, s2 -> sumCompare(s1, s2, CYCLES) }, setOf(OmType.PRODUCTION)),
    SA("SUM", listOf(COST, CYCLES, AREA), { s1, s2 -> sumCompare(s1, s2, AREA) }, setOf(NORMAL, INFINITE)),
    SI("SUM", listOf(COST, CYCLES, INSTRUCTIONS), { s1, s2 -> sumCompare(s1, s2, INSTRUCTIONS) }, setOf(OmType.PRODUCTION)), ;

    override val contentDescription: String = requiredParts.joinToString("/") { it.key.toString() }
    override val scoreComparator = Comparator<OmScore> { s1, s2 -> compare(normalizeScore(s1), normalizeScore(s2)) }

    override fun supportsPuzzle(puzzle: OmPuzzle) = supportedTypes.contains(puzzle.type) && supportedGroups.contains(puzzle.group)

    override fun supportsScore(score: OmScore) = score.parts.keys.containsAll(requiredParts)

    private fun normalCompare(s1: OmScore, s2: OmScore): Int = s1.parts.map { (part, value) -> value.compareTo(s2.parts[part]!!) }.firstOrNull { it != 0 } ?: 0

    private fun lastTwoProductCompare(s1: OmScore, s2: OmScore): Int = normalCompare(s1.transformToLastTwoProduct(), s2.transformToLastTwoProduct())

    private fun OmScore.transformToLastTwoProduct() =
        OmScore((parts.toList().dropLast(2) + parts.toList().takeLast(2).let { COMPUTED to it.first().second * it.last().second }))

    private fun sumCompare(s1: OmScore, s2: OmScore, tieBreaker: OmScorePart): Int =
        Comparator.comparingDouble<OmScore> { score -> score.parts.map { it.value }.sum() }.thenComparingDouble { it.parts[tieBreaker]!! }.compare(s1, s2)

    fun normalizeScore(score: OmScore): OmScore = OmScore(sortScoreParts(score.parts.asIterable()).map { it.key to it.value })

    fun sortScoreParts(parts: Iterable<Map.Entry<OmScorePart, Double>>): Iterable<Map.Entry<OmScorePart, Double>> = parts.sortedBy {
        check(requiredParts.contains(it.key))
        requiredParts.indexOf(it.key)
    }
}
