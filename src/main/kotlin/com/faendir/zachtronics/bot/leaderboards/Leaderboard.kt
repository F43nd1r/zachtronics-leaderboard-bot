package com.faendir.zachtronics.bot.leaderboards

import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Record
import com.faendir.zachtronics.bot.model.Score

interface Leaderboard<C : Category<C, S, P>, S : Score, P : Puzzle> {

    val supportedCategories: Collection<C>

    fun update(user: String, puzzle: P, categories: List<C>, score: S, link: String): UpdateResult<C, S> = UpdateResult.NotSupported()

    fun get(puzzle: P, category: C): Record?
}

sealed class UpdateResult<C : Category<C, S, *>, S : Score> {
    class Success<C : Category<C, S, *>, S : Score>(val oldScores: Map<C, S?>) : UpdateResult<C, S>()

    class ParetoUpdate<C : Category<C, S, *>, S : Score> : UpdateResult<C, S>()

    class BetterExists<C : Category<C, S, *>, S : Score>(val scores: Map<C, S>) : UpdateResult<C, S>()

    class BrokenLink<C : Category<C, S, *>, S : Score> : UpdateResult<C, S>()

    class NotSupported<C : Category<C, S, *>, S : Score> : UpdateResult<C, S>()
}

