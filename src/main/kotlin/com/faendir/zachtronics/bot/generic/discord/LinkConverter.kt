package com.faendir.zachtronics.bot.generic.discord

import com.faendir.discord4j.command.annotation.OptionConverter
import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.reaction.ReactionEmoji
import discord4j.core.event.domain.interaction.SlashCommandEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.withContext
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.util.*

class LinkConverter : OptionConverter<String> {
    override fun fromString(context: SlashCommandEvent, string: String?): Mono<Optional<String>> = mono {
        string?.trim()?.let { s ->
            Optional.of(findLink(s, context.interaction.channel.flatMapMany { it.getMessagesBefore(it.lastMessageId.orElseGet { Snowflake.of(Instant.now()) }) }
                .filter { it.author.isPresent && it.author.get() == context.interaction.user }))
        } ?: Optional.empty()
    }

    private suspend fun findLink(input: String, messages: Flux<Message>): String {
        val link = if (Regex("m\\d+").matches(input)) {
            val num = input.removePrefix("m").toInt()
            val message = messages.elementAt(num - 1).awaitSingle()
            message.attachments.firstOrNull()?.url?.also { message.addReaction(ReactionEmoji.unicode("\uD83D\uDC4D"/* 👍 */)).awaitSingleOrNull() }
                ?: throw IllegalArgumentException(
                    "https://discord.com/channels/${
                        message.guild.awaitSingle().id.asLong().toString().ifEmpty { "@me" }
                    }/${message.channelId.asLong()}/${message.id.asLong()} had no attachments"
                )
        } else {
            input
        }
        withContext(Dispatchers.IO) {
            checkLink(link)
        }
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