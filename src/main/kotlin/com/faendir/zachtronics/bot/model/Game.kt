package com.faendir.zachtronics.bot.model

import discord4j.core.`object`.entity.User
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

interface Game {

    val displayName: String

    val commandName: String

    fun hasWritePermission(user: User): Boolean = false
}