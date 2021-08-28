package com.faendir.zachtronics.bot.main.discord

import com.faendir.zachtronics.bot.main.GameContext
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
import org.springframework.boot.SpringApplication
import org.springframework.boot.info.GitProperties
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import kotlin.system.exitProcess

@Service
class DiscordService(
    private val discordClient: GatewayDiscordClient,
    private val gameContexts: List<GameContext>,
    private val gitProperties: GitProperties,
    private val applicationContext: ApplicationContext
) {
    companion object {
        private val logger = LoggerFactory.getLogger(DiscordService::class.java)
    }

    @PostConstruct
    fun init() {
        val requests = gameContexts.map { context ->
            val game = context.game
            val request = ApplicationCommandRequest.builder()
                .name(game.commandName)
                .description(game.displayName)
            for (command in context.commands) {
                if (command.isEnabled) request.addOption(command.data)
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
            logger.error("Fatal error in slash command - shutting down: ", throwable)
            SpringApplication.exit(applicationContext, { 1 })
            Thread.sleep(5000)
            exitProcess(1)
        }.subscribe()
        logger.info("Connected to discord with version ${gitProperties.shortCommitId}")
        discordClient.updatePresence(ClientPresence.online(ClientActivity.playing(gitProperties.shortCommitId))).subscribe()
    }

    private fun handleCommand(event: SlashCommandEvent): Mono<Void> = mono {
        try {
            val gameContext = findGameContext(event)
            val option = event.options.first()
            val command = gameContext.commands.find { it.data.name() == option.name }
                ?: throw IllegalArgumentException("I did not recognize the command \"${option.name}\".")
            if (!command.isReadOnly && !gameContext.game.hasWritePermission(event.interaction.member.map { it as User }.orElse(event.interaction.user)).awaitSingle()) {
                throw IllegalArgumentException("sorry, you do not have the permission to use this command.")
            }
            val result = command.handle(event).awaitSingle()
            event.interactionResponse.createFollowupMessage(result).awaitSingle()
        } catch (e: Exception) {
            logger.info("User command failed", e)
            event.interactionResponse.createFollowupMessage("**Failed**: ${e.message ?: "Something went wrong"}").awaitSingle()
        }
    }.then()

    private fun findGameContext(event: SlashCommandEvent): GameContext {
        val name = event.commandName
        return gameContexts.find { it.game.commandName == name } ?: throw IllegalArgumentException( "I did not recognize the game \"$name\".")
    }

    @PreDestroy
    fun preDestroy() {
        discordClient.logout().block()
    }
}


