package com.faendir.om.discord.model

import com.faendir.om.discord.model.ScorePart.*
import com.faendir.om.discord.puzzle.Puzzle
import com.faendir.om.discord.puzzle.Type
import kotlinx.serialization.Serializable
import java.text.DecimalFormat

@Serializable
data class Score(val parts: LinkedHashMap<ScorePart, Double>) {

    fun reorderToStandard(): Score {
        return Score(parts.asIterable().sortedBy { it.key.ordinal }.map { it.key to it.value }.toMap(LinkedHashMap()))
    }

    fun toString(separator: String, includeSuffix: Boolean = true) =
        parts.asIterable().joinToString(separator) { (part, value) ->
            val num = numberFormat.format(value)
            if (includeSuffix) num + part.key else num
        }

    fun isBetterOrEqualTo(category: Category, other: Score): Boolean {
        return category.isBetterOrEqual(category.normalizeScore(this), category.normalizeScore(other))
    }

    companion object {
        private val numberFormat = DecimalFormat("0.#")

        fun parse(puzzle: Puzzle, string: String): Score? {
            if (string.isBlank()) return null
            val parts = string.split('/')
            if (parts.size < 3) return null
            if (string.contains(Regex("[a-zA-Z]"))) {
                return Score((parts.map { ScorePart.parse(it) ?: return null }).toMap(LinkedHashMap()))
            }
            if (parts.size == 4) {
                return Score(
                    linkedMapOf(
                        COST to parts[0].toDouble(),
                        CYCLES to parts[1].toDouble(),
                        AREA to parts[2].toDouble(),
                        INSTRUCTIONS to parts[3].toDouble()
                    )
                )
            }
            if (parts.size == 3) {
                return Score(
                    linkedMapOf(
                        COST to parts[0].toDouble(),
                        CYCLES to parts[1].toDouble(),
                        (if (puzzle.type == Type.PRODUCTION) INSTRUCTIONS else AREA) to parts[2].toDouble()
                    )
                )
            }
            return null
        }
    }
}