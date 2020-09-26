package com.faendir.zachtronics.bot.discord

import com.faendir.zachtronics.bot.config.DiscordProperties
import com.faendir.zachtronics.bot.discord.commands.Command
import com.faendir.zachtronics.bot.model.*
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.requests.restaction.MessageAction
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Service
class DiscordService(private val discordProperties: DiscordProperties, private val commands: List<Command>, private val games: List<Game<*, *, *, *>>) {
    private val adapter = object : ListenerAdapter() {
        override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
            if (!event.author.isBot) {
                val game = games.find { it.discordChannel == event.channel.name } ?: return
                handleMessage(event.message, game) { event.channel.sendMessage(it) }
            }
        }

        override fun onGuildMessageUpdate(event: GuildMessageUpdateEvent) {
            messageCache[event.message.idLong]?.let { response ->
                val game = games.find { it.discordChannel == event.channel.name } ?: return
                handleMessage(event.message, game) { response.editMessage(it) }
            }
        }
    }
    private val jda: JDA = JDABuilder.createLight(discordProperties.token, GatewayIntent.GUILD_MESSAGES).addEventListeners(adapter).build().awaitReady()

    //size bounded message cache
    private val messageCache = object : LinkedHashMap<Long, Message>() {
        override fun removeEldestEntry(eldest: Map.Entry<Long, Message>) = size > 50
    }

    @PostConstruct
    fun onStartup() {
        if (discordProperties.debugMessages) {
            games.forEach { sendMessage(it.discordChannel, "Hello, I\'m now listening to this channel!") }
        }
    }

    private fun <C : Category<C, S, P>, S : Score, P : Puzzle, R : Record<S>> handleMessage(message: Message, game: Game<C, S, P, R>,
                                                                                            createMessageAction: (String) -> MessageAction) {
        commands.forEach { command ->
            if (message.contentRaw.startsWith("!${command.name}")) {
                createMessageAction("${message.author.asMention} ${
                    if (command.requiresRoles.isEmpty() || message.member?.roles?.map { it.name }?.containsAll(command.requiresRoles) == true) {
                        command.handleMessage(game, message)
                    } else {
                        "sorry, you do not have all required roles for this command ${command.requiresRoles.joinToString(separator = "`, `", prefix = "(`", postfix = "`)")}."
                    }
                }").mention(message.author).queue {
                    messageCache[message.idLong] = it
                }
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


