package com.faendir.om.discord.leaderboards

import com.faendir.om.discord.model.Category
import com.faendir.om.discord.model.Score
import com.faendir.om.discord.puzzle.Puzzle

interface Leaderboard {
    val supportedCategories: Collection<Category>

    fun update(user: String, puzzle: Puzzle, categories: List<Category>, score: Score, link: String) : UpdateResult

    fun get(puzzle: Puzzle, category: Category) : GetResult
}

sealed class UpdateResult {
    class Success(val oldScores: Map<Category,Score?>) : UpdateResult()

    object ParetoUpdate : UpdateResult()

    class BetterExists(val scores: Map<Category,Score>) : UpdateResult()

    object BrokenLink : UpdateResult()

    class GenericFailure(val exception: Exception) : UpdateResult()
}

sealed class GetResult {
    class Success(val score: Score, val link: String) : GetResult()

    object NoScore : GetResult()
}

