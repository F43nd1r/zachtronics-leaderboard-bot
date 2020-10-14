package com.faendir.zachtronics.bot.generic.discord

import com.faendir.zachtronics.bot.generic.discord.topic.HelpTopic
import com.faendir.zachtronics.bot.utils.Result
import com.faendir.zachtronics.bot.utils.match
import com.faendir.zachtronics.bot.utils.message
import net.dv8tion.jda.api.entities.Message
import org.springframework.stereotype.Component

@Component
class HelpCommand(private val topics: List<HelpTopic>) : Command {
    override val name: String = "help"
    override val helpText: String = "This command"
    override val isReadOnly: Boolean = true
    private val regex = Regex("!help\\s*(?<topic>\\S+)?")

    override fun handleMessage(message: Message): String {
        return message.match(regex).flatMap { command ->
            val topicId = command.groups["topic"]?.value ?: ""
            topics.find { it.id.equals(topicId, ignoreCase = true) }?.let { Result.success(it.getHelpText(message)) } ?: Result.parseFailure("""
                    |I can't help you with "$topicId". Try one of these:
                    |```
                    |${topics.filter { it.id.isNotBlank() }.joinToString("\n") { it.id }}
                    |```
                    """.trimMargin())
        }.message
    }
}