package com.faendir.zachtronics.bot.discord.commands

import com.faendir.zachtronics.bot.discord.commands.topic.HelpTopic
import com.faendir.zachtronics.bot.model.*
import com.faendir.zachtronics.bot.utils.Result
import com.faendir.zachtronics.bot.utils.match
import com.faendir.zachtronics.bot.utils.message
import net.dv8tion.jda.api.entities.Message
import org.springframework.stereotype.Component

@Component
class HelpCommand(private val topics: List<HelpTopic>) : Command {
    override val name: String = "help"
    override fun helpText(game: Game<*, *, *, *>): String = "This command"
    override val isReadOnly: Boolean = true
    private val regex = Regex("!help\\s*(?<topic>\\S+)?")

    override fun <C : Category<S, P>, S : Score, P : Puzzle, R : Record<S>> handleMessage(game: Game<C, S, P, R>, message: Message): String {
        return message.match(regex).flatMap { command ->
            val topicId = command.groups["topic"]?.value ?: ""
            topics.find { it.id == topicId }?.let { Result.success(it.getHelpText(game, message)) }
                ?: Result.parseFailure("I can't help you with " + "\"$topicId\". Try one of these:\n```${topics.joinToString("\n") { it.id }}```")
        }.message
    }
}