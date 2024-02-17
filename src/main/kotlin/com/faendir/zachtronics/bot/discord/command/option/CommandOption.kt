/*
 * Copyright (c) 2022
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

package com.faendir.zachtronics.bot.discord.command.option

import discord4j.common.util.Snowflake
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.event.domain.interaction.InteractionCreateEvent
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.`object`.command.ApplicationCommandOption
import discord4j.core.`object`.entity.Attachment
import discord4j.core.`object`.entity.Role
import discord4j.core.`object`.entity.User
import discord4j.core.`object`.entity.channel.Channel
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData
import discord4j.discordjson.json.ApplicationCommandOptionData

/**
 * @param T discord type, must match [type]. Nullity must match [required]
 * @param R conversion result type
 */
data class CommandOption<T, R>(
    val name: String,
    val description: String?,
    val type: ApplicationCommandOption.Type,
    val required: Boolean,
    val choices: Map<String, T & Any>?,
    private val autoComplete: ((partial: T & Any) -> Map<String, T & Any>)?,
    private val getValue: (ApplicationCommandInteractionOptionValue) -> T,
    private val convert: InteractionCreateEvent.(T & Any) -> R,
) {

    @Suppress("UNCHECKED_CAST")
    fun get(event: ChatInputInteractionEvent): R =
        event.leafOptions.find { it.name == name }?.value
            ?.run { if (required) orElseThrow() else orElse(null) }
            ?.let { getValue(it) }
            ?.let { event.convert(it) } as R

    fun autoComplete(event: ChatInputAutoCompleteEvent): List<ApplicationCommandOptionChoiceData>? {
        if (autoComplete == null) return null
        val value = event.focusedOption.value.orElse(null) ?: return null
        val parsedValue = getValue(value) ?: return null
        return autoComplete.invoke(parsedValue).map { (name, value) -> ApplicationCommandOptionChoiceData.builder().name(name).value(value).build() }
    }

    fun build(): ApplicationCommandOptionData =
        ApplicationCommandOptionData.builder()
            .name(name)
            .description(description ?: name)
            .type(type.value)
            .required(required)
            .apply { choices?.let { choices(it.map { (name, value) -> ApplicationCommandOptionChoiceData.builder().name(name).value(value).build() }) } }
            .autocomplete(autoComplete != null)
            .build()
}

private val ChatInputInteractionEvent.leafOptions: List<ApplicationCommandInteractionOption>
    get() {
        var options = options
        var option = options.firstOrNull()
        while (option?.type == ApplicationCommandOption.Type.SUB_COMMAND || option?.type == ApplicationCommandOption.Type.SUB_COMMAND_GROUP
        ) {
            options = option.options
            option = options.firstOrNull()
        }
        return options
    }

class CommandOptionBuilder<T, R> private constructor(
    private val type: ApplicationCommandOption.Type,
    private val name: String,
    private val getValue: (ApplicationCommandInteractionOptionValue) -> T,
    private var convert: InteractionCreateEvent.(T & Any) -> R,
) {
    private var required: Boolean = false
    private var description: String? = null
    private var autoComplete: ((partial: T & Any) -> Map<String, T & Any>)? = null
    private var choices: Map<String, T & Any>? = null


    fun required(): CommandOptionBuilder<T & Any, R & Any> {
        required = true
        @Suppress("UNCHECKED_CAST")
        return this as CommandOptionBuilder<T & Any, R & Any>
    }

    fun description(description: String) = apply { this.description = description }

    fun autoComplete(autoComplete: (partial: T & Any) -> Map<String, T & Any>) = apply { this.autoComplete = autoComplete }

    fun <U> convert(convert: InteractionCreateEvent.(T & Any) -> U): CommandOptionBuilder<T, U> {
        @Suppress("UNCHECKED_CAST")
        return (this as CommandOptionBuilder<T, U>).apply {
            this.convert = convert
        }
    }

    fun choices(choices: Map<String, T & Any>) = apply {
        this.choices = choices
    }

    fun build(): CommandOption<T, R> = CommandOption(name, description, type, required, choices, autoComplete, getValue, convert)

    companion object {
        @JvmStatic
        fun string(name: String) = CommandOptionBuilder<String?, String?>(ApplicationCommandOption.Type.STRING, name, { it.asString() }, { it })

        @JvmStatic
        fun long(name: String) = CommandOptionBuilder<Long?, Long?>(ApplicationCommandOption.Type.INTEGER, name, { it.asLong() }, { it })

        @JvmStatic
        fun double(name: String) = CommandOptionBuilder<Double?, Double?>(ApplicationCommandOption.Type.NUMBER, name, { it.asDouble() }, { it })

        @JvmName("bool")
        @JvmStatic
        fun boolean(name: String) = CommandOptionBuilder<Boolean?, Boolean?>(ApplicationCommandOption.Type.BOOLEAN, name, { it.asBoolean() }, { it })

        @JvmStatic
        fun user(name: String) = CommandOptionBuilder<User?, User?>(ApplicationCommandOption.Type.USER, name, { it.asUser().block()!! }, { it })

        @JvmStatic
        fun channel(name: String) = CommandOptionBuilder<Channel?, Channel?>(ApplicationCommandOption.Type.CHANNEL, name, { it.asChannel().block()!! }, { it })

        @JvmStatic
        fun role(name: String) = CommandOptionBuilder<Role?, Role?>(ApplicationCommandOption.Type.ROLE, name, { it.asRole().block()!! }, { it })

        @JvmStatic
        fun mentionable(name: String) =
            CommandOptionBuilder<Snowflake?, Snowflake?>(ApplicationCommandOption.Type.MENTIONABLE, name, { it.asSnowflake() }, { it })

        @JvmStatic
        fun attachment(name: String) =
            CommandOptionBuilder<Attachment?, Attachment?>(ApplicationCommandOption.Type.ATTACHMENT, name, { it.asAttachment() }, { it })
    }
}