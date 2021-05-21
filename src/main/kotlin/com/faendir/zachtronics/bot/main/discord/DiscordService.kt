package com.faendir.zachtronics.bot.main.discord

import com.faendir.zachtronics.bot.main.GameContext
import com.faendir.zachtronics.bot.utils.flatMapFirst
import com.faendir.zachtronics.bot.utils.throwIfEmpty
import discord4j.common.util.Snowflake
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.entity.User
import discord4j.core.event.domain.InteractionCreateEvent
import discord4j.discordjson.json.ApplicationCommandRequest
import discord4j.rest.util.MultipartRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import java.time.Instant

@Service
class DiscordService(discordClient: GatewayDiscordClient, private val gameContexts: List<GameContext>) {
    companion object {
        private val logger = LoggerFactory.getLogger(DiscordService::class.java)
    }

    init {
        val requests = gameContexts.map { context ->
            val game = context.game
            val request = ApplicationCommandRequest.builder()
                .name(game.commandName)
                .description(game.displayName)
            for (command in context.commands) {
                request.addOption(command.buildData())
            }
            request.build()
        }
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
        discordClient.on(InteractionCreateEvent::class.java).flatMap {
            it.acknowledge().then(handleCommand(it))
        }.subscribe()
    }

    private fun handleCommand(event: InteractionCreateEvent): Mono<Void> {
        return Mono.defer {
            findGameContext(event).zipWith(Mono.just(event.interaction.commandInteraction.options.first()))
                .flatMapFirst { gameContext, option ->
                    val command = gameContext.commands.find { it.name == option.name }
                        ?: throw IllegalArgumentException("I did not recognize the command \"${option.name}\".")

                    if (!command.isReadOnly) {
                        gameContext.game.hasWritePermission(event.interaction.member.map { it as User }.orElse(event.interaction.user)).map {
                            if (it) {
                                command
                            } else {
                                throw IllegalArgumentException("sorry, you do not have the permission to use this command.")
                            }
                        }
                    }else {
                        command.toMono()
                    }
                }
                .flatMap { (command, option) ->
                    val previousMessages = event.interaction.channel.flatMapMany { it.getMessagesBefore(Snowflake.of(Instant.now())) }
                        .filter { it.author.isPresent && it.author.get() == event.interaction.user }
                    command.handle(option.options, event.interaction.user, previousMessages) }
                .flatMap { event.interactionResponse.createFollowupMessage(MultipartRequest.ofRequest(it), true) }
                .onErrorResume {
                    logger.info("User command failed", it)
                    event.interactionResponse.createFollowupMessage(it.message ?: "Something went wrong")
                }
                .then()
        }
    }

    private fun findGameContext(event: InteractionCreateEvent): Mono<GameContext> {
        val name = event.interaction.commandInteraction.name
        return Mono.fromCallable { gameContexts.find { it.game.commandName == name } }
            .throwIfEmpty { "I did not recognize the game \"$name\"." }
    }
}


