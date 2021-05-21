package com.faendir.zachtronics.bot.generic.discord

import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.User
import discord4j.discordjson.json.ApplicationCommandOptionData
import discord4j.discordjson.json.WebhookExecuteRequest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface Command {

    /**
     * this name must match the name used in [buildData]
     */
    val name: String

    val isReadOnly: Boolean

    fun handle(options: List<ApplicationCommandInteractionOption>, user: User, previousMessages: Flux<Message>) : Mono<WebhookExecuteRequest>

    fun buildData() : ApplicationCommandOptionData
}