package com.faendir.zachtronics.bot.utils

import com.faendir.zachtronics.bot.leaderboards.Leaderboard
import com.faendir.zachtronics.bot.model.Game
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Score

data class GameLeaderboardsPair<S : Score<S, *>, P : Puzzle>(val game: Game<S, P>, val leaderboards: List<Leaderboard<*, S, P>>)

fun List<Leaderboard<*, *, *>>.groupByGame(): List<GameLeaderboardsPair<*, *>> {
    val result = mutableListOf<GameLeaderboardsPair<*, *>>()
    for (leaderboard in this) {
        val old = result.find { it.game == leaderboard.game }?.also { result.remove(it) }?.leaderboards ?: emptyList()
        result.add(leaderboard.pairWithGame(old))
    }
    return result
}

private fun <S : Score<S, *>, P : Puzzle> Leaderboard<*, S, P>.pairWithGame(previous: List<Leaderboard<*, *, *>>): GameLeaderboardsPair<S, P> {
    @Suppress("UNCHECKED_CAST") return GameLeaderboardsPair(game, (previous as List<Leaderboard<*, S, P>>) + this)
}