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

package com.faendir.zachtronics.bot.discord.serialization

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import discord4j.common.util.Snowflake
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandOption.Type.ATTACHMENT
import discord4j.core.`object`.command.ApplicationCommandOption.Type.BOOLEAN
import discord4j.core.`object`.command.ApplicationCommandOption.Type.CHANNEL
import discord4j.core.`object`.command.ApplicationCommandOption.Type.INTEGER
import discord4j.core.`object`.command.ApplicationCommandOption.Type.MENTIONABLE
import discord4j.core.`object`.command.ApplicationCommandOption.Type.NUMBER
import discord4j.core.`object`.command.ApplicationCommandOption.Type.ROLE
import discord4j.core.`object`.command.ApplicationCommandOption.Type.STRING
import discord4j.core.`object`.command.ApplicationCommandOption.Type.SUB_COMMAND
import discord4j.core.`object`.command.ApplicationCommandOption.Type.SUB_COMMAND_GROUP
import discord4j.core.`object`.command.ApplicationCommandOption.Type.UNKNOWN
import discord4j.core.`object`.command.ApplicationCommandOption.Type.USER
import org.springframework.stereotype.Component

@Suppress("LeakingThis")
@Component
class Discord4JJacksonModule : SimpleModule() {
    init {
        addSerializer(ChatInputInteractionEventSerializer())
        addDeserializer(Snowflake::class.java, SnowflakeDeserializer())
    }
}

@JsonSerialize
class ChatInputInteractionEventSerializer : StdSerializer<ChatInputInteractionEvent>(ChatInputInteractionEvent::class.java) {
    override fun serialize(value: ChatInputInteractionEvent, gen: JsonGenerator, provider: SerializerProvider) {
        var options = value.options
        var option = options.firstOrNull()
        while (option?.type == SUB_COMMAND || option?.type ==
            SUB_COMMAND_GROUP
        ) {
            options = option.options
            option = options.firstOrNull()
        }
        serialize(gen, value, options)
    }

    private fun serialize(gen: JsonGenerator, event: ChatInputInteractionEvent, options: List<ApplicationCommandInteractionOption>) {
        gen.writeStartObject()
        options.forEach { option ->
            when (option.type) {
                UNKNOWN -> throw IllegalArgumentException("Unknown application command option")
                SUB_COMMAND, SUB_COMMAND_GROUP -> throw IllegalArgumentException("should not have sub commands at this level")
                STRING -> gen.writeStringField(option.name, option.value.orElse(null)?.asString())
                INTEGER -> option.value.orElse(null)?.asLong()?.also { gen.writeNumberField(option.name, it) } ?: gen.writeNullField(option.name)
                BOOLEAN -> option.value.orElse(null)?.asBoolean()?.also { gen.writeBooleanField(option.name, it) } ?: gen.writeNullField(option.name)
                USER, CHANNEL, ROLE, MENTIONABLE -> option.value.orElse(null)?.asSnowflake()?.asLong()?.also { gen.writeNumberField(option.name, it) }
                    ?: gen.writeNullField(option.name)

                NUMBER -> option.value.orElse(null)?.asDouble()?.also { gen.writeNumberField(option.name, it) } ?: gen.writeNullField(option.name)
                ATTACHMENT -> option.value.orElse(null)?.raw?.let {
                    event.interaction.commandInteraction.orElse(null)?.resolved?.orElse(null)?.getAttachment(Snowflake.of(it))?.orElse(null)
                }?.also {
                    gen.writeObjectField(option.name, Attachment(it.filename, it.contentType.orElse(null), it.size, it.url))
                } ?: gen.writeNullField(option.name)
            }
        }
        gen.writeEndObject()
    }
}

data class Attachment(val filename: String, val contentType: String?, val size: Int, val url: String)

class SnowflakeDeserializer : StdDeserializer<Snowflake>(Snowflake::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Snowflake = Snowflake.of(p.longValue)
}