package com.faendir.zachtronics.bot.model

interface Leaderboard<C : Category<S, P>, S : Score, P : Puzzle, R : Record<S>> {
    val supportedCategories: List<C>

    @JvmDefault
    fun update(puzzle: P, record: R): UpdateResult<C, S> = UpdateResult.NotSupported()

    fun get(puzzle: P, category: C): R?
}

sealed class UpdateResult<C : Category<S, *>, S : Score> {
    class Success<C : Category<S, *>, S : Score>(val oldScores: Map<C, S?>) : UpdateResult<C, S>()

    class ParetoUpdate<C : Category<S, *>, S : Score> : UpdateResult<C, S>()

    class BetterExists<C : Category<S, *>, S : Score>(val scores: Map<C, S>) : UpdateResult<C, S>()

    class NotSupported<C : Category<S, *>, S : Score> : UpdateResult<C, S>()
}

