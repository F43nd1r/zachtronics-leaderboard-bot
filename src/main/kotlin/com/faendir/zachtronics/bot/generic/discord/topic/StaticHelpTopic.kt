package com.faendir.zachtronics.bot.generic.discord.topic

import net.dv8tion.jda.api.entities.Message

open class StaticHelpTopic(override val id: String, private val text: String) : HelpTopic {
    override fun getHelpText(message: Message): String = text
}