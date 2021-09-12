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

package com.faendir.zachtronics.bot.discord.command

import discord4j.core.`object`.entity.User
import discord4j.core.event.domain.interaction.SlashCommandEvent
import discord4j.discordjson.json.ApplicationCommandRequest
import discord4j.discordjson.json.WebhookExecuteRequest
import discord4j.rest.util.MultipartRequest

interface GameCommand : TopLevelCommand {
    val displayName: String

    val commands: List<Command>

    override fun buildRequest(): ApplicationCommandRequest {
        val request = ApplicationCommandRequest.builder()
            .name(commandName)
            .description(displayName)
        for (command in commands) {
            request.addOption(command.data)
        }
        return request.build()
    }

    override fun handle(event: SlashCommandEvent): MultipartRequest<WebhookExecuteRequest> {
        val option = event.options.first()
        val command = commands.find { it.data.name() == option.name }
            ?: throw IllegalArgumentException("I did not recognize the command \"${option.name}\".")
        if (command is Secured && !command.hasExecutionPermission(event.interaction.member.map { it as User }.orElse(event.interaction.user))) {
            throw IllegalArgumentException("sorry, you do not have the permission to use this command.")
        }
        return command.handle(event)
    }
}