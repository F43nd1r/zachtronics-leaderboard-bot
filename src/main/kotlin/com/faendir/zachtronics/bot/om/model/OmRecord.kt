package com.faendir.zachtronics.bot.om.model

import com.faendir.zachtronics.bot.model.Record
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.dv8tion.jda.api.EmbedBuilder

@Serializable
data class OmRecord(override val score: OmScore, val link: String, @Transient val author: String? = null) : Record<OmScore> {
    override fun toDisplayString(): String = "${score.toDisplayString()} $link"

    override fun display(embed: EmbedBuilder) {
        embed.appendDescription(score.toDisplayString())
        embed.setImage(link)
    }
}