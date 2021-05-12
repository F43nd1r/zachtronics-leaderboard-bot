package com.faendir.zachtronics.bot.model

import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

interface Leaderboard<C : Category<S, P>, S : Score, P : Puzzle, R : Record<S>> {
    val supportedCategories: List<C>

    fun update(puzzle: P, record: R): Mono<UpdateResult> = UpdateResult.NotSupported().toMono()

    fun get(puzzle: P, category: C): Mono<R>
}

sealed class UpdateResult {
    class Success(val oldScores: Map<out Category<*,*>, Score?>) : UpdateResult()

    class ParetoUpdate : UpdateResult()

    class BetterExists(val scores: Map<out Category<*,*>, Score>) : UpdateResult()

    class NotSupported : UpdateResult()
}

