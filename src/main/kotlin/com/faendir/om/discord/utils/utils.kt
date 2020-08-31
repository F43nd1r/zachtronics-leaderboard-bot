package com.faendir.om.discord.utils

import com.faendir.om.discord.leaderboards.Leaderboard
import com.faendir.om.discord.config.GitProperties
import com.faendir.om.discord.model.Category
import com.faendir.om.discord.model.Score
import com.faendir.om.discord.puzzle.Puzzle
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider

fun List<Leaderboard>.findCategories(puzzle: Puzzle, score: Score): Map<Leaderboard, List<Category>> =
    mapNotNull { leaderboard ->
        val categories = leaderboard.supportedCategories.filter {
            it.supportedGroups.contains(puzzle.group) &&
                    it.supportedTypes.contains(puzzle.type) &&
                    score.parts.keys.containsAll(it.requiredParts)
        }
        if (categories.isNotEmpty()) leaderboard to categories else null
    }.toMap()
