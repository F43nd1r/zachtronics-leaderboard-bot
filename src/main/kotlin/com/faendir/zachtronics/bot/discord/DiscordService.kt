package com.faendir.zachtronics.bot.discord

import com.faendir.zachtronics.bot.discord.command.GameCommand
import com.faendir.zachtronics.bot.discord.command.Secured
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.entity.User
import discord4j.core.`object`.presence.ClientActivity
import discord4j.core.`object`.presence.ClientPresence
import discord4j.core.event.domain.interaction.SlashCommandEvent
import discord4j.discordjson.json.ApplicationCommandRequest
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
    private val gameCommands: List<GameCommand>,
    private val gitProperties: GitProperties,
    private val restartEndpoint: RestartEndpoint
) {
    companion object {
        private val logger = LoggerFactory.getLogger(DiscordService::class.java)
    }

    @PostConstruct
    fun init() {
        val requests = gameCommands.map { gameCommand ->
            val request = ApplicationCommandRequest.builder()
                .name(gameCommand.commandName)
                .description(gameCommand.displayName)
            for (command in gameCommand.commands) {
                request.addOption(command.data)
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
        discordClient.on(SlashCommandEvent::class.java).flatMap { event ->
            mono {
                logger.info("Acknowledging ${event.commandName} by ${event.interaction.user.username}")
                event.acknowledge().awaitSingleOrNull()
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

    private fun handleCommand(event: SlashCommandEvent): Mono<Void> = mono {
        val gameContext = findGameCommand(event)
        val option = event.options.first()
        val command = gameContext.commands.find { it.data.name() == option.name }
            ?: throw IllegalArgumentException("I did not recognize the command \"${option.name}\".")
        if (command is Secured && !command.hasExecutionPermission(event.interaction.member.map { it as User }.orElse(event.interaction.user))) {
            throw IllegalArgumentException("sorry, you do not have the permission to use this command.")
        }
        val result = command.handle(event)
        event.interactionResponse.createFollowupMessage(result).awaitSingle()
    }.onErrorResume {
        logger.info("User command failed", it)
        event.interactionResponse.createFollowupMessage("**Failed**: ${it.message ?: "Something went wrong"}")
    }.then()

    private fun findGameCommand(event: SlashCommandEvent): GameCommand {
        val name = event.commandName
        return gameCommands.find { it.commandName == name } ?: throw IllegalArgumentException("I did not recognize the game \"$name\".")
    }

    @PreDestroy
    fun preDestroy() {
        discordClient.logout().block()
    }
}


