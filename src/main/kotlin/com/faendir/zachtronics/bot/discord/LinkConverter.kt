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

import com.faendir.discord4j.command.parse.OptionConverter
import com.faendir.discord4j.command.parse.SingleParseResult
import com.faendir.zachtronics.bot.utils.isValidLink
import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.reaction.ReactionEmoji
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import reactor.core.publisher.Flux
import java.time.Instant

class LinkConverter : OptionConverter<String, String> {
    override fun fromValue(context: ChatInputInteractionEvent, value: String): SingleParseResult<String> {
        return findLink(
            value.trim(),
            context.interaction.channel.flatMapMany { it.getMessagesBefore(it.lastMessageId.orElseGet { Snowflake.of(Instant.now()) }) }
                .filter { it.author.isPresent && it.author.get() == context.interaction.user })
    }

    private fun findLink(input: String, messages: Flux<Message>): SingleParseResult<String> {
        val link = if (Regex("m\\d{1,2}").matches(input)) {
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
        if (!isValidLink(link)) {
            return SingleParseResult.Failure("\"$link\" is not a valid link")
        }
        return SingleParseResult.Success(link)
    }
}