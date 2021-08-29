package com.faendir.zachtronics.bot.generic.discord

import com.faendir.discord4j.command.annotation.OptionConverter
import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.reaction.ReactionEmoji
import discord4j.core.event.domain.interaction.SlashCommandEvent
import reactor.core.publisher.Flux
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant

class LinkConverter : OptionConverter<String> {
    override fun fromString(context: SlashCommandEvent, string: String): String {
        return findLink(
            string.trim(),
            context.interaction.channel.flatMapMany { it.getMessagesBefore(it.lastMessageId.orElseGet { Snowflake.of(Instant.now()) }) }
                .filter { it.author.isPresent && it.author.get() == context.interaction.user })
    }

    private fun findLink(input: String, messages: Flux<Message>): String {
        val link = if (Regex("m\\d+").matches(input)) {
            val num = input.removePrefix("m").toInt()
            val message = messages.elementAt(num - 1).block()!!
            message.attachments.firstOrNull()?.url?.also { message.addReaction(ReactionEmoji.unicode("\uD83D\uDC4D"/* 👍 */)).block() }
                ?: throw IllegalArgumentException(
                    "https://discord.com/channels/${
                        message.guild.block()!!.id.asLong().toString().ifEmpty { "@me" }
                    }/${message.channelId.asLong()}/${message.id.asLong()} had no attachments"
                )
        } else {
            input
        }
        checkLink(link)
        return link
    }

    private fun checkLink(string: String) {
        val valid = try {
            val connection = URL(string).openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.setRequestProperty("Accept", "*/*")
            connection.responseCode in (200 until 400) // accept all redirects as well
        } catch (e: Exception) {
            false
        }
        if (!valid) {
            throw IllegalArgumentException("\"$string\" is not a valid link")
        }
    }
}