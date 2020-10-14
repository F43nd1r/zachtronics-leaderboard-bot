package com.faendir.zachtronics.bot.generic.discord.topic

import net.dv8tion.jda.api.entities.Message

interface HelpTopic {
    val id: String
    fun getHelpText(message: Message): String
}