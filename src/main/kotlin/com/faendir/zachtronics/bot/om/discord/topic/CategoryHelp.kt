package com.faendir.zachtronics.bot.om.discord.topic

import com.faendir.zachtronics.bot.generic.discord.topic.StaticHelpTopic
import com.faendir.zachtronics.bot.om.model.OmCategory
import org.springframework.stereotype.Component

@Component
class CategoryHelp : StaticHelpTopic("category", """
        |Official categories are named after the primary metric and first tiebreaker.
        |For example `GC` means: best cost with cycles as tiebreaker (and area as secondary tiebreaker).
        |`X` refers to the product of the two other scores, for example `GX` means best cost with cycles*area as tiebreaker.
        |`SUM-G` refers to the sum of all three scores, with cost as tiebreaker.
        |
        |Height and width are area metrics limited in one direction. For more info see <https://f43nd1r.github.io/om-leaderboard/wh/>
        |`O` refers to overlap, which violates the rules of the game. Fore more info see <https://f43nd1r.github.io/om-leaderboard/overlap/>
        |
        |All categories are:
        |```
        |${OmCategory.values().joinToString("\n") { "${it.displayName} (${it.contentDescription})" }}
        |```
    """.trimMargin())