package com.faendir.zachtronics.bot.discord.command

import discord4j.core.event.domain.interaction.SlashCommandEvent
import discord4j.discordjson.json.ApplicationCommandRequest
import discord4j.discordjson.json.WebhookExecuteRequest
import discord4j.rest.util.MultipartRequest

interface TopLevelCommand {
    val commandName: String

    fun buildRequest(): ApplicationCommandRequest

    fun handle(event: SlashCommandEvent): MultipartRequest<WebhookExecuteRequest>
}