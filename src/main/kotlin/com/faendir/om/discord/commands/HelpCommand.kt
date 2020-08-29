package com.faendir.om.discord.commands

import com.faendir.om.discord.leaderboards.Leaderboard
import com.faendir.om.discord.utils.reply
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class HelpCommand(@Lazy private val commands: List<Command>, @Lazy private val leaderboards: List<Leaderboard>) :
    Command {
    override val regex: Regex = Regex("!help")
    override val name: String = "help"
    override val helpText: String = "This command"

    override fun handleMessage(author: User, channel: TextChannel, message: Message, command: MatchResult) {
        channel.reply(
            author,
            "Available commands:\n```${makeCommandTable()}```\nSupported categories:\n```${makeCategoryList()}```"
        )
    }

    private fun makeCommandTable(): String {
        val nameLength = commands.maxOf { it.name.length } + 4
        val textLength = commands.maxOf { it.helpText.length }
        return commands.joinToString("\n") { it.name.padEnd(nameLength) + it.helpText.padEnd(textLength) }
    }

    private fun makeCategoryList(): String {
        return leaderboards.flatMap { it.supportedCategories }.distinctBy { it.displayName }
            .joinToString("\n") { category -> "${category.displayName} (${category.requiredParts.joinToString("/") { it.key.toString() }})" }
    }
}