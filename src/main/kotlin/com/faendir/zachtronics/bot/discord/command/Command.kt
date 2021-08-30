package com.faendir.zachtronics.bot.discord.command

import discord4j.core.event.domain.interaction.SlashCommandEvent
import discord4j.discordjson.json.ApplicationCommandOptionData
import discord4j.discordjson.json.WebhookExecuteRequest
import discord4j.rest.util.MultipartRequest

interface Command {
    fun handle(event: SlashCommandEvent): MultipartRequest<WebhookExecuteRequest>

    val data: ApplicationCommandOptionData
}