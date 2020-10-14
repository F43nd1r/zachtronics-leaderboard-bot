package com.faendir.zachtronics.bot.model

import com.faendir.zachtronics.bot.utils.Result
import net.dv8tion.jda.api.entities.Member

interface Game<C : Category<S, P>, S : Score, P : Puzzle, R : Record<S>> {
    val discordChannel: String

    val displayName: String

    fun parseCategory(name: String): List<C>

    fun parsePuzzle(name: String): Result<P>

    @JvmDefault
    fun hasWritePermission(member: Member?): Boolean = false
}