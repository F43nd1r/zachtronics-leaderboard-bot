package com.faendir.zachtronics.bot.main.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "discord")
class DiscordProperties {
    lateinit var token: String
    var debugMessages: Boolean = false
}