package com.faendir.zachtronics.bot.om.model

import com.faendir.zachtronics.bot.model.Score
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.text.DecimalFormat

@Serializable
data class OmScore(val parts: LinkedHashMap<OmScorePart, Double>, @Transient val modifier: OmModifier? = null) : Score {
    constructor(parts: Iterable<Pair<OmScorePart, Double>>, modifier: OmModifier? = null) : this(parts.toMap(LinkedHashMap()), modifier)
    constructor(vararg parts: Pair<OmScorePart, Double>, modifier: OmModifier? = null) : this(parts.asIterable(), modifier)

    var displayAsSum = false

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

    override fun toDisplayString(): String {
        return if(displayAsSum){
            toStandardDisplayString("+") { part, value -> format(value) + part.key } + " = " + numberFormat.format(parts.values.sum())
        }else {
            toStandardDisplayString { part, value -> format(value) + part.key }
        }
    }

    fun toShortDisplayString() = toStandardDisplayString { _, value -> format(value) }
}