package com.faendir.om.discord.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "git")
class GitProperties {
    lateinit var accessToken: String
    lateinit var username: String
}