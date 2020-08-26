package com.faendir.om.discord.utils

import com.faendir.om.discord.categories.Category
import com.faendir.om.discord.leaderboards.Leaderboard
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User

fun <T> List<T>.toTriple(): Triple<T, T, T> {
    require(this.size == 3) { "List is not of length 3!" }
    val (a, b, c) = this
    return Triple(a, b, c)
}

fun TextChannel.reply(user: User, message: String) = sendMessage("${user.asMention} $message").mention(user).queue()

fun List<Leaderboard<*>>.find(score: Score): List<Pair<Leaderboard<*>, Category>> =
    flatMap { leaderboard -> leaderboard.supportedCategories.map { leaderboard to it } }
        .filter { pair -> score.map { it.first }.containsAll(pair.second.requiredParts) }
