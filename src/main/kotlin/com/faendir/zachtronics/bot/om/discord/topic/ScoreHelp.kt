package com.faendir.zachtronics.bot.om.discord.topic

import com.faendir.zachtronics.bot.generic.discord.topic.HelpTopic
import com.faendir.zachtronics.bot.om.model.OmScorePart
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import org.springframework.stereotype.Component

@Component
class ScoreHelp : HelpTopic {
    override val id: String = "score"
    override fun display(message: Message, embed: EmbedBuilder) {
        embed.setTitle("Scores")
        embed.setDescription("""
        |A score can be either in the standard order (cost/cycles/area/instructions), or be a combination of parts marked with suffixes:
        |```
        |${OmScorePart.values().filter { it.displayName != null }.joinToString("\n") { "${it.key} ${it.displayName} " }}
        |```
        |Example: `3.5w/320c/400g`
    """.trimMargin())
    }
}