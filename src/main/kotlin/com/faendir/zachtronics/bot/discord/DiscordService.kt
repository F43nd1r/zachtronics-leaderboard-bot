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

import com.faendir.zachtronics.bot.config.DiscordProperties
import com.faendir.zachtronics.bot.discord.command.Command
import com.faendir.zachtronics.bot.utils.editReplyWithFailure
import com.faendir.zachtronics.bot.utils.user
import com.fasterxml.jackson.databind.ObjectMapper
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.`object`.entity.channel.Channel
import discord4j.core.`object`.presence.ClientActivity
import discord4j.core.`object`.presence.ClientPresence
import discord4j.rest.http.client.ClientException
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.boot.info.GitProperties
import org.springframework.cloud.context.restart.RestartEndpoint
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import kotlin.random.Random

@Service
class DiscordService(
    private val discordClient: GatewayDiscordClient,
    private val commands: List<Command.TopLevel>,
    private val discordProperties: DiscordProperties,
    private val gitProperties: GitProperties,
    private val restartEndpoint: RestartEndpoint,
    private val objectMapper: ObjectMapper,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(DiscordService::class.java)
    }

    @PostConstruct
    fun init() {
        val requests = commands.map { it.buildRequest() }
        val restClient = discordClient.restClient
        restClient.applicationId.flatMapMany {
            restClient.applicationService.bulkOverwriteGlobalApplicationCommand(it, requests)
                .doOnError { e -> logger.warn("Unable to create global command", e) }
                .onErrorResume { Mono.empty() }
        }.subscribe()
        discordClient.subscribeEvent<ChatInputInteractionEvent> { event ->
            event.deferReply().awaitSingleOrNull()
            val command = findCommand(event.commandName)
            if (!command.secured.hasExecutionPermission(event, event.user())) {
                throw IllegalArgumentException("sorry, you do not have the permission to use this command.")
            }
            command.handle(event).awaitSingleOrNull()
            logger.info("Handled ${event.commandName} by ${event.interaction.user.username}")
        }
        discordClient.on(ChatInputAutoCompleteEvent::class.java) { event ->
            mono {
                val command = findCommand(event.commandName)
                if (!command.secured.hasExecutionPermission(event, event.user())) {
                    throw IllegalArgumentException("sorry, you do not have the permission to use this command.")
                }
                event.respondWithSuggestions(command.autoComplete(event)?.takeIf { it.size <= 25 } ?: emptyList()).awaitSingleOrNull()
                logger.debug("Autocompleted ${event.commandName} by ${event.interaction.user.username}")
            }
        }.subscribe()
        discordClient.on(MessageCreateEvent::class.java) { event ->
            mono {
                val messages = discordProperties.randomMessages
                if(messages != null) {
                    val channel = event.message.channel.awaitSingleOrNull()
                    if (channel?.type == Channel.Type.DM && event.message.author.orElse(null)?.isBot == false && Random.nextInt(10) == 0) {
                        channel.createMessage(messages.random()).awaitSingleOrNull()
                    }
                }
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

    private fun findCommand(name: String): Command.TopLevel = commands.first { it.name == name }

    @PreDestroy
    fun preDestroy() {
        discordClient.logout().block()
    }
}


