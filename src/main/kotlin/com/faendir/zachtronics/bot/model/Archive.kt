package com.faendir.zachtronics.bot.model

interface Archive<S: Solution> {

    @JvmDefault
    fun archive(solution: S) : Boolean
}