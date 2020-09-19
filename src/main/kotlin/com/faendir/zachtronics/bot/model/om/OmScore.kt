package com.faendir.zachtronics.bot.model.om

import com.faendir.zachtronics.bot.model.Score
import kotlinx.serialization.Serializable
import java.text.DecimalFormat

@Serializable
data class OmScore(val parts: LinkedHashMap<OmScorePart, Double>) : Score {
    companion object {
        private val numberFormat = DecimalFormat("0.#")
    }

    override fun toDisplayString(): String = reorderToStandard().parts.asIterable().joinToString("/") { (part, value) -> numberFormat.format(value) + part.key }

    fun toShortDisplayString() = reorderToStandard().parts.asIterable().joinToString("/") { (_, value) -> numberFormat.format(value) }

    private fun reorderToStandard(): OmScore = OmScore(parts.asIterable().sortedBy { it.key }.map { it.key to it.value }.toMap(LinkedHashMap()))

    override val contentDescription: String
        get() = parts.keys.joinToString("/") { it.key.toString() }
}