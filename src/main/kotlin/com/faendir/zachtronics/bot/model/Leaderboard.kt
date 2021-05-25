package com.faendir.zachtronics.bot.model

import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono

interface Leaderboard<C : Category, P : Puzzle, R : Record> {
    val supportedCategories: List<C>

    fun update(puzzle: P, record: R): Mono<UpdateResult> = UpdateResult.NotSupported().toMono()

    fun get(puzzle: P, category: C): Mono<R>

    fun getAll(puzzle: P, categories: Collection<C>): Mono<Map<C, R>> =
        categories.toFlux().flatMap { Mono.zip(it.toMono(), get(puzzle, it)) }.collectMap({ it.t1 }, { it.t2 })
}

sealed class UpdateResult {
    class Success(val oldScores: Map<out Category, Score?>) : UpdateResult()

    class ParetoUpdate : UpdateResult()

    class BetterExists(val scores: Map<out Category, Score>) : UpdateResult()

    class NotSupported : UpdateResult()
}

