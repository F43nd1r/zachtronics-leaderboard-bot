package com.faendir.zachtronics.bot.discord.commands

import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.utils.Result
import net.dv8tion.jda.api.entities.Message

fun <P : Puzzle> List<P>.getSinglePuzzle(): Result<P> {
    return when (val size = this.size) {
        0 -> Result.Failure("sorry, I did not recognize the puzzle.")
        1 -> Result.Success(first())
        in 2..5 -> Result.Failure("sorry, your puzzle request was not precise enough. Use one of:\n${joinToString("\n") { it.displayName }}")
        else -> Result.Failure("sorry, your puzzle request was not precise enough. $size matches.")
    }
}

fun Message.match(regex: Regex): Result<MatchResult> {
    return regex.matchEntire(contentRaw)?.let { Result.Success(it) } ?: Result.Failure("sorry, I could not parse your command. Type `!help` to see the syntax.")
}