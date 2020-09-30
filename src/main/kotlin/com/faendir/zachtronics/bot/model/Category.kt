package com.faendir.zachtronics.bot.model

interface Category<S : Score, P : Puzzle> {
    val displayName: String
    val contentDescription: String
    val scoreComparator: Comparator<S>

    fun supportsPuzzle(puzzle: P): Boolean

    fun supportsScore(score: S): Boolean
}