package com.faendir.zachtronics.bot.discord.commands

import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.utils.Result
import com.faendir.zachtronics.bot.utils.Result.Failure
import com.faendir.zachtronics.bot.utils.Result.Success
import net.dv8tion.jda.api.entities.Message

fun <P : Puzzle> List<P>.getSinglePuzzle(name: String): Result<P> {
    return when (val size = this.size) {
        0 -> Failure("sorry, I did not recognize the puzzle \"$name\".")
        1 -> Success(first())
        in 2..5 -> Failure("sorry, your request for \"$name\" was not precise enough. Use one of:\n${joinToString("\n") { it.displayName }}")
        else -> Failure("sorry, your request for \"$name\" was not precise enough. $size matches.")
    }
}

fun Message.match(regex: Regex): Result<MatchResult> {
    return regex.matchEntire(contentRaw)?.let { Success(it) } ?: Failure("sorry, I could not parse your command. Type `!help` to see the syntax.")
}