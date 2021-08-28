package com.faendir.zachtronics.bot.generic.discord

import discord4j.core.event.domain.interaction.SlashCommandEvent
import discord4j.discordjson.json.ApplicationCommandOptionData
import discord4j.discordjson.json.WebhookExecuteRequest
import discord4j.rest.util.MultipartRequest
import reactor.core.publisher.Mono

interface Command {

    val isEnabled: Boolean

    val isReadOnly: Boolean

    fun handle(event: SlashCommandEvent): Mono<MultipartRequest<WebhookExecuteRequest>>

    val data: ApplicationCommandOptionData
}