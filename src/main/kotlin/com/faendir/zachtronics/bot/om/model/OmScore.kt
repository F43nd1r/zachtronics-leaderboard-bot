package com.faendir.zachtronics.bot.om.model

import com.faendir.zachtronics.bot.model.Score
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.text.DecimalFormat

@Serializable
data class OmScore(val parts: LinkedHashMap<OmScorePart, Double>, @Transient var modifier: OmModifier = OmModifier.NORMAL) : Score {
    constructor(parts: Iterable<Pair<OmScorePart, Double>>, modifier: OmModifier = OmModifier.NORMAL) : this(parts.toMap(LinkedHashMap()), modifier)
    constructor(vararg parts: Pair<OmScorePart, Double>, modifier: OmModifier = OmModifier.NORMAL) : this(parts.asIterable(), modifier)
    constructor(parts: Map<OmScorePart, Double>, modifier: OmModifier = OmModifier.NORMAL) : this(LinkedHashMap(parts), modifier)

    @Transient
    var displayAsSum = false

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OmScore

        if (parts.any { other.parts[it.key] != it.value }) return false
        if (modifier != other.modifier) return false

        return true
    }

    override fun hashCode(): Int {
        var result = parts.map { it.key.hashCode() * it.value.hashCode() }.sum()
        result = 31 * result + modifier.hashCode()
        return result
    }

    companion object {
        private val numberFormat = DecimalFormat("0.#")

        fun parse(puzzle: OmPuzzle, string: String): OmScore {
            if (string.isBlank()) throw IllegalArgumentException("I didn't find a score in your command.")
            val outerParts = string.split(':')
            val (modifier, scoreString) = when (outerParts.size) {
                1 -> OmModifier.NORMAL to string
                2 -> (OmModifier.values().find { it.displayName.toString().equals(outerParts[0], ignoreCase = true) }
                    ?: throw IllegalArgumentException("\"${outerParts[0]}\" is not a modifier.")) to outerParts[1]
                else -> throw IllegalArgumentException("I didn't understand \"$string\".")
            }
            val parts = scoreString.split('/', '-')
            if (parts.size < 3) throw IllegalArgumentException("your score must have at least three parts.")
            if (string.contains(Regex("[a-zA-Z]"))) {
                return OmScore(parts.map { OmScorePart.parse(it) ?: throw IllegalArgumentException("I didn't understand \"$it\".") }, modifier)
            }
            if (parts.size == 4) {
                return OmScore(OmScorePart.COST to parts[0].toDouble(),
                    OmScorePart.CYCLES to parts[1].toDouble(),
                    OmScorePart.AREA to parts[2].toDouble(),
                    OmScorePart.INSTRUCTIONS to parts[3].toDouble(),
                    modifier = modifier)
            }
            if (parts.size == 3) {
                return OmScore(OmScorePart.COST to parts[0].toDouble(),
                    OmScorePart.CYCLES to parts[1].toDouble(),
                    (if (puzzle.type == OmType.PRODUCTION) OmScorePart.INSTRUCTIONS else OmScorePart.AREA) to parts[2].toDouble(),
                    modifier = modifier)
            }
            throw IllegalArgumentException("you need to specify score part identifiers when using more than four values.")
        }
    }
}