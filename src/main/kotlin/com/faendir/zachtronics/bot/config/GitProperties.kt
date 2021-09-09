package com.faendir.zachtronics.bot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration("gitAccessProperties")
@ConfigurationProperties(prefix = "git")
class GitProperties {
    lateinit var accessToken: String
    lateinit var username: String
    var webhookSecret: String = ""
}