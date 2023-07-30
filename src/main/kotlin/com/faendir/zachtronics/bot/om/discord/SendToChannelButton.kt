/*
 * Copyright (c) 2023
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

package com.faendir.zachtronics.bot.om.discord

import com.faendir.zachtronics.bot.discord.StatelessComponent
import com.faendir.zachtronics.bot.utils.filterIsInstance
import com.faendir.zachtronics.bot.utils.user
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.interaction.ButtonInteractionEvent
import discord4j.core.event.domain.interaction.ComponentInteractionEvent
import discord4j.core.`object`.component.ActionRow
import discord4j.core.`object`.component.Button
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.core.spec.EmbedCreateSpec
import discord4j.rest.util.AllowedMentions
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import kotlin.jvm.optionals.getOrNull

@Component
class SendToMainChannelButton(private val discord: GatewayDiscordClient) : StatelessComponent {
    override val id: String = "send-to-main-channel"

    override suspend fun trigger(event: ComponentInteractionEvent) {
        if (event is ButtonInteractionEvent) {
            val message = event.message.getOrNull() ?: return
            val user = event.user()
            discord.guilds.flatMap { it.getChannelById(Channel.MAIN.id) }
                .filterIsInstance<MessageChannel>()
                .flatMap { channel ->
                    channel.createMessage()
                        .withContent(user.mention + " wanted you all to see this:")
                        .withAllowedMentions(AllowedMentions.builder().allowUser(user.id).build())
                        .withEmbeds(message.embeds.map { EmbedCreateSpec.builder().from(it.data).build() })
                }
                .collectList()
                .awaitSingleOrNull()
            message.edit()
                .withComponentsOrNull(emptyList())
                .awaitSingleOrNull()
        }
    }

    companion object {
        fun createAction() = ActionRow.of(Button.primary("send-to-main-channel", "Send to main channel"))
    }
}