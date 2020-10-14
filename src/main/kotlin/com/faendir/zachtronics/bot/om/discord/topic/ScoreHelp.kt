package com.faendir.zachtronics.bot.om.discord.topic

import com.faendir.zachtronics.bot.generic.discord.topic.StaticHelpTopic
import com.faendir.zachtronics.bot.om.model.OmScorePart
import org.springframework.stereotype.Component

@Component
class ScoreHelp : StaticHelpTopic("score", """
        |A score can be either in the standard order (cost/cycles/area/instructions), or be a combination of parts marked with suffixes:
        |```
        |${OmScorePart.values().filter { it.displayName != null }.joinToString("\n") { "${it.key} ${it.displayName} " }}
        |```
        |Example: `3.5w/320c/400g`
    """.trimMargin())