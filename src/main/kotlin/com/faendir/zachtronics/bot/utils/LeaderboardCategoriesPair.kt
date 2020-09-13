package com.faendir.zachtronics.bot.utils

import com.faendir.zachtronics.bot.leaderboards.Leaderboard
import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Score

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