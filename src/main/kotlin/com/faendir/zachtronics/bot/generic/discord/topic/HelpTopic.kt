package com.faendir.zachtronics.bot.generic.discord.topic

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message

interface HelpTopic {
    val id: String

    @JvmDefault
    fun getHelpText(message: Message): String = ""

    @JvmDefault
    fun display(message: Message, embed: EmbedBuilder) {
        embed.setDescription(getHelpText(message))
    }
}