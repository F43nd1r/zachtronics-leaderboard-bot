package com.faendir.zachtronics.bot.generic.archive

import com.faendir.zachtronics.bot.model.Solution

interface Archive<S: Solution> {

    /**
     * @return everything that was updated
     */
    @JvmDefault
    fun archive(solution: S) : List<String>
}