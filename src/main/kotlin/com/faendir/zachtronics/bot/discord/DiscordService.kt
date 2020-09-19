package com.faendir.zachtronics.bot.discord

import com.faendir.zachtronics.bot.config.DiscordProperties
import com.faendir.zachtronics.bot.discord.commands.Command
import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.model.Game
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Score
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent
import org.springframework.stereotype.Service
import javax.annotation.PreDestroy

@Service
class DiscordService(discordProperties: DiscordProperties, private val commands: List<Command>, games: List<Game<*, *, *>>) {
    private val adapter = object : ListenerAdapter() {
        override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
            if (!event.author.isBot) {
                val game = games.find { it.discordChannel == event.channel.name } ?: return
                handleMessage(event, game)
            }
        }
    }
    private val jda: JDA = JDABuilder.createLight(discordProperties.token, GatewayIntent.GUILD_MESSAGES).addEventListeners(adapter).build().awaitReady()

    private fun <C : Category<C, S, P>, S : Score, P : Puzzle> handleMessage(event: GuildMessageReceivedEvent, game: Game<C, S, P>) {
        val message = event.message.contentRaw
        commands.forEach { command ->
            if (message.startsWith("!${command.name}")) {
                event.channel.sendMessage("${event.author.asMention} ${
                    if (command.requiresRoles.isEmpty() || event.member?.roles?.map { it.name }?.containsAll(command.requiresRoles) == true) {
                        command.regex.find(message)?.let {
                            command.handleMessage(game, event.author, event.channel, event.message, it)
                        } ?: "sorry, could not parse your command. Type `!help` to see the syntax."
                    } else {
                        "sorry, you do not have all required roles for this command ${command.requiresRoles.joinToString(separator = "`, `", prefix = "(`", postfix = "`)")}."
                    }
                }").mention(event.author).queue()
            }
        }
    }

    fun sendMessage(channel: String, message: String) {
        jda.guilds.forEach { guild -> guild.channels.filterIsInstance<TextChannel>().find { it.name == channel }?.sendMessage(message)?.queue() }
    }

    @PreDestroy
    fun stop() {
        jda.shutdown()
    }
}


