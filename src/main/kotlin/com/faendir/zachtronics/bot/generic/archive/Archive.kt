package com.faendir.zachtronics.bot.generic.archive

import com.faendir.zachtronics.bot.model.Solution
import reactor.core.publisher.Mono

interface Archive<S: Solution> {

    /**
     * @return (title, description)
     */
    fun archive(solution: S) : Mono<Pair<String, String>>
}