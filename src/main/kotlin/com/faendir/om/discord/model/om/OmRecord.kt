package com.faendir.om.discord.model.om

import com.faendir.om.discord.model.Record
import kotlinx.serialization.Serializable

@Serializable
data class OmRecord(override val category: OmCategory, override val score: OmScore, override val link: String) : Record