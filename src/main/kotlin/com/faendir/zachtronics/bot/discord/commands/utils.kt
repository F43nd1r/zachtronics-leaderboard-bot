package com.faendir.zachtronics.bot.discord.commands

import com.faendir.zachtronics.bot.model.Game
import com.faendir.zachtronics.bot.model.Puzzle

fun <P : Puzzle> findPuzzle(game: Game<*, *, P>, puzzleName: String, processPuzzle: (P) -> String): String {
    val puzzles = game.findPuzzleByName(puzzleName)
    return when (val size = puzzles.size) {
        0 -> "sorry, I did not recognize the puzzle \"$puzzleName\"."
        1 -> processPuzzle(puzzles.first())
        in 2..5 -> "sorry, your request for \"$puzzleName\" was not precise enough. Use one of:\n${puzzles.joinToString("\n") { it.displayName }}"
        else -> "sorry, your request for \"$puzzleName\" was not precise enough. $size matches."
    }
}