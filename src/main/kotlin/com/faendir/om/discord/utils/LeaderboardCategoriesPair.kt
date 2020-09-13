package com.faendir.om.discord.utils

import com.faendir.om.discord.leaderboards.Leaderboard
import com.faendir.om.discord.model.Category
import com.faendir.om.discord.model.Puzzle
import com.faendir.om.discord.model.Score

data class LeaderBoardCategoriesPair<C : Category<C, S, *>, S : Score<S, *>, P : Puzzle>(val leaderboard: Leaderboard<C, S, P>, val categories: Collection<C>)

fun <C : Category<C, S, *>, S : Score<S, *>, P : Puzzle> Leaderboard<C, S, P>.findCategories(puzzle: P, score: S): LeaderBoardCategoriesPair<C, S, P>? {
    val categories = supportedCategories.filter {
        it.supportedGroups.contains(puzzle.group) && it.supportedTypes.contains(puzzle.type) && score.parts.keys.containsAll(it.requiredParts)
    }
    return if (categories.isNotEmpty()) {
        LeaderBoardCategoriesPair(this, categories)
    } else {
        null
    }
}