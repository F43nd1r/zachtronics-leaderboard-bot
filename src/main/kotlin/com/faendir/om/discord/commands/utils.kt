package com.faendir.om.discord.commands

import com.faendir.om.discord.puzzle.Puzzle

fun findPuzzle(puzzleName: String, processPuzzle: (Puzzle) -> String): String {
    val puzzles = Puzzle.findByName(puzzleName)
    return when (val size = puzzles.size) {
        0 -> "sorry, I did not recognize the puzzle \"$puzzleName\"."
        1 -> processPuzzle(puzzles.first())
        in 2..5 -> "sorry, your request for \"$puzzleName\" was not accurate enough. Use one of:\n${puzzles.joinToString("\n") { it.displayName }}"
        else -> "sorry, your request for \"$puzzleName\" was not accurate enough. $size matches."
    }
}