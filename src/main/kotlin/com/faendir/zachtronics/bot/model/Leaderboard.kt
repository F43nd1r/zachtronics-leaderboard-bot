package com.faendir.zachtronics.bot.model

interface Leaderboard<C : Category<S, P>, S : Score, P : Puzzle, R : Record<S>> {
    val supportedCategories: List<C>

    @JvmDefault
    fun update(puzzle: P, record: R): UpdateResult = UpdateResult.NotSupported()

    fun get(puzzle: P, category: C): R?
}

sealed class UpdateResult {
    class Success(val oldScores: Map<out Category<*,*>, Score?>) : UpdateResult()

    class ParetoUpdate : UpdateResult()

    class BetterExists(val scores: Map<out Category<*,*>, Score>) : UpdateResult()

    class NotSupported : UpdateResult()
}

