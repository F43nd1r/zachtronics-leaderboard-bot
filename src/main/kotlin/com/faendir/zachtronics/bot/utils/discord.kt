/*
 * Copyright (c) 2026
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

package com.faendir.zachtronics.bot.utils

import com.faendir.zachtronics.bot.discord.Colors
import com.faendir.zachtronics.bot.discord.embed.EmbedLimits
import com.faendir.zachtronics.bot.discord.embed.SafeEmbedMessageBuilder
import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.model.DisplayContext
import com.faendir.zachtronics.bot.model.Record
import com.faendir.zachtronics.bot.model.StringFormat
import com.faendir.zachtronics.bot.repository.CategoryRecord
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent
import discord4j.core.event.domain.interaction.InteractionCreateEvent
import discord4j.core.`object`.command.ApplicationCommandOption
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.User
import discord4j.core.spec.EmbedCreateFields
import discord4j.core.spec.EmbedCreateSpec
import discord4j.core.spec.InteractionReplyEditMono

@Suppress("UNCHECKED_CAST")
fun <B : SafeEmbedMessageBuilder<B>, R : Record<C>?, C : Category> B.embedCategoryRecords(
    records: Iterable<CategoryRecord<R, C>>,
    supportedCategories: Collection<C> = emptyList()
): B {
    return embedRecords(
        records = records.sortedWith(
            compareBy(nullsLast()) { it.categories.minByOrNull { c -> c as Comparable<Any> } as? Comparable<Any> }
        ),
        supportedCategories = supportedCategories,
        formatCategorySpecific = true
    )
}

fun <B : SafeEmbedMessageBuilder<B>, R : Record<C>?, C : Category> B.embedRecords(
    records: Iterable<CategoryRecord<R, C>>,
    supportedCategories: Collection<C> = emptyList(),
    formatCategorySpecific: Boolean = false
): B {
    return this.addFields(
        records.map { (record, categories) ->
            EmbedCreateFields.Field.of(
                categories.smartFormat(supportedCategories).ifEmptyZeroWidthSpace(),
                record?.toDisplayString(DisplayContext(StringFormat.DISCORD, categories.takeIf { formatCategorySpecific })) ?: "none",
                true
            )
        }
    )
}

fun InteractionCreateEvent.user(): User = this.interaction.user

fun InteractionReplyEditMono.clear() = withContentOrNull(null).withComponentsOrNull(null).withEmbedsOrNull(null)

fun DeferrableInteractionEvent.editReplyWithFailure(message: String?) =
    editReply().withEmbeds(
        EmbedCreateSpec.builder()
            .title("Failed")
            .color(Colors.FAILURE)
            .description(message?.truncateWithEllipsis(EmbedLimits.DESCRIPTION) ?: "Something went wrong")
            .build()
    ).then()

val ChatInputInteractionEvent.completeName: String
    get() {
        var ret = commandName
        var opt = options.singleOrNull { it.type == ApplicationCommandOption.Type.SUB_COMMAND }
        while (opt != null) {
            ret += " ${opt.name}"
            opt = opt.options.singleOrNull { it.type == ApplicationCommandOption.Type.SUB_COMMAND }
        }
        return ret
    }

val Message.url
    get() = "https://discord.com/channels/${guild.map { it.id.asString() }.block() ?: "@me"}/${channelId.asString()}/${id.asString()}"
