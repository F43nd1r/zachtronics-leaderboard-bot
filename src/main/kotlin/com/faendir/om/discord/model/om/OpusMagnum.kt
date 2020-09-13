package com.faendir.om.discord.model.om

import com.faendir.om.discord.model.Game

object OpusMagnum : Game<OmScore, OmPuzzle> {
    override val discordChannel = "opus-magnum"
    override fun findPuzzleByName(name: String) = OmPuzzle.values().filter { it.displayName.contains(name, ignoreCase = true) }
    override fun parseScore(puzzle: OmPuzzle, string: String): OmScore? {
        if (string.isBlank()) return null
        val parts = string.split('/')
        if (parts.size < 3) return null
        if (string.contains(Regex("[a-zA-Z]"))) {
            return OmScore((parts.map { OmScorePart.parse(it) ?: return null }).toMap(LinkedHashMap()))
        }
        if (parts.size == 4) {
            return OmScore(linkedMapOf(OmScorePart.COST to parts[0].toDouble(),
                OmScorePart.CYCLES to parts[1].toDouble(),
                OmScorePart.AREA to parts[2].toDouble(),
                OmScorePart.INSTRUCTIONS to parts[3].toDouble()))
        }
        if (parts.size == 3) {
            return OmScore(linkedMapOf(OmScorePart.COST to parts[0].toDouble(),
                OmScorePart.CYCLES to parts[1].toDouble(),
                (if (puzzle.type == OmType.PRODUCTION) OmScorePart.INSTRUCTIONS else OmScorePart.AREA) to parts[2].toDouble()))
        }
        return null
    }
}