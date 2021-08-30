package com.faendir.zachtronics.bot.discord.command

interface GameCommand {
    val displayName: String

    val commandName: String

    val commands: List<Command>
}