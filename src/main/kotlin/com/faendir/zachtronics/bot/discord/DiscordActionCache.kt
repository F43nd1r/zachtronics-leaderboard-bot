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

package com.faendir.zachtronics.bot.discord

import com.faendir.zachtronics.bot.utils.user
import discord4j.core.event.domain.interaction.ButtonInteractionEvent
import discord4j.core.event.domain.interaction.ComponentInteractionEvent
import discord4j.core.`object`.component.ActionComponent
import discord4j.core.`object`.component.Button
import discord4j.core.`object`.component.MessageComponent
import discord4j.core.`object`.entity.User
import discord4j.core.`object`.reaction.ReactionEmoji
import discord4j.discordjson.json.ComponentData
import discord4j.discordjson.json.ImmutableComponentData
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import java.util.*

@Component
class DiscordActionCache {
    private val buttonCache = mutableMapOf<String, ButtonCacheEntry>()

    suspend fun trigger(event: ComponentInteractionEvent) {
        event.user()
        when (event) {
            is ButtonInteractionEvent -> {
                val cacheEntry = buttonCache[event.customId]
                if(cacheEntry != null) {
                    if(cacheEntry.user.id == event.user().id) {
                        cacheEntry.callback(event)
                    } else {
                        event.reply().withEphemeral(true).withContent("You are not allowed to interact with other people's slash commands").awaitSingleOrNull()
                    }
                } else {
                    event.reply().withEphemeral(true).withContent("Sorry, you can't interact with this slash command anymore since the bot was restarted.").awaitSingleOrNull()
                }
            }
        }
    }

    fun createButton(
        user: User,
        style: Button.Style = Button.Style.PRIMARY,
        emoji: ReactionEmoji? = null,
        label: String? = null,
        action: suspend (event: ButtonInteractionEvent) -> Unit
    ): ActionComponent {
        val id = createId()
        buttonCache[id] = ButtonCacheEntry(user, action)
        val builder: ImmutableComponentData.Builder = ComponentData.builder()
            .type(MessageComponent.Type.BUTTON.value)
            .style(style.value)
            .customId(id)
        if (emoji != null) builder.emoji(emoji.asEmojiData())
        if (label != null) builder.label(label)

        return object : ActionComponent(builder.build()) {}
    }

    private fun createId() = "${UUID.randomUUID()}/${System.nanoTime()}"
}

private data class ButtonCacheEntry(val user: User, val callback: suspend (event: ButtonInteractionEvent) -> Unit)