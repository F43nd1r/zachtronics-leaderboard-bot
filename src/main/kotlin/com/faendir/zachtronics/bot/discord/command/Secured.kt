package com.faendir.zachtronics.bot.discord.command

import discord4j.core.`object`.entity.User

interface Secured {
    fun hasExecutionPermission(user: User): Boolean
}