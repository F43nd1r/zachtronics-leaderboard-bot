package com.faendir.zachtronics.bot.generic.discord

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message

interface Command {

    val name: String

    val helpText: String

    val isReadOnly: Boolean

    @JvmDefault
    fun handleMessage(message: Message): String = ""

    @JvmDefault
    fun handleMessageEmbed(message: Message) : EmbedBuilder = EmbedBuilder().setDescription(handleMessage(message))
}