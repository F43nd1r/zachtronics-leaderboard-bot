package com.faendir.zachtronics.bot.generic.archive

import com.faendir.zachtronics.bot.model.Solution

interface Archive<S: Solution> {

    /**
     * @return (title, description)
     */
    fun archive(solution: S) : Pair<String, String>

    /**
     * @return [(title, description), ...]
     */
    fun archiveAll(solutions: Collection<S>): List<Pair<String, String>> = solutions.map { archive(it) }
}