package com.faendir.zachtronics.bot.om.model

import com.faendir.zachtronics.bot.model.Game
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.User
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Component
class OpusMagnum : Game {
    override val displayName = "Opus Magnum"

    override val commandName = "om"

    override fun hasWritePermission(user: User): Mono<Boolean> {
        return if(user is Member) user.roles.any { it.name == "trusted-leaderboard-poster" } else false.toMono()
    }
}