package com.faendir.om.discord.leaderboards

import com.faendir.om.discord.utils.Score
import com.faendir.om.discord.categories.Category

interface Leaderboard<P> {
    val supportedCategories: Collection<Category>

    fun findPuzzle(puzzle: String): PuzzleResult<P>

    fun update(user: String, puzzle: P, category: Category, score: Score, link: String) : UpdateResult

    fun get(puzzle: P, category: Category) : GetResult
}

sealed class PuzzleResult<T> {
    class Success<T>(val puzzle: T) : PuzzleResult<T>()

    class NotFound<T>() : PuzzleResult<T>()

    class Ambiguous<T>(vararg val puzzles : String) : PuzzleResult<T>()

}

sealed class UpdateResult {
    class Success(val puzzle: String, val oldScore: Score?) : UpdateResult()

    class BetterExists(val puzzle: String, val score: Score) : UpdateResult()

    object BrokenLink : UpdateResult()

    class GenericFailure(val exception: Exception) : UpdateResult()
}

sealed class GetResult {
    class Success(val puzzle: String, val score: Score, val link: String) : GetResult()

    class NoScore(val puzzle: String) : GetResult()

    class ScoreNotRecorded(val puzzle: String, val reason: String) : GetResult()
}

