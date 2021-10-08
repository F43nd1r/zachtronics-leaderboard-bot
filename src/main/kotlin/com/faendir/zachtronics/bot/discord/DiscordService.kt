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

import com.faendir.zachtronics.bot.discord.command.Secured
import com.faendir.zachtronics.bot.discord.command.TopLevelCommand
import com.fasterxml.jackson.databind.ObjectMapper
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.entity.User
import discord4j.core.`object`.presence.ClientActivity
import discord4j.core.`object`.presence.ClientPresence
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.rest.http.client.ClientException
import kotlinx.coroutines.reactor.awaitSingle
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
    private val commands: List<TopLevelCommand>,
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
        Flux.fromIterable(requests)
            .zipWith(restClient.applicationId.repeat())
            .flatMap { (req, appId) ->
                restClient.applicationService
                    .createGlobalApplicationCommand(appId, req)
                    .doOnError { e -> logger.warn("Unable to create global command", e) }
                    .onErrorResume { Mono.empty() }
            }
            .subscribe()
        discordClient.on(ChatInputInteractionEvent::class.java).flatMap { event ->
            mono {
                logger.info("Acknowledging ${event.commandName} by ${event.interaction.user.username}")
                event.deferReply().awaitSingleOrNull()
                logger.info("Acknowledged ${event.commandName} by ${event.interaction.user.username}")
                handleCommand(event).awaitSingleOrNull()
                logger.info("Handled ${event.commandName} by ${event.interaction.user.username}")
            }
        }.onErrorContinue { throwable, _ ->
            logger.error("Fatal error in slash command - restarting: ", throwable)
            val restartThread = Thread { restartEndpoint.restart() }
            restartThread.isDaemon = false
            restartThread.start()
        }.subscribe()
        logger.info("Connected to discord with version ${gitProperties.shortCommitId}")
        discordClient.updatePresence(ClientPresence.online(ClientActivity.playing(gitProperties.shortCommitId))).subscribe()
    }

    private fun handleCommand(event: ChatInputInteractionEvent): Mono<Void> = mono {
        val command = findCommand(event)
        if (command is Secured && !command.hasExecutionPermission(event.interaction.member.map { it as User }.orElse(event.interaction.user))) {
            throw IllegalArgumentException("sorry, you do not have the permission to use this command.")
        }
        val result = command.handle(event)
        try {
            event.interactionResponse.createFollowupMessage(result).awaitSingle()
        } catch (e: ClientException) {
            logger.info("User command response failed", e)
            throw RuntimeException(
                "Your command was processed successfully, but the response couldn't be displayed: ${
                    e.errorResponse.orElse(null)?.fields?.let {
                        objectMapper.writeValueAsString(
                            it
                        )
                    } ?: e.message
                }")
        }
    }.onErrorResume {
        logger.info("User command failed", it)
        event.interactionResponse.createFollowupMessage("**Failed**: ${it.message ?: "Something went wrong"}")
    }.then()

    private fun findCommand(event: ChatInputInteractionEvent): TopLevelCommand {
        val name = event.commandName
        return commands.find { it.commandName == name } ?: throw IllegalArgumentException("I did not recognize the game \"$name\".")
    }

    @PreDestroy
    fun preDestroy() {
        discordClient.logout().block()
    }
}


