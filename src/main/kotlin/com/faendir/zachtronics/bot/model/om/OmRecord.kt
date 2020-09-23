package com.faendir.zachtronics.bot.model.om

import com.faendir.zachtronics.bot.model.Record
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class OmRecord(override val score: OmScore, val link: String, @Transient val author: String? = null) : Record<OmScore> {
    override fun toDisplayString(): String = "${score.toDisplayString()} $link"
}