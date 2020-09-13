package com.faendir.om.discord.model

import java.text.DecimalFormat

interface Score<SELF : Score<SELF, P>, P : ScorePart<P>> {
    val parts: LinkedHashMap<P, Double>

    fun reorderToStandard(): SELF {
        return mutate(parts.asIterable().sortedBy { it.key }.map { it.key to it.value }.toMap(LinkedHashMap()))
    }

    fun mutate(parts: LinkedHashMap<P, Double>): SELF

    fun toString(separator: String, includeSuffix: Boolean = true) = parts.asIterable().joinToString(separator) { (part, value) ->
        val num = numberFormat.format(value)
        if (includeSuffix) num + part.key else num
    }

    companion object {
        private val numberFormat = DecimalFormat("0.#")
    }
}