package com.faendir.zachtronics.bot.model

import discord4j.core.`object`.entity.User
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

interface Game<C : Category<S, P>, S : Score, P : Puzzle, R : Record<S>> {
    val discordChannel: String

    val displayName: String

    val commandName: String

    fun parseCategory(name: String): List<C>

    fun parsePuzzle(name: String): P

    fun hasWritePermission(user: User): Mono<Boolean> = false.toMono()
}