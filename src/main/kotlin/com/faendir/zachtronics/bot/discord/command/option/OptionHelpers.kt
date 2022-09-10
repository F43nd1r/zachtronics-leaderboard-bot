/*
 * Copyright (c) 2022
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

package com.faendir.zachtronics.bot.discord.command.option

import com.faendir.zachtronics.bot.utils.fuzzyMatch
import com.faendir.zachtronics.bot.utils.isValidLink
import com.faendir.zachtronics.bot.utils.url
import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.reaction.ReactionEmoji
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toFlux
import java.time.Instant

inline fun <reified T : Enum<T>> enumOptionBuilder(name: String, noinline displayName: T.() -> String) = enumOptionBuilder(name, T::class.java, displayName)

fun <T : Enum<T>> enumOptionBuilder(name: String, type: Class<T>, displayName: T.() -> String): CommandOptionBuilder<String?, T?> {
    val values = type.enumConstants.toList()
    return CommandOptionBuilder.string(name)
        .run {
            if (values.size <= 25) {
                choices(values.associate { it.displayName() to it.name })
                    .convert { name -> values.find { it.name == name } }
            } else {
                autoComplete { partial -> values.fuzzyMatch(partial) { displayName() }.associate { it.displayName() to it.displayName() } }
                    .convert { name ->
                        name?.let {
                            val match = values.fuzzyMatch(it) { displayName() }
                            when (match.size) {
                                0 -> throw IllegalArgumentException("I did not recognize \"$name\".")
                                1 -> match.single()
                                else -> throw IllegalArgumentException("your request for \"$name\" was not precise enough. ${match.size} matches.")
                            }
                        }
                    }
            }
        }
}

fun linkOptionBuilder(name: String) = CommandOptionBuilder.string(name)
    .convert { link ->
        link?.let { l ->
            findLink(
                l.trim(),
                interaction.channel.flatMapMany { channel ->
                    val lastMessageId = channel.lastMessageId.orElseGet { Snowflake.of(Instant.now()) }
                    channel.lastMessage.toFlux().concatWith(channel.getMessagesBefore(lastMessageId).filter { it.id.asLong() != lastMessageId.asLong() })
                }
                    .filter { it.author.isPresent && it.author.get() == interaction.user })
        }
    }


private val linkRegex = Regex("m(?<message>\\d{1,2})(\\.(?<attachment>\\d{1,2}))?")
private fun findLink(input: String, messages: Flux<Message>): String {
    val link = linkRegex.matchEntire(input)?.let { match ->
        val num = match.groups["message"]!!.value.toInt()
        val attachment = match.groups["attachment"]?.value?.toInt()
        val message = messages.elementAt(num - 1).block()!!
        message.attachments.getOrNull(attachment?.minus(1) ?: 0)?.url?.also { message.addReaction(ReactionEmoji.unicode("\uD83D\uDC4D"/* 👍 */)).block() }
            ?: throw IllegalArgumentException("${message.url} had no attachments")
    } ?: input

    if (!isValidLink(link)) {
        throw IllegalArgumentException("\"$link\" is not a valid link")
    }
    return link
}