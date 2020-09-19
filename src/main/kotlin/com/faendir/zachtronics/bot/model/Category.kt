package com.faendir.zachtronics.bot.model

interface Category<SELF : Category<SELF, S, P>, S : Score, P : Puzzle> : Comparable<SELF> {
    val displayName: String
    val contentDescription: String

    fun isBetterOrEqual(s1: S, s2: S): Boolean

    fun supportsPuzzle(puzzle: P): Boolean

    fun supportsScore(score: S): Boolean
}