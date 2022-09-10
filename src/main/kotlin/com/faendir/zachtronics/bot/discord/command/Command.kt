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

package com.faendir.zachtronics.bot.discord.command

import com.faendir.zachtronics.bot.discord.command.option.CommandOption
import com.faendir.zachtronics.bot.discord.command.security.Secured
import com.faendir.zachtronics.bot.utils.SafeMessageBuilder
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent
import discord4j.core.event.domain.interaction.InteractionCreateEvent
import discord4j.core.`object`.command.ApplicationCommandOption
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData
import discord4j.discordjson.json.ApplicationCommandOptionData
import discord4j.discordjson.json.ApplicationCommandRequest
import reactor.core.publisher.Mono

sealed interface Command {
    val name: String
    val secured: Secured

    /**
     * this method has to run in less than 3 seconds.
     * Don't do network calls or disk reads here!
     */
    fun ephemeral(event: ChatInputInteractionEvent): Boolean

    /**
     * this method has to run in less than 15 minutes if it wants to respond to the given event.
     */
    fun handle(event: ChatInputInteractionEvent): Mono<Void>

    /**
     * this method has to run in less than 3 seconds.
     * Don't do network calls or disk reads here!
     */
    fun autoComplete(event: ChatInputAutoCompleteEvent): List<ApplicationCommandOptionChoiceData>?

    interface TopLevel : Command {
        fun buildRequest(): ApplicationCommandRequest
    }

    abstract class Leaf : Command {
        open val description: String? = null
        open val ephemeral: Boolean = false
        abstract val options: List<CommandOption<*, *>>

        override fun ephemeral(event: ChatInputInteractionEvent) = ephemeral

        fun buildOptions(): List<ApplicationCommandOptionData> = options.map { it.build() }

        override fun autoComplete(event: ChatInputAutoCompleteEvent): List<ApplicationCommandOptionChoiceData>? {
            val focus = event.focusedOption
            val option = options.find { it.name == focus.name }
            return option?.autoComplete(event)
        }
    }

    abstract class BasicLeaf : Leaf() {
        override fun handle(event: ChatInputInteractionEvent) = handleEvent(event).send(event)

        abstract fun handleEvent(event: ChatInputInteractionEvent): SafeMessageBuilder
    }

    abstract class Group : TopLevel {
        abstract val commands: List<Leaf>

        private fun findCommand(event: InteractionCreateEvent): Leaf {
            val name = event.interaction.commandInteraction.orElseThrow().options.first().name
            return commands.first { it.name == name }
        }

        override val secured = Secured { event, user ->
            findCommand(event).secured.hasExecutionPermission(event, user)
        }

        override fun ephemeral(event: ChatInputInteractionEvent) = findCommand(event).ephemeral(event)

        override fun buildRequest() = ApplicationCommandRequest.builder()
            .name(name)
            .description(name)
            .addAllOptions(commands.map {
                ApplicationCommandOptionData.builder()
                    .name(it.name)
                    .type(ApplicationCommandOption.Type.SUB_COMMAND.value)
                    .description(it.description ?: it.name)
                    .addAllOptions(it.buildOptions())
                    .build()
            }).build()

        override fun handle(event: ChatInputInteractionEvent) = findCommand(event).handle(event)

        override fun autoComplete(event: ChatInputAutoCompleteEvent) = findCommand(event).autoComplete(event)
    }

    abstract class Single : Leaf(), TopLevel {
        override fun buildRequest() = ApplicationCommandRequest.builder()
            .name(name)
            .description(description ?: name)
            .addAllOptions(buildOptions())
            .build()
    }
}

