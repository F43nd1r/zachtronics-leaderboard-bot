package com.faendir.zachtronics.bot.generic.discord

import com.faendir.zachtronics.bot.model.Archive
import com.faendir.zachtronics.bot.model.Solution
import com.faendir.zachtronics.bot.utils.Result
import com.faendir.zachtronics.bot.utils.message
import net.dv8tion.jda.api.entities.Message

abstract class AbstractArchiveCommand<S : Solution>(private val archive: Archive<S>) : Command {
    override val name: String = "archive"
    override val isReadOnly: Boolean = false

    override fun handleMessage(message: Message): String {
        return parseSolution(message).flatMap {
            val result = archive.archive(it)
            if (result.isNotEmpty()) {
                Result.success("thanks, your solution has been archived ${it.score.toDisplayString()} $result.")
            } else {
                Result.failure("sorry, your solution did not qualify for archiving.")
            }
        }.message
    }

    abstract fun parseSolution(message: Message): Result<S>
}