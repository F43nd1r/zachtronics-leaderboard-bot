package com.faendir.zachtronics.bot.discord

import com.faendir.zachtronics.bot.discord.commands.Command
import com.faendir.zachtronics.bot.model.*
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.restaction.MessageAction
import org.springframework.stereotype.Service

@Service
class DiscordService(private val jda: JDA, private val commands: List<Command>, private val games: List<Game<*, *, *, *>>) {
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

    init {
        jda.addEventListener(adapter)
    }

    //size bounded message cache
    private val messageCache = object : LinkedHashMap<Long, Message>() {
        override fun removeEldestEntry(eldest: Map.Entry<Long, Message>) = size > 50
    }

    private fun <C : Category<S, P>, S : Score, P : Puzzle, R : Record<S>> handleMessage(message: Message, game: Game<C, S, P, R>,
                                                                                         createMessageAction: (String) -> MessageAction) {
        commands.forEach { command ->
            if (message.contentRaw.startsWith("!${command.name}")) {
                createMessageAction("${message.author.asMention} ${
                    if (command.isReadOnly || game.hasWritePermission(message.member)) {
                        command.handleMessage(game, message)
                    } else {
                        "sorry, you do not have the permission to use this command."
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
}


