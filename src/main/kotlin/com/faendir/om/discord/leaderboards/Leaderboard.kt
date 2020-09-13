package com.faendir.om.discord.leaderboards

import com.faendir.om.discord.model.*

interface Leaderboard<C : Category<C, S, *>, S : Score<S, *>, P : Puzzle> {
    val game: Game<S, P>

    val supportedCategories: Collection<C>

    fun update(user: String, puzzle: P, categories: List<C>, score: S, link: String): UpdateResult<C, S> = UpdateResult.NotSupported()

    fun get(puzzle: P, category: C): Record?
}

sealed class UpdateResult<C : Category<C, S, *>, S : Score<S, *>> {
    class Success<C : Category<C, S, *>, S : Score<S, *>>(val oldScores: Map<C, S?>) : UpdateResult<C, S>()

    class ParetoUpdate<C : Category<C, S, *>, S : Score<S, *>> : UpdateResult<C, S>()

    class BetterExists<C : Category<C, S, *>, S : Score<S, *>>(val scores: Map<C, S>) : UpdateResult<C, S>()

    class BrokenLink<C : Category<C, S, *>, S : Score<S, *>> : UpdateResult<C, S>()

    class NotSupported<C : Category<C, S, *>, S : Score<S, *>> : UpdateResult<C, S>()
}

