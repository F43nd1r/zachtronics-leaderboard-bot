/*
 * Copyright (c) 2021
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.faendir.zachtronics.bot.discord

import com.faendir.discord4j.command.parse.SingleParseResult
import com.faendir.discord4j.command.parse.StringOptionConverter
import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.reaction.ReactionEmoji
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import reactor.core.publisher.Flux
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant

class LinkConverter : StringOptionConverter<String> {
    override fun fromString(context: ChatInputInteractionEvent, string: String): SingleParseResult<String> {
        return findLink(
            string.trim(),
            context.interaction.channel.flatMapMany { it.getMessagesBefore(it.lastMessageId.orElseGet { Snowflake.of(Instant.now()) }) }
                .filter { it.author.isPresent && it.author.get() == context.interaction.user }
                .onErrorMap {
                    //TODO remove when threads are supported
                    if (it is UnsupportedOperationException) {
                        IllegalArgumentException("mX arguments are not supported in threads.", it)
                    } else {
                        it
                    }
                })
    }

    private fun findLink(input: String, messages: Flux<Message>): SingleParseResult<String> {
        val link = if (Regex("m\\d+").matches(input)) {
            val num = input.removePrefix("m").toInt()
            val message = messages.elementAt(num - 1).block()!!
            message.attachments.firstOrNull()?.url?.also { message.addReaction(ReactionEmoji.unicode("\uD83D\uDC4D"/* 👍 */)).block() }
                ?: return SingleParseResult.Failure(
                    "https://discord.com/channels/${
                        message.guild.block()!!.id.asLong().toString().ifEmpty { "@me" }
                    }/${message.channelId.asLong()}/${message.id.asLong()} had no attachments"
                )
        } else {
            input
        }
        if(!isValid(link) ){
            return SingleParseResult.Failure("\"$link\" is not a valid link")
        }
        return SingleParseResult.Success(link)
    }

    private fun isValid(string: String): Boolean {
        return try {
            val connection = URL(string).openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.setRequestProperty("Accept", "*/*")
            connection.responseCode in (200 until 400) // accept all redirects as well
        } catch (e: Exception) {
            false
        }
    }
}