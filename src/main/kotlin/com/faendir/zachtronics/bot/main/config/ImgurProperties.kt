package com.faendir.zachtronics.bot.main.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "imgur")
class ImgurProperties {
    lateinit var clientId: String
}