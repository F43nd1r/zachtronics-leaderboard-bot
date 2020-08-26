package com.faendir.om.discord.jda

import com.faendir.om.discord.commands.Command
import com.faendir.om.discord.utils.reply
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent
import org.springframework.stereotype.Service
import javax.annotation.PreDestroy

@Service
class JdaService(private val jdaProperties: JdaProperties, private val commands: List<Command>) : ListenerAdapter() {
    private lateinit var jda: JDA

    fun start() {
        jda = JDABuilder.createLight(jdaProperties.token, GatewayIntent.GUILD_MESSAGES)
            .addEventListeners(this)
            .build()
        jda.awaitReady()
    }

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        if(!event.author.isBot) {
            val message = event.message.contentRaw
            commands.forEach { command ->
                if(message.startsWith("!${command.name}")) {
                    command.regex.find(message)
                        ?.let { command.handleMessage(event.author, event.channel, event.message, it) }
                        ?: event.channel.reply(event.author, "sorry, could not parse your command. Type `!help` to see the syntax.")
                }
            }
        }
    }

    @PreDestroy
    fun stop() {
        jda.shutdown()
    }
}


