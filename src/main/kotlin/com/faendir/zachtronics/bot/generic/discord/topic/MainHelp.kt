package com.faendir.zachtronics.bot.generic.discord.topic

import com.faendir.zachtronics.bot.generic.discord.Command
import com.faendir.zachtronics.bot.model.Game
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.Message
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class MainHelp(@Lazy private val game: Game<*, *, *, *>, @Lazy private val topics: List<HelpTopic>, @Lazy private val commands: List<Command>) : HelpTopic {
    override val id: String = ""

    override fun getHelpText(message: Message): String {
        val (availableCommands, otherCommands) = commands.partition {
            it.isReadOnly || message.channelType == ChannelType.TEXT && game.hasWritePermission(message.member)
        }
        return """
            |Hello, I'm the leaderboard bot!
            |
            |Available Commands:
            |```
            |${availableCommands.joinToString("\n") { "!${it.name} ${it.helpText}" }}
            |```${getOtherCommandsText(otherCommands, message)}
            |To learn more, try `!help <topic>` with one of the following:
            |```
            |${topics.joinToString("\n") { it.id }}
            |```
            """.trimMargin()
    }

    private fun getOtherCommandsText(otherCommands: List<Command>, message: Message): String {
        return if (otherCommands.isNotEmpty()) {
            """
            |Other Commands (${if (message.channelType == ChannelType.TEXT) "You lack permission to use these" else "Not available in private messages"}):
            |```
            |${otherCommands.joinToString("\n") { "!${it.name} ${it.helpText}" }}
            |```
            """.trimMargin()
        } else {
            ""
        }
    }
}