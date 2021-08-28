package com.faendir.zachtronics.bot.generic.archive

import com.faendir.zachtronics.bot.model.Solution
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux

interface Archive<S: Solution> {

    /**
     * @return (title, description)
     */
    fun archive(solution: S) : Mono<Pair<String, String>>

    /**
     * @return [(title, description), ...]
     */
    fun archiveAll(solutions: Collection<S>) = solutions.toFlux().flatMap { archive(it) }.collectList()
}