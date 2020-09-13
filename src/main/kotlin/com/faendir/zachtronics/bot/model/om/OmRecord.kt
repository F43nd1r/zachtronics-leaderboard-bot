package com.faendir.zachtronics.bot.model.om

import com.faendir.zachtronics.bot.model.Record
import kotlinx.serialization.Serializable

@Serializable
data class OmRecord(override val category: OmCategory, override val score: OmScore, override val link: String) : Record