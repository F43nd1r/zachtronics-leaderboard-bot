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