package com.faendir.om.discord.commands

import com.faendir.om.discord.leaderboards.Leaderboard
import com.faendir.om.discord.model.Game
import com.faendir.om.discord.model.Puzzle
import com.faendir.om.discord.model.Score
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User

interface Command {
    val regex: Regex

    val name: String

    val helpText: String

    val requiresRoles: List<String>
        get() = emptyList()

    fun <S : Score<S, *>, P : Puzzle> handleMessage(game: Game<S, P>, leaderboards: List<Leaderboard<*, S, P>>, author: User, channel: TextChannel, message: Message,
                                                    command: MatchResult): String
}