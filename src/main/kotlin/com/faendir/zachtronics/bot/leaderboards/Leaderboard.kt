package com.faendir.zachtronics.bot.leaderboards

import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Record
import com.faendir.zachtronics.bot.model.Score

interface Leaderboard<C : Category<C, S, P>, S : Score, P : Puzzle, R : Record<S>> {
    val supportedCategories: List<C>

    @JvmDefault
    fun update(puzzle: P, record: R): UpdateResult<C, S> = UpdateResult.NotSupported()

    fun get(puzzle: P, category: C): R?
}

sealed class UpdateResult<C : Category<C, S, *>, S : Score> {
    class Success<C : Category<C, S, *>, S : Score>(val oldScores: Map<C, S?>) : UpdateResult<C, S>()

    class ParetoUpdate<C : Category<C, S, *>, S : Score> : UpdateResult<C, S>()

    class BetterExists<C : Category<C, S, *>, S : Score>(val scores: Map<C, S>) : UpdateResult<C, S>()

    class NotSupported<C : Category<C, S, *>, S : Score> : UpdateResult<C, S>()
}

