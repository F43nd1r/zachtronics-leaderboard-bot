package com.faendir.zachtronics.bot.model

interface Category<S : Score, P : Puzzle> {
    val displayName: String
    val contentDescription: String

    fun isBetterOrEqual(s1: S, s2: S): Boolean

    fun supportsPuzzle(puzzle: P): Boolean

    fun supportsScore(score: S): Boolean
}