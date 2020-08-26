package com.faendir.om.discord.categories

import com.faendir.om.discord.utils.Score

class Category private constructor(
    val name: String,
    val requiredParts: List<ScorePart>
) {
    fun normalizeScore(score: Score): Score {
        check(score.map { it.first }.containsAll(requiredParts))
        return requiredParts.map { require -> score.first { require == it.first } }
    }

    companion object {
        fun create(defaultName: String, vararg requiredParts: ScorePart) =
            Category(defaultName, requiredParts.toList())
    }
}
