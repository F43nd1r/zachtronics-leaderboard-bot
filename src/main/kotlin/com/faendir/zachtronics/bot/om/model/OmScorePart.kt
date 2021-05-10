package com.faendir.zachtronics.bot.om.model

enum class OmScorePart(val key: Char, val displayName: String?) {
    HEIGHT('h', "Height"),
    WIDTH('w', "Width"),
    COST('g', "Cost"),
    CYCLES('c', "Cycles"),
    AREA('a', "Area"),
    INSTRUCTIONS('i', "Instructions"),
    COMPUTED('#', null);

    companion object {
        fun parse(string: String): Pair<OmScorePart, Double>? {
            if (string.isEmpty()) return null
            val part = string.last().let { key -> values().find { key.equals(it.key, ignoreCase = true) } } ?: return null
            val number = string.substring(0, string.length - 1).toDoubleOrNull() ?: return null
            return part to number
        }
    }
}