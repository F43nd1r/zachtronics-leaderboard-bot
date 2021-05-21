package com.faendir.zachtronics.bot.main.discord

import com.faendir.zachtronics.bot.main.config.DiscordProperties
import discord4j.core.DiscordClientBuilder
import discord4j.core.GatewayDiscordClient
import discord4j.gateway.intent.Intent
import discord4j.gateway.intent.IntentSet
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DiscordConfiguration(private val discordProperties: DiscordProperties) {

    @Bean
    fun discordClient(): GatewayDiscordClient = DiscordClientBuilder.create(discordProperties.token)
        .build()
        .gateway()
        .setEnabledIntents(IntentSet.of(Intent.DIRECT_MESSAGES, Intent.GUILD_MESSAGES))
        .login()
        .block()!!
}