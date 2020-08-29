package com.faendir.om.discord.utils

import com.faendir.om.discord.leaderboards.Leaderboard
import com.faendir.om.discord.leaderboards.git.GitProperties
import com.faendir.om.discord.model.Category
import com.faendir.om.discord.model.Score
import com.faendir.om.discord.puzzle.Puzzle
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider

fun TextChannel.reply(user: User, message: String) = sendMessage("${user.asMention} $message").mention(user).queue()

fun List<Leaderboard>.find(puzzle: Puzzle, score: Score): Map<Leaderboard, List<Category>> =
    mapNotNull { leaderboard ->
        val categories = leaderboard.supportedCategories.filter {
            it.supportedGroups.contains(puzzle.group) &&
                    it.supportedTypes.contains(puzzle.type) &&
                    score.parts.keys.containsAll(it.requiredParts)
        }
        if (categories.isNotEmpty()) leaderboard to categories else null
    }.toMap()

fun Git.commitAndPushChanges(
    user: String,
    puzzle: Puzzle,
    updated: Collection<String>,
    gitProperties: GitProperties
) {
    commit()
        .setAuthor("om-leaderboard-discord-bot", "om-leaderboard-discord-bot@faendir.com")
        .setCommitter(
            "om-leaderboard-discord-bot",
            "om-leaderboard-discord-bot@faendir.com"
        )
        .setMessage("Automated update with solution for ${puzzle.displayName} $updated by $user")
        .call()
    push().setCredentialsProvider(
        UsernamePasswordCredentialsProvider(
            gitProperties.username,
            gitProperties.accessToken
        )
    ).setTimeout(120).call()
}
