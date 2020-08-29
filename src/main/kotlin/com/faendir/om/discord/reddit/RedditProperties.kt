package com.faendir.om.discord.reddit

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "reddit")
class RedditProperties {
    lateinit var accessToken: String
    lateinit var clientId: String
    lateinit var username: String
    lateinit var password: String
}