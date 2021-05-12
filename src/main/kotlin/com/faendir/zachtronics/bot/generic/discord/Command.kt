package com.faendir.zachtronics.bot.generic.discord

import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.entity.User
import discord4j.discordjson.json.ApplicationCommandOptionData
import discord4j.discordjson.json.WebhookExecuteRequest
import reactor.core.publisher.Mono

interface Command {

    val name: String

    val helpText: String

    val isReadOnly: Boolean

    fun handle(options: List<ApplicationCommandInteractionOption>, user: User) : Mono<WebhookExecuteRequest>

    fun buildData() : ApplicationCommandOptionData
}