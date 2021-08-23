package com.faendir.zachtronics.bot.main.discord

import com.faendir.zachtronics.bot.main.config.DiscordProperties
import discord4j.core.DiscordClientBuilder
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.presence.ClientActivity
import discord4j.core.`object`.presence.ClientPresence
import discord4j.gateway.intent.Intent
import discord4j.gateway.intent.IntentSet
import org.springframework.boot.info.GitProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DiscordConfiguration {

    @Bean
    fun discordClient(discordProperties: DiscordProperties, gitProperties: GitProperties): GatewayDiscordClient = DiscordClientBuilder.create(discordProperties.token)
        .build()
        .gateway()
        .setInitialPresence { ClientPresence.online(ClientActivity.playing(gitProperties.shortCommitId)) }
        .setEnabledIntents(IntentSet.of(Intent.DIRECT_MESSAGES, Intent.GUILD_MESSAGES))
        .login()
        .block()!!
}