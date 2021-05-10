package com.faendir.zachtronics.bot.model

import net.dv8tion.jda.api.EmbedBuilder

interface Record<S : Score> {
    val score: S
    fun toDisplayString(): String

    @JvmDefault
    fun display(embed: EmbedBuilder) {
        embed.appendDescription(toDisplayString())
    }
}