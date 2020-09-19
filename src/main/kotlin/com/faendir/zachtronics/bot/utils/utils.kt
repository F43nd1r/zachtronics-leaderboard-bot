package com.faendir.zachtronics.bot.utils

import com.faendir.zachtronics.bot.leaderboards.Leaderboard
import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Score

fun <C : Category<C, S, P>, S : Score, P : Puzzle> Leaderboard<C, S, P>.findCategoriesSupporting(puzzle: P, score: S): Pair<Leaderboard<C, S, P>, List<C>>? {
    val categories = supportedCategories.filter { it.supportsPuzzle(puzzle) && it.supportsScore(score) }
    return if (categories.isNotEmpty()) {
        this to categories
    } else {
        null
    }
}