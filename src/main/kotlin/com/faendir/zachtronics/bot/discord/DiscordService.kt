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

import com.faendir.discord4j.command.parse.CombinedParseResult
import com.faendir.discord4j.command.parse.SingleParseResult
import com.faendir.zachtronics.bot.discord.command.Secured
import com.faendir.zachtronics.bot.discord.command.TopLevelCommand
import com.faendir.zachtronics.bot.utils.editReplyWithFailure
import com.faendir.zachtronics.bot.utils.user
import com.fasterxml.jackson.databind.ObjectMapper
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.component.ActionRow
import discord4j.core.`object`.component.SelectMenu
import discord4j.core.`object`.presence.ClientActivity
import discord4j.core.`object`.presence.ClientPresence
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent
import discord4j.rest.http.client.ClientException
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.boot.info.GitProperties
import org.springframework.cloud.context.restart.RestartEndpoint
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Service
class DiscordService(
    private val discordClient: GatewayDiscordClient,
    private val commands: List<TopLevelCommand<*>>,
    private val gitProperties: GitProperties,
    private val restartEndpoint: RestartEndpoint,
    private val objectMapper: ObjectMapper,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(DiscordService::class.java)
    }

    private val defferedProcessingCache = mutableMapOf<String, CommandCacheEntry>()

    data class CommandCacheEntry(
        val command: TopLevelCommand<*>,
        val event: ChatInputInteractionEvent,
        var partial: Map<String, Any?>
    )

    private val optionNameCache = mutableMapOf<String, Any?>()

    @PostConstruct
    fun init() {
        val requests = commands.map { it.request }
        val restClient = discordClient.restClient
        Flux.fromIterable(requests)
            .zipWith(restClient.applicationId.repeat())
            .flatMap { (req, appId) ->
                restClient.applicationService
                    .createGlobalApplicationCommand(appId, req)
                    .doOnError { e -> logger.warn("Unable to create global command", e) }
                    .onErrorResume { Mono.empty() }
            }
            .subscribe()
        discordClient.subscribeEvent<ChatInputInteractionEvent> { event ->
            event.deferReply().awaitSingleOrNull()
            val command = findCommand(event.commandName)
            if (command is Secured && !command.hasExecutionPermission(event.user())) {
                throw IllegalArgumentException("sorry, you do not have the permission to use this command.")
            }
            command.handleChatInput(event)
            logger.info("Handled ${event.commandName} by ${event.interaction.user.username}")
        }
        discordClient.subscribeEvent<SelectMenuInteractionEvent> { event ->
            val (id, name) = event.customId.split(":")
            if (defferedProcessingCache.containsKey(id)) {
                val (command, originalEvent, partial) = defferedProcessingCache.getValue(id)
                if (originalEvent.user() == event.user()) {
                    event.deferEdit().awaitSingleOrNull()
                    command.handleSelect(originalEvent, id, partial + (name to optionNameCache.getValue(event.values.first())))
                    logger.debug("Handled select on ${originalEvent.commandName} by ${event.interaction.user.username}")
                } else {
                    event.reply("You can't interact with other persons slash commands.").withEphemeral(true).awaitSingleOrNull()
                }
            } else {
                event.reply("**Failed**: Unknown interaction. Please resend your original command.").withEphemeral(true).awaitSingleOrNull()
            }
        }
        discordClient.on(ChatInputAutoCompleteEvent::class.java) { event ->
            mono {
                val command = findCommand(event.commandName)
                if (command is Secured && !command.hasExecutionPermission(event.user())) {
                    throw IllegalArgumentException("sorry, you do not have the permission to use this command.")
                }
                event.respondWithSuggestions(command.autoComplete(event)?.takeIf { it.size <= 25 } ?: emptyList()).awaitSingleOrNull()
                logger.debug("Autocompleted ${event.commandName} by ${event.interaction.user.username}")
            }
        }.subscribe()
        logger.info("Connected to discord with version ${gitProperties.shortCommitId}")
        discordClient.updatePresence(ClientPresence.online(ClientActivity.playing(gitProperties.shortCommitId))).subscribe()
    }

    private inline fun <reified T : DeferrableInteractionEvent> GatewayDiscordClient.subscribeEvent(noinline handle: suspend (T) -> Unit) {
        val name = T::class.java.simpleName.removeSuffix("InteractionEvent")
        on(T::class.java).flatMap { event ->
            mono {
                try {
                    handle(event)
                } catch (e: ClientException) {
                    logger.info("$name response failed", e)
                    throw RuntimeException(
                        "Your command was processed successfully, but the response couldn't be displayed: ${
                            e.errorResponse.orElse(null)?.fields?.let {
                                @Suppress("BlockingMethodInNonBlockingContext")
                                objectMapper.writeValueAsString(it)
                            } ?: e.message
                        }")
                }
            }.then().onErrorResume {
                logger.info("$name failed", it)
                event.editReplyWithFailure(it.message)
            }
        }.onErrorContinue { throwable, _ ->
            logger.error("Fatal error in $name - restarting: ", throwable)
            val restartThread = Thread { restartEndpoint.restart() }
            restartThread.isDaemon = false
            restartThread.start()
        }.subscribe()
    }

    private suspend fun <T> TopLevelCommand<T>.handleChatInput(event: ChatInputInteractionEvent) {
        when (val parseResult = parse(event)) {
            is CombinedParseResult.Failure -> event.editReplyWithFailure(parseResult.messages.joinToString("\n"))
            is CombinedParseResult.Ambiguous -> {
                val id = commandName + event.commandId.asString()
                defferedProcessingCache[id] = CommandCacheEntry(this, event, parseResult.partialResult)
                event.editReply("Your command was ambiguous. Please select from the options below:")
                    .withComponentsOrNull(parseResult.options.map { (name, ambiguous) -> ambiguous.createSelect(name, id) })
            }
            is CombinedParseResult.Success -> handle(event, parseResult.value)
        }.awaitSingleOrNull()
    }

    private suspend fun <T> TopLevelCommand<T>.handleSelect(event: ChatInputInteractionEvent, id: String, parameters: Map<String, Any?>) {
        val params = map(parameters)
        if (params != null) {
            handle(event, params).awaitSingleOrNull()
            defferedProcessingCache.remove(id)
        } else {
            defferedProcessingCache.getValue(id).partial = parameters
        }
    }

    private fun <T> SingleParseResult.Ambiguous<T>.createSelect(name: String, id: String): ActionRow {
        return ActionRow.of(SelectMenu.of("$id:$name", options.map {
            val s = stringify(it)
            optionNameCache[s] = it
            SelectMenu.Option.of(s, s)
        }))
    }

    private fun findCommand(name: String): TopLevelCommand<*> {
        return commands.find { it.commandName == name } ?: throw IllegalArgumentException("I did not recognize the game \"$name\".")
    }

    @PreDestroy
    fun preDestroy() {
        discordClient.logout().block()
    }
}


