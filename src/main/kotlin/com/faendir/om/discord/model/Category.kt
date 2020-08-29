package com.faendir.om.discord.model

import com.faendir.om.discord.model.ScorePart.*
import com.faendir.om.discord.puzzle.Group
import com.faendir.om.discord.puzzle.Type
import com.faendir.om.discord.puzzle.Type.INFINITE
import com.faendir.om.discord.puzzle.Type.NORMAL


private val whGroups = setOf(
    Group.CHAPTER_1,
    Group.CHAPTER_2,
    Group.CHAPTER_3,
    Group.CHAPTER_4,
    Group.CHAPTER_5,
    Group.JOURNAL_I,
    Group.JOURNAL_II,
    Group.JOURNAL_III,
    Group.JOURNAL_IV,
    Group.JOURNAL_V,
    Group.JOURNAL_VII,
    Group.JOURNAL_VIII,
    Group.JOURNAL_IX
)

enum class Category(
    val displayName: String,
    val requiredParts: List<ScorePart>,
    private val isBetterOrEqualImpl: Category.(Score, Score) -> Boolean,
    val supportedTypes: Set<Type> = Type.values().toSet(),
    val supportedGroups: Set<Group> = Group.values().toSet(),
) {
    WIDTH("W", listOf(ScorePart.WIDTH, CYCLES, COST), Category::normalSort, setOf(NORMAL), whGroups),
    HEIGHT("H", listOf(ScorePart.HEIGHT, CYCLES, COST), Category::normalSort, setOf(NORMAL, INFINITE), whGroups),

    GC("GC", listOf(COST, CYCLES, AREA), Category::normalSort, setOf(NORMAL, INFINITE)),
    GA("GA", listOf(COST, AREA, CYCLES), Category::normalSort, setOf(NORMAL, INFINITE)),
    GX("GX", listOf(COST, CYCLES, AREA), Category::lastTwoProductSort, setOf(NORMAL, INFINITE)),
    GCP("GC", listOf(COST, CYCLES, INSTRUCTIONS), Category::normalSort, setOf(Type.PRODUCTION)),
    GI("GI", listOf(COST, INSTRUCTIONS, CYCLES), Category::normalSort, setOf(Type.PRODUCTION)),
    GXP("GX", listOf(COST, CYCLES, INSTRUCTIONS), Category::lastTwoProductSort, setOf(Type.PRODUCTION)),
    CG("CG", listOf(CYCLES, COST, AREA), Category::normalSort, setOf(NORMAL, INFINITE)),
    CA("CA", listOf(CYCLES, AREA, COST), Category::normalSort, setOf(NORMAL, INFINITE)),
    CX("CX", listOf(CYCLES, COST, AREA), Category::lastTwoProductSort, setOf(NORMAL, INFINITE)),
    CGP("CG", listOf(CYCLES, COST, INSTRUCTIONS), Category::normalSort, setOf(Type.PRODUCTION)),
    CI("CI", listOf(CYCLES, INSTRUCTIONS, COST), Category::normalSort, setOf(Type.PRODUCTION)),
    CXP("CX", listOf(CYCLES, COST, INSTRUCTIONS), Category::lastTwoProductSort, setOf(Type.PRODUCTION)),
    AG("AG", listOf(AREA, COST, CYCLES), Category::normalSort, setOf(NORMAL, INFINITE)),
    AC("AC", listOf(AREA, CYCLES, COST), Category::normalSort, setOf(NORMAL, INFINITE)),
    AX("AX", listOf(AREA, COST, CYCLES), Category::lastTwoProductSort, setOf(NORMAL, INFINITE)),
    IG("IG", listOf(INSTRUCTIONS, COST, CYCLES), Category::normalSort, setOf(Type.PRODUCTION)),
    IC("IC", listOf(INSTRUCTIONS, CYCLES, COST), Category::normalSort, setOf(Type.PRODUCTION)),
    IX("IX", listOf(INSTRUCTIONS, COST, CYCLES), Category::lastTwoProductSort, setOf(Type.PRODUCTION)),
    SG("SUM", listOf(COST, CYCLES, AREA), { s1, s2 -> sumSort(s1, s2, COST) }, setOf(NORMAL, INFINITE)),
    SGP("SUM", listOf(COST, CYCLES, INSTRUCTIONS), { s1, s2 -> sumSort(s1, s2, COST) }, setOf(Type.PRODUCTION)),
    SC("SUM", listOf(COST, CYCLES, AREA), { s1, s2 -> sumSort(s1, s2, CYCLES) }, setOf(NORMAL, INFINITE)),
    SCP("SUM", listOf(COST, CYCLES, INSTRUCTIONS), { s1, s2 -> sumSort(s1, s2, CYCLES) }, setOf(Type.PRODUCTION)),
    SA("SUM", listOf(COST, CYCLES, AREA), { s1, s2 -> sumSort(s1, s2, AREA) }, setOf(NORMAL, INFINITE)),
    SI("SUM", listOf(COST, CYCLES, INSTRUCTIONS), { s1, s2 -> sumSort(s1, s2, INSTRUCTIONS) }, setOf(Type.PRODUCTION)),
    ;

    fun normalizeScore(score: Score): Score {
        check(score.parts.keys.containsAll(requiredParts))
        return Score(requiredParts.map { it to score.parts[it]!! }.toMap(LinkedHashMap()))
    }

    fun isBetterOrEqual(s1: Score, s2: Score): Boolean = isBetterOrEqualImpl(s1, s2)

    private fun normalSort(s1: Score, s2: Score): Boolean {
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

    private fun lastTwoProductSort(s1: Score, s2: Score): Boolean {
        return normalSort(s1.transformToLastTwoProduct(), s2.transformToLastTwoProduct())
    }


    private fun Score.transformToLastTwoProduct(): Score {
        return Score(
            (parts.toList().dropLast(2) + parts.toList().takeLast(2)
                .let { COMPUTED to it.first().second * it.last().second }).toMap(LinkedHashMap())
        )
    }

    fun sumSort(s1: Score, s2: Score, tieBreaker: ScorePart): Boolean {
        val sum1 = s1.parts.map { it.value }.sum()
        val sum2 = s2.parts.map { it.value }.sum()
        return sum1 < sum2 || (sum1 == sum2 && s1.parts[tieBreaker]!! <= s2.parts[tieBreaker]!!)
    }
}
