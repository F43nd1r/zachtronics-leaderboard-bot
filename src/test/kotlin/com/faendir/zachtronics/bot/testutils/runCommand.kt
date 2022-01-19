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

package com.faendir.zachtronics.bot.testutils

import com.faendir.discord4j.command.parse.CombinedParseResult
import com.faendir.zachtronics.bot.discord.command.GameCommand
import com.faendir.zachtronics.bot.discord.command.security.DiscordUser
import com.faendir.zachtronics.bot.model.StringFormat
import com.faendir.zachtronics.bot.utils.SafeEmbedMessageBuilder
import com.faendir.zachtronics.bot.utils.SafePlainMessageBuilder
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.`object`.command.ApplicationCommandOption
import discord4j.core.`object`.entity.Member
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.spec.EmbedCreateFields
import discord4j.core.spec.EmbedCreateSpec
import discord4j.core.spec.InteractionFollowupCreateSpec
import discord4j.core.spec.InteractionReplyEditSpec
import discord4j.discordjson.possible.Possible
import io.mockk.every
import io.mockk.mockk
import java.io.Serializable
import java.util.*

@Suppress("ReactiveStreamsUnusedPublisher", "UNCHECKED_CAST")
fun mockGameCommandRun(gameCommand: GameCommand, subCommandName: String, args: Map<String, Serializable>): String {
    val gatewayDiscordClient = mockk<GatewayDiscordClient>(relaxed = true)

    val subCommandOption = mockk<ApplicationCommandInteractionOption>()
    every { subCommandOption.name } returns subCommandName
    every { subCommandOption.type } returns ApplicationCommandOption.Type.SUB_COMMAND
    every { subCommandOption.options } returns args.map { mockOption(it.key, it.value) }

    val ieee = Member(gatewayDiscordClient, mockk(relaxed = true), 0)
    every { ieee.id.asLong() } returns DiscordUser.IEEE12345.id

    val interactionEvent = mockk<ChatInputInteractionEvent>(relaxed = true)
    every { interactionEvent.options } returns listOf(subCommandOption)
    every { interactionEvent.interaction.user } returns ieee
    every { interactionEvent.interaction.member } returns Optional.of(ieee)

    val parseResult = gameCommand.parse(interactionEvent)
    if (parseResult !is CombinedParseResult.Success) return parseResult.toString()

    val (subCommand, parameters) = parseResult.value as GameCommand.SubCommandWithParameters<Any>

    val messageBuilder = subCommand.handleEvent(interactionEvent, parameters)

    val result = when(messageBuilder) {
        is SafePlainMessageBuilder -> listOf(messageBuilder.getContent()) + messageBuilder.getFiles().map { it.name() }
        is SafeEmbedMessageBuilder -> messageBuilder.getEmbeds().flatten().flatMap { it.collectStrings() }
            else -> emptyList()
    }.filterNotNull().joinToString("\n") { it.replace(StringFormat.DISCORD.separator, "/") }
    println(result)
    return result
}

@Suppress("INACCESSIBLE_TYPE")
private fun EmbedCreateSpec.collectStrings(): List<String?> =
    listOf(title().value, description().value) + fields().flatMap { listOf(it.name(), it.value()) } + (footer() as? EmbedCreateFields.Footer)?.text()

val <T> Possible<Optional<T>>.value: T?
    get() = Possible.flatOpt(this).orElse(null)

val <T> Possible<T>.value: T?
    @JvmName("getValue2")
    get() = this.toOptional().orElse(null)

private fun mockOption(name: String, value: Serializable): ApplicationCommandInteractionOption {
    val type = when (value) {
        is Boolean -> ApplicationCommandOption.Type.BOOLEAN
        is Int -> ApplicationCommandOption.Type.INTEGER
        else -> ApplicationCommandOption.Type.STRING
    }
    val optionValue = ApplicationCommandInteractionOptionValue(mockk(), null, type.value, value.toString())
    val option = mockk<ApplicationCommandInteractionOption>()
    every { option.type } returns type
    every { option.name } returns name
    every { option.value } returns Optional.of(optionValue)
    return option
}