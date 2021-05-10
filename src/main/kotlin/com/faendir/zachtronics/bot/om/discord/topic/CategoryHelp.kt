package com.faendir.zachtronics.bot.om.discord.topic

import com.faendir.zachtronics.bot.generic.discord.topic.HelpTopic
import com.faendir.zachtronics.bot.generic.discord.topic.StaticHelpTopic
import com.faendir.zachtronics.bot.om.model.OmCategory
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import org.springframework.stereotype.Component

@Component
class CategoryHelp : HelpTopic {
    override val id: String = "category"

    override fun display(message: Message, embed: EmbedBuilder) {
        embed.setTitle("Categories")
        embed.setDescription("""
        |Official categories are named after the primary metric and first tiebreaker.
        |
        |Height and width are area metrics limited in one direction. For more info see <https://f43nd1r.github.io/om-leaderboard/wh/>
        |`o` refers to overlap, which violates the rules of the game. Fore more info see <https://f43nd1r.github.io/om-leaderboard/overlap/>
        |
        |All categories are:
        """.trimMargin())
        embed.addField("GC","`g/c/a` or `g/c/i`", true)
        embed.addField("GA","`g/a/c`", true)
        embed.addField("GI","`g/i/c`", true)
        embed.addField("GX","`g/c*a` or `g/c*i`", true)
        embed.addField("CG","`c/g/a` or `c/g/i`", true)
        embed.addField("CA","`c/a/g`", true)
        embed.addField("CI","`c/i/g`", true)
        embed.addField("CX","`c/g*a` or `c/g*i`", true)
        embed.addField("AG","`a/g/c`", true)
        embed.addField("AC","`a/c/g`", true)
        embed.addField("AX","`a/g*c`", true)
        embed.addField("IG","`i/g/c`", true)
        embed.addField("IC","`i/c/g`", true)
        embed.addField("IX","`i/g*c`", true)
        embed.addField("SUM","`g+c+a` or `g+c+i`", true)
        embed.addField("SUM4","`g+c+a+i`", true)
        embed.addField("Height","`h/c/g`", true)
        embed.addField("Width","`w/c/g`", true)
        embed.addField("OGC/OGA/OGX/OCG/OCA/OCX/OAG/OAC/OAX","Overlap variants", true)
        embed.addField("TIG/TIC/TIA","trackless variants", true)
    }
}