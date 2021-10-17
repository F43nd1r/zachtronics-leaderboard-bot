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
import com.faendir.zachtronics.bot.utils.user
import com.fasterxml.jackson.databind.ObjectMapper
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.component.ActionRow
import discord4j.core.`object`.component.SelectMenu
import discord4j.core.`object`.presence.ClientActivity
import discord4j.core.`object`.presence.ClientPresence
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
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
    private val optionNameCache = mutableMapOf<String, Any>()

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
        discordClient.on(ChatInputInteractionEvent::class.java).flatMap { event ->
            mono {
                handleCommand(event).awaitSingleOrNull()
                logger.info("Handled ${event.commandName} by ${event.interaction.user.username}")
            }
        }.onErrorContinue { throwable, _ ->
            logger.error("Fatal error in slash command - restarting: ", throwable)
            val restartThread = Thread { restartEndpoint.restart() }
            restartThread.isDaemon = false
            restartThread.start()
        }.subscribe()
        discordClient.on(SelectMenuInteractionEvent::class.java).flatMap { event ->
            mono {
                val (id, name) = event.customId.split(":")
                if (defferedProcessingCache.containsKey(id)) {
                    val (command, originalEvent, partial) = defferedProcessingCache.getValue(id)
                    if(originalEvent.user() == event.user()) {
                        event.deferEdit().awaitSingleOrNull()
                        command.handle(originalEvent, id, partial + (name to optionNameCache.getValue(event.values.first())))
                        logger.info("Handled select on ${originalEvent.commandName} by ${event.interaction.user.username}")
                    } else {
                        event.reply("You can't interact with other persons slash commands.").withEphemeral(true).awaitSingleOrNull()
                    }
                } else {
                    event.reply("**Failed**: Unknown interaction. Please resend your original command.").withEphemeral(true).awaitSingleOrNull()
                }
            }.onErrorResume {
                logger.info("User select failed", it)
                event.interactionResponse.createFollowupMessage("**Failed**: ${it.message ?: "Something went wrong"}").then()
            }
        }.onErrorContinue { throwable, _ ->
            logger.error("Fatal error in select menu - restarting: ", throwable)
            val restartThread = Thread { restartEndpoint.restart() }
            restartThread.isDaemon = false
            restartThread.start()
        }.subscribe()
        logger.info("Connected to discord with version ${gitProperties.shortCommitId}")
        discordClient.updatePresence(ClientPresence.online(ClientActivity.playing(gitProperties.shortCommitId))).subscribe()
    }

    private fun handleCommand(event: ChatInputInteractionEvent): Mono<Void> = mono {
        val command = findCommand(event)
        if (command is Secured && !command.hasExecutionPermission(event.user())) {
            throw IllegalArgumentException("sorry, you do not have the permission to use this command.")
        }
        try {
            command.handle(event).awaitSingleOrNull()
        } catch (e: ClientException) {
            logger.info("User command response failed", e)
            throw RuntimeException(
                "Your command was processed successfully, but the response couldn't be displayed: ${
                    e.errorResponse.orElse(null)?.fields?.let {
                        objectMapper.writeValueAsString(it)
                    } ?: e.message
                }")
        }
    }.onErrorResume {
        logger.info("User command failed", it)
        event.reply("**Failed**: ${it.message ?: "Something went wrong"}")
    }.then()

    private fun <T> TopLevelCommand<T>.handle(event: ChatInputInteractionEvent): Mono<Void> {
        return when (val parseResult = parse(event)) {
            is CombinedParseResult.Failure -> event.reply("**Failed**:\n${parseResult.messages.joinToString("\n")}").withEphemeral(true)
            is CombinedParseResult.Ambiguous -> {
                val id = commandName + event.commandId.asString()
                defferedProcessingCache[id] = CommandCacheEntry(this, event, parseResult.partialResult)
                event.reply("Your command was ambiguous. Please select from the options below:")
                    .withComponents(parseResult.options.map { (name, ambiguous) -> createSelectFor(name, ambiguous, id) })
            }
            is CombinedParseResult.Success -> event.deferReply().then(handle(event, parseResult.value))
        }
    }

    private suspend fun <T> TopLevelCommand<T>.handle(event: ChatInputInteractionEvent, id: String, parameters: Map<String, Any?>) {
        val params = map(parameters)
        if (params != null) {
            params.let { handle(event, it).awaitSingleOrNull() }
            defferedProcessingCache.remove(id)
        } else {
            defferedProcessingCache.getValue(id).partial = parameters
        }
    }

    private fun <T> createSelectFor(
        name: String,
        ambiguous: SingleParseResult.Ambiguous<T>,
        id: String
    ): ActionRow {
        return ActionRow.of(SelectMenu.of("$id:$name", ambiguous.options.map {
            val s = ambiguous.stringify(it)
            optionNameCache[s] = it!!
            SelectMenu.Option.of(s, s)
        }))
    }

    private fun findCommand(event: ChatInputInteractionEvent): TopLevelCommand<*> {
        val name = event.commandName
        return commands.find { it.commandName == name } ?: throw IllegalArgumentException("I did not recognize the game \"$name\".")
    }

    @PreDestroy
    fun preDestroy() {
        discordClient.logout().block()
    }
}


