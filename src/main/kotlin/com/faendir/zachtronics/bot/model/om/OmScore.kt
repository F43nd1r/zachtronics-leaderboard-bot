package com.faendir.zachtronics.bot.model.om

import com.faendir.zachtronics.bot.model.Score
import kotlinx.serialization.Serializable
import java.text.DecimalFormat

@Serializable
data class OmScore(val parts: LinkedHashMap<OmScorePart, Double>) : Score {
    constructor(parts: Iterable<Pair<OmScorePart, Double>>) : this(parts.toMap(LinkedHashMap()))
    constructor(vararg parts: Pair<OmScorePart, Double>) : this(parts.asIterable())

    companion object {
        private val numberFormat = DecimalFormat("0.#")
    }

    fun toDisplayString(preprocess: Iterable<Map.Entry<OmScorePart, Double>>.() -> Iterable<Map.Entry<OmScorePart, Double>> = { this }, separator: String = "/",
                        format: DecimalFormat.(OmScorePart, Double) -> String): String {
        return parts.asIterable().preprocess().joinToString(separator) { (part, value) -> numberFormat.format(part, value) }
    }

    private fun toStandardDisplayString(separator: String = "/", format: DecimalFormat.(OmScorePart, Double) -> String): String {
        return toDisplayString({ sortedBy { it.key } }, separator, format)
    }

    override fun toDisplayString(): String = toStandardDisplayString { part, value -> format(value) + part.key }

    fun toShortDisplayString() = toStandardDisplayString { _, value -> format(value) }
}