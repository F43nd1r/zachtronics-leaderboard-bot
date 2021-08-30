package com.faendir.zachtronics.bot.discord

import com.faendir.zachtronics.bot.config.DiscordProperties
import discord4j.core.DiscordClientBuilder
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.presence.ClientPresence
import org.springframework.cloud.context.restart.RestartEndpoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DiscordConfiguration {

    @Bean
    fun discordClient(discordProperties: DiscordProperties): GatewayDiscordClient = DiscordClientBuilder.create(discordProperties.token)
        .build()
        .gateway()
        .setInitialPresence { ClientPresence.online() }
        .login()
        .block()!!

    @Bean
    fun restartEndpoint() = RestartEndpoint()
}