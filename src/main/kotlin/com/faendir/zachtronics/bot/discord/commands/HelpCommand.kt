package com.faendir.zachtronics.bot.discord.commands

import com.faendir.zachtronics.bot.leaderboards.Leaderboard
import com.faendir.zachtronics.bot.model.*
import net.dv8tion.jda.api.entities.Message
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class HelpCommand(@Lazy private val commands: List<Command>) : Command {
    override val name: String = "help"
    override fun helpText(game: Game<*, *, *, *>): String = "This command"
    override val isReadOnly: Boolean = true

    override fun <C : Category<C, S, P>, S : Score, P : Puzzle, R : Record<S>> handleMessage(game: Game<C, S, P, R>, message: Message): String {
        return "Available commands:\n```${makeCommandTable(game)}```\nSupported categories:\n```${makeCategoryList(game.leaderboards)}```"
    }
    private fun makeCommandTable(game: Game<*, *, *, *>): String {
        val nameLength = commands.maxOf { it.name.length } + 4
        val textLength = commands.maxOf { it.helpText(game).length }
        return commands.joinToString("\n") { it.name.padEnd(nameLength) + it.helpText(game).padEnd(textLength) }
    }

    private fun makeCategoryList(leaderboards: List<Leaderboard<*, *, *, *>>): String {
        return leaderboards.flatMap { it.supportedCategories }
            .distinctBy { it.displayName }
            .joinToString("\n") { category -> "${category.displayName} (${category.contentDescription})" }
    }
}