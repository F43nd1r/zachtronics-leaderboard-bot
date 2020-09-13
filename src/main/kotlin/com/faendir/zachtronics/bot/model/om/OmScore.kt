package com.faendir.zachtronics.bot.model.om

import com.faendir.zachtronics.bot.model.Score
import kotlinx.serialization.Serializable

@Serializable
data class OmScore(override val parts: LinkedHashMap<OmScorePart, Double>) : Score<OmScore, OmScorePart> {
    override fun mutate(parts: LinkedHashMap<OmScorePart, Double>): OmScore = OmScore(parts)
}