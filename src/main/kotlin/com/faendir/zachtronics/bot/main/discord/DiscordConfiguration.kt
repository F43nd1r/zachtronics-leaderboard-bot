package com.faendir.zachtronics.bot.main.discord

import com.faendir.zachtronics.bot.main.config.DiscordProperties
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DiscordConfiguration(private val discordProperties: DiscordProperties) {
    @Bean(initMethod = "awaitReady", destroyMethod = "shutdown")
    fun jda(): JDA = JDABuilder.createLight(discordProperties.token, GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES).build()

}