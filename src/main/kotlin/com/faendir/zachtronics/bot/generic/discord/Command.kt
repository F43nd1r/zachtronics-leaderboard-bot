package com.faendir.zachtronics.bot.generic.discord

import net.dv8tion.jda.api.entities.Message

interface Command {

    val name: String

    val helpText: String

    val isReadOnly: Boolean

    fun handleMessage(message: Message): String
}