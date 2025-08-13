/*
 * Copyright (c) 2025
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

import com.faendir.zachtronics.bot.discord.command.security.asDiscordUser
import com.faendir.zachtronics.bot.utils.fuzzyMatch
import com.faendir.zachtronics.bot.utils.isValidLink
import com.faendir.zachtronics.bot.utils.url
import discord4j.common.util.Snowflake
import discord4j.core.event.domain.interaction.InteractionCreateEvent
import discord4j.core.`object`.command.Interaction
import discord4j.core.`object`.emoji.Emoji
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
                        name.let {
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

fun authorOptionBuilder() = CommandOptionBuilder.string("author")
    .description("Solution author, use Community for trivial or collective solutions")
fun solutionOptionBuilder() = dataLinkOptionBuilder("solution")
    .description("Link to the solution file, can be `m1` to scrape it from your last message")
fun imageOptionBuilder() = displayLinkOptionBuilder("image")
    .description("Link to your image of the solution")

/** does not allow storage of discord links, as they expire shortly */
fun displayLinkOptionBuilder(name: String) = CommandOptionBuilder.string(name)
    .convert { link -> resolveLink(link, resolveDiscord = false) }
fun dataLinkOptionBuilder(name: String) = CommandOptionBuilder.string(name)
    .convert { link -> resolveLink(link) }

private val discordLinkRegex = Regex("m(?<message>\\d{1,2})(\\.(?<attachment>\\d{1,2}))?")
fun InteractionCreateEvent.resolveLink(text: String, resolveDiscord: Boolean = true): String {
    var link = text.trim()
    if (resolveDiscord)
        link = discordLinkRegex.matchEntire(link)?.let { match ->
            val num = match.groups["message"]!!.value.toInt()
            val attachment = match.groups["attachment"]?.value?.toInt()
            val message = interaction.messages().elementAt(num - 1).block()!!

            message.attachments.getOrNull(attachment?.minus(1) ?: 0)?.url?.also {
                message.addReaction(emoji(interaction)).block()
            }
                ?: throw IllegalArgumentException("${message.url} had no attachments")
        } ?: link

    if (!isValidLink(link)) {
        throw IllegalArgumentException("\"$link\" is not a valid link")
    }
    return link
}

private fun Interaction.messages() =
    channel.flatMapMany { channel ->
        val lastMessageId = channel.lastMessageId.orElseGet { Snowflake.of(Instant.now()) }
        channel.lastMessage.toFlux().concatWith(
            channel.getMessagesBefore(lastMessageId).filter { it.id.asLong() != lastMessageId.asLong() })
    }
        .filter { it.author.isPresent && it.author.get() == user }

private fun emoji(interaction: Interaction) =
    interaction.user.asDiscordUser()?.getSpecialEmoji?.invoke(interaction.guild.block()) ?: Emoji.unicode("\uD83D\uDC4D"/* 👍 */)