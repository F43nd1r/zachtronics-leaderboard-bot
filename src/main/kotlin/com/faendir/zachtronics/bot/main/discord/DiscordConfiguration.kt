package com.faendir.zachtronics.bot.main.discord

import com.faendir.zachtronics.bot.main.config.DiscordProperties
import discord4j.core.DiscordClientBuilder
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.presence.ClientActivity
import discord4j.core.`object`.presence.ClientPresence
import discord4j.gateway.intent.Intent
import discord4j.gateway.intent.IntentSet
import org.slf4j.LoggerFactory
import org.springframework.boot.info.GitProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DiscordConfiguration {
    companion object {
        private val logger = LoggerFactory.getLogger(DiscordService::class.java)
    }

    @Bean
    fun discordClient(discordProperties: DiscordProperties, gitProperties: GitProperties): GatewayDiscordClient = DiscordClientBuilder.create(discordProperties.token)
        .build()
        .gateway()
        .setInitialPresence {
            logger.info("Connecting to discord with version ${gitProperties.shortCommitId}")
            ClientPresence.online(ClientActivity.playing(gitProperties.shortCommitId))
        }
        .setEnabledIntents(IntentSet.of(Intent.DIRECT_MESSAGES, Intent.GUILD_MESSAGES))
        .login()
        .block()!!
}