package com.faendir.om.discord.commands

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User

interface Command {
    val regex: Regex

    val name: String

    val helpText: String

    val requiresRoles: List<String>
        get() = emptyList()

    fun handleMessage(author: User, channel: TextChannel, message: Message, command: MatchResult)
}