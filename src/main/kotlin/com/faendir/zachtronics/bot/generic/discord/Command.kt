package com.faendir.zachtronics.bot.generic.discord

import com.faendir.zachtronics.bot.model.Game
import discord4j.core.`object`.entity.User
import discord4j.core.event.domain.interaction.SlashCommandEvent
import discord4j.discordjson.json.ApplicationCommandOptionData
import discord4j.discordjson.json.WebhookExecuteRequest
import discord4j.rest.util.MultipartRequest

interface Command {

    val isEnabled: Boolean

    val isReadOnly: Boolean

    fun handle(event: SlashCommandEvent): MultipartRequest<WebhookExecuteRequest>

    fun hasExecutionPermission(game: Game, user: User): Boolean

    val data: ApplicationCommandOptionData
}