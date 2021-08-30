package com.faendir.zachtronics.bot.om.discord

import com.faendir.zachtronics.bot.discord.command.Secured
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.User

object OmSecured : Secured {
    override fun hasExecutionPermission(user: User): Boolean {
        return if (user is Member) user.roles.any { it.name == "trusted-leaderboard-poster" }.block()!! else false
    }
}