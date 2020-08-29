package com.faendir.om.discord.model

enum class ScorePart(val key: Char) {
    HEIGHT('h'), WIDTH('w'), COST('g'), CYCLES('c'), AREA('a'), INSTRUCTIONS('i'), COMPUTED('#');

    companion object {
        fun parse(string: String): Pair<ScorePart, Double>? {
            if (string.isEmpty()) return null
            val part =
                string.last().let { key -> values().find { key.equals(it.key, ignoreCase = true) } } ?: return null
            val number = string.substring(0, string.length - 1).toDoubleOrNull() ?: return null
            return part to number
        }
    }
}