package com.faendir.zachtronics.bot.archive

import com.faendir.zachtronics.bot.model.Solution

interface Archive<S: Solution> {

    /**
     * @return (title, description)
     */
    fun archive(solution: S) : ArchiveResult

    /**
     * @return [(title, description), ...]
     */
    fun archiveAll(solutions: Collection<S>): List<ArchiveResult> = solutions.map { archive(it) }
}