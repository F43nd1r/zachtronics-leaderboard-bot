package com.faendir.zachtronics.bot.model.om

import com.faendir.zachtronics.bot.model.Record
import kotlinx.serialization.Serializable

@Serializable
data class OmRecord(val category: OmCategory, val score: OmScore, val link: String) : Record {
    override fun toDisplayString(): String = "${category.displayName} ${score.toDisplayString()} $link"
}