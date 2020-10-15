package com.faendir.zachtronics.bot.model

interface Archive<S: Solution> {

    /**
     * @return everything that was updated
     */
    @JvmDefault
    fun archive(solution: S) : List<String>
}