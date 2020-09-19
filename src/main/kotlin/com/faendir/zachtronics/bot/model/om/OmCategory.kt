package com.faendir.zachtronics.bot.model.om

import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.model.om.OmScorePart.*
import com.faendir.zachtronics.bot.model.om.OmType.INFINITE
import com.faendir.zachtronics.bot.model.om.OmType.NORMAL


enum class OmCategory(
    override val displayName: String,
    private val requiredParts: List<OmScorePart>,
    private val isBetterOrEqualImpl: OmCategory.(OmScore, OmScore) -> Boolean,
    private val supportedTypes: Set<OmType> = OmType.values().toSet(),
    private val supportedGroups: Set<OmGroup> = OmGroup.values().toSet(),
) : Category<OmCategory, OmScore, OmPuzzle> {
    WIDTH("W", listOf(OmScorePart.WIDTH, CYCLES, COST), OmCategory::normalSort, setOf(NORMAL)),
    HEIGHT("H", listOf(OmScorePart.HEIGHT, CYCLES, COST), OmCategory::normalSort, setOf(NORMAL, INFINITE)),

    GC("GC", listOf(COST, CYCLES, AREA), OmCategory::normalSort, setOf(NORMAL, INFINITE)),
    GA("GA", listOf(COST, AREA, CYCLES), OmCategory::normalSort, setOf(NORMAL, INFINITE)),
    GX("GX", listOf(COST, CYCLES, AREA), OmCategory::lastTwoProductSort, setOf(NORMAL, INFINITE)),
    GCP("GC", listOf(COST, CYCLES, INSTRUCTIONS), OmCategory::normalSort, setOf(OmType.PRODUCTION)),
    GI("GI", listOf(COST, INSTRUCTIONS, CYCLES), OmCategory::normalSort, setOf(OmType.PRODUCTION)),
    GXP("GX", listOf(COST, CYCLES, INSTRUCTIONS), OmCategory::lastTwoProductSort, setOf(OmType.PRODUCTION)),
    CG("CG", listOf(CYCLES, COST, AREA), OmCategory::normalSort, setOf(NORMAL, INFINITE)),
    CA("CA", listOf(CYCLES, AREA, COST), OmCategory::normalSort, setOf(NORMAL, INFINITE)),
    CX("CX", listOf(CYCLES, COST, AREA), OmCategory::lastTwoProductSort, setOf(NORMAL, INFINITE)),
    CGP("CG", listOf(CYCLES, COST, INSTRUCTIONS), OmCategory::normalSort, setOf(OmType.PRODUCTION)),
    CI("CI", listOf(CYCLES, INSTRUCTIONS, COST), OmCategory::normalSort, setOf(OmType.PRODUCTION)),
    CXP("CX", listOf(CYCLES, COST, INSTRUCTIONS), OmCategory::lastTwoProductSort, setOf(OmType.PRODUCTION)),
    AG("AG", listOf(AREA, COST, CYCLES), OmCategory::normalSort, setOf(NORMAL, INFINITE)),
    AC("AC", listOf(AREA, CYCLES, COST), OmCategory::normalSort, setOf(NORMAL, INFINITE)),
    AX("AX", listOf(AREA, COST, CYCLES), OmCategory::lastTwoProductSort, setOf(NORMAL, INFINITE)),
    IG("IG", listOf(INSTRUCTIONS, COST, CYCLES), OmCategory::normalSort, setOf(OmType.PRODUCTION)),
    IC("IC", listOf(INSTRUCTIONS, CYCLES, COST), OmCategory::normalSort, setOf(OmType.PRODUCTION)),
    IX("IX", listOf(INSTRUCTIONS, COST, CYCLES), OmCategory::lastTwoProductSort, setOf(OmType.PRODUCTION)),
    SG("SUM", listOf(COST, CYCLES, AREA), { s1, s2 -> sumSort(s1, s2, COST) }, setOf(NORMAL, INFINITE)),
    SGP("SUM", listOf(COST, CYCLES, INSTRUCTIONS), { s1, s2 -> sumSort(s1, s2, COST) }, setOf(OmType.PRODUCTION)),
    SC("SUM", listOf(COST, CYCLES, AREA), { s1, s2 -> sumSort(s1, s2, CYCLES) }, setOf(NORMAL, INFINITE)),
    SCP("SUM", listOf(COST, CYCLES, INSTRUCTIONS), { s1, s2 -> sumSort(s1, s2, CYCLES) }, setOf(OmType.PRODUCTION)),
    SA("SUM", listOf(COST, CYCLES, AREA), { s1, s2 -> sumSort(s1, s2, AREA) }, setOf(NORMAL, INFINITE)),
    SI("SUM", listOf(COST, CYCLES, INSTRUCTIONS), { s1, s2 -> sumSort(s1, s2, INSTRUCTIONS) }, setOf(OmType.PRODUCTION)), ;

    override val contentDescription: String = requiredParts.joinToString("/") { it.key.toString() }

    override fun supportsPuzzle(puzzle: OmPuzzle) = supportedTypes.contains(puzzle.type) && supportedGroups.contains(puzzle.group)

    override fun supportsScore(score: OmScore) = score.parts.keys.containsAll(requiredParts)

    override fun isBetterOrEqual(s1: OmScore, s2: OmScore): Boolean {
        return isBetterOrEqualImpl(s1, s2)
    }

    private fun normalSort(s1: OmScore, s2: OmScore): Boolean {
        s1.parts.forEach { (part, value) ->
            val otherValue = s2.parts[part]!!
            //better score
            if (value < otherValue) return true
            //worse score
            if (value > otherValue) return false
            //same score, check next component
        }
        //everything is the same
        return true
    }

    private fun lastTwoProductSort(s1: OmScore, s2: OmScore): Boolean {
        return normalSort(s1.transformToLastTwoProduct(), s2.transformToLastTwoProduct())
    }


    private fun OmScore.transformToLastTwoProduct(): OmScore {
        return OmScore((parts.toList().dropLast(2) + parts.toList().takeLast(2).let { COMPUTED to it.first().second * it.last().second }).toMap(LinkedHashMap()))
    }

    fun sumSort(s1: OmScore, s2: OmScore, tieBreaker: OmScorePart): Boolean {
        val sum1 = s1.parts.map { it.value }.sum()
        val sum2 = s2.parts.map { it.value }.sum()
        return sum1 < sum2 || (sum1 == sum2 && s1.parts[tieBreaker]!! <= s2.parts[tieBreaker]!!)
    }

    fun normalizeScore(score: OmScore): OmScore {
        check(score.parts.keys.containsAll(requiredParts))
        return OmScore(requiredParts.map { it to score.parts[it]!! }.toMap(LinkedHashMap()))
    }
}
