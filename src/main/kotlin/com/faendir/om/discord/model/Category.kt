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
    val supportedTypes: Set<Type> = Type.values().toSet(),
    val supportedGroups: Set<Group> = Group.values().toSet(),
    val sort: Sort
) {
    WIDTH("W", listOf(ScorePart.WIDTH, CYCLES, COST), setOf(NORMAL), whGroups, sort = Sort.NORMAL),
    HEIGHT("H", listOf(ScorePart.HEIGHT, CYCLES, COST), setOf(NORMAL, INFINITE), whGroups, sort = Sort.NORMAL),

    GC("GC", listOf(COST, CYCLES, AREA), setOf(NORMAL, INFINITE), sort = Sort.NORMAL),
    GA("GA", listOf(COST, AREA, CYCLES), setOf(NORMAL, INFINITE), sort = Sort.NORMAL),
    GX("GX", listOf(COST, CYCLES, AREA), setOf(NORMAL, INFINITE), sort = Sort.LAST_TWO_PRODUCT),
    GCP("GC", listOf(COST, CYCLES, INSTRUCTIONS), setOf(Type.PRODUCTION), sort = Sort.NORMAL),
    GI("GI", listOf(COST, INSTRUCTIONS, CYCLES), setOf(Type.PRODUCTION), sort = Sort.NORMAL),
    GXP("GX", listOf(COST, CYCLES, INSTRUCTIONS), setOf(Type.PRODUCTION), sort = Sort.LAST_TWO_PRODUCT),
    CG("CG", listOf(CYCLES, COST, AREA), setOf(NORMAL, INFINITE), sort = Sort.NORMAL),
    CA("CA", listOf(CYCLES, AREA, COST), setOf(NORMAL, INFINITE), sort = Sort.NORMAL),
    CX("CX", listOf(CYCLES, COST, AREA), setOf(NORMAL, INFINITE), sort = Sort.LAST_TWO_PRODUCT),
    CGP("CG", listOf(CYCLES, COST, INSTRUCTIONS), setOf(Type.PRODUCTION), sort = Sort.NORMAL),
    CI("CI", listOf(CYCLES, INSTRUCTIONS, COST), setOf(Type.PRODUCTION), sort = Sort.NORMAL),
    CXP("CX", listOf(CYCLES, COST, INSTRUCTIONS), setOf(Type.PRODUCTION), sort = Sort.LAST_TWO_PRODUCT),
    AG("AG", listOf(AREA, COST, CYCLES), setOf(NORMAL, INFINITE), sort = Sort.NORMAL),
    AC("AC", listOf(AREA, CYCLES, COST), setOf(NORMAL, INFINITE), sort = Sort.NORMAL),
    AX("AX", listOf(AREA, COST, CYCLES), setOf(NORMAL, INFINITE), sort = Sort.LAST_TWO_PRODUCT),
    IG("IG", listOf(INSTRUCTIONS, COST, CYCLES), setOf(Type.PRODUCTION), sort = Sort.NORMAL),
    IC("IC", listOf(INSTRUCTIONS, CYCLES, COST), setOf(Type.PRODUCTION), sort = Sort.NORMAL),
    IX("IX", listOf(INSTRUCTIONS, COST, CYCLES), setOf(Type.PRODUCTION), sort = Sort.LAST_TWO_PRODUCT),
    SUM("SUM", listOf(COST, CYCLES, AREA), setOf(NORMAL, INFINITE), sort = Sort.SUM),
    SUMP("SUM", listOf(COST, CYCLES, INSTRUCTIONS), setOf(Type.PRODUCTION), sort = Sort.SUM),
    ;

    fun normalizeScore(score: Score): Score {
        check(score.parts.keys.containsAll(requiredParts))
        return Score(requiredParts.map { it to score.parts[it]!! }.toMap(LinkedHashMap()))
    }
}

enum class Sort {
    NORMAL {
        override fun isBetterOrEqual(s1: Score, s2: Score): Boolean {
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
    },
    LAST_TWO_PRODUCT {
        override fun isBetterOrEqual(s1: Score, s2: Score): Boolean {
            return NORMAL.isBetterOrEqual(s1.transformToLastTwoProduct(), s2.transformToLastTwoProduct())
        }


        private fun Score.transformToLastTwoProduct(): Score {
            return Score(
                (parts.toList().dropLast(2) + parts.toList().takeLast(2)
                    .let { COMPUTED to it.first().second * it.last().second }).toMap(LinkedHashMap())
            )
        }
    },
    SUM {
        override fun isBetterOrEqual(s1: Score, s2: Score): Boolean {
            return s1.parts.map { it.value }.sum() <= s2.parts.map { it.value }.sum()
        }
    };

    abstract fun isBetterOrEqual(s1: Score, s2: Score): Boolean
}
