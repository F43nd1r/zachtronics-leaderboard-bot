package com.faendir.zachtronics.bot.model

import com.faendir.zachtronics.bot.leaderboards.Leaderboard
import com.faendir.zachtronics.bot.utils.Result
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message

interface Game<C : Category<C, S, P>, S : Score, P : Puzzle, R : Record<S>> {
    val discordChannel: String

    val leaderboards: List<Leaderboard<C, S, P, R>>

    val submissionSyntax : String

    fun parseSubmission(message: Message): Result<Pair<P, R>>

    fun parseCategory(name: String): List<C>

    fun parsePuzzle(name: String): Result<P>

    @JvmDefault
    fun hasWritePermission(member: Member?) : Boolean = false
}