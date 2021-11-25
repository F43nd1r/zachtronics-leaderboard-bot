/*
 * Copyright (c) 2021
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.faendir.zachtronics.bot.discord

import com.faendir.zachtronics.bot.config.DiscordProperties
import discord4j.core.DiscordClientBuilder
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.presence.ClientPresence
import org.springframework.cloud.context.restart.RestartEndpoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!test")
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