/*
 * Copyright (c) 2025
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

package com.faendir.zachtronics.bot.discord.embed

import com.faendir.zachtronics.bot.discord.DiscordActionCache
import com.faendir.zachtronics.bot.utils.clear
import com.faendir.zachtronics.bot.utils.ifEmptyZeroWidthSpace
import com.faendir.zachtronics.bot.utils.truncateWithEllipsis
import com.faendir.zachtronics.bot.utils.user
import discord4j.core.event.domain.interaction.ButtonInteractionEvent
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent
import discord4j.core.`object`.component.ActionComponent
import discord4j.core.`object`.component.ActionRow
import discord4j.core.`object`.emoji.Emoji
import discord4j.core.`object`.entity.User
import discord4j.core.spec.EmbedCreateFields
import discord4j.core.spec.EmbedCreateSpec
import discord4j.rest.util.Color
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Mono

class PaginatedSafeEmbedMessageBuilder(private val discordActionCache: DiscordActionCache) :
    SafeEmbedMessageBuilder<PaginatedSafeEmbedMessageBuilder> {
    private var title: String? = null
    private var url: String? = null
    private var author: Triple<String, String?, String?>? = null
    private var color: Color? = null
    private val fields: MutableList<Pair<EmbedCreateFields.Field, Int>> = mutableListOf()

    override fun title(title: String) = apply {
        this.title = title.truncateWithEllipsis(EmbedLimits.TITLE).ifEmptyZeroWidthSpace()
    }

    override fun url(url: String) = apply {
        this.url = url
    }

    override fun addField(name: String, value: String, inline: Boolean) = apply {
        val safeName = name.truncateWithEllipsis(EmbedLimits.FIELD_NAME).ifEmptyZeroWidthSpace()
        val safeValue = value.truncateWithEllipsis(EmbedLimits.FIELD_VALUE).ifEmptyZeroWidthSpace()
        fields.add(EmbedCreateFields.Field.of(safeName, safeValue, inline) to (safeName.length + safeValue.length))
    }

    override fun addFields(fields: Iterable<EmbedCreateFields.Field>) = apply {
        fields.forEach { addField(it.name(), it.value(), it.inline()) }
    }

    override fun author(author: String, url: String?, iconUrl: String?) = apply {
        val safeAuthor = author.truncateWithEllipsis(EmbedLimits.AUTHOR).ifEmptyZeroWidthSpace()
        this.author = Triple(safeAuthor, url, iconUrl)
    }

    override fun color(color: Color) = apply {
        this.color = color
    }

    private fun createPagination(user: User, embeds: List<EmbedCreateSpec>): List<ActionComponent> {
        var activeIndex = 0

        suspend fun ButtonInteractionEvent.update() {
            edit().withEmbeds(embeds[activeIndex]).awaitSingleOrNull()
        }

        val actions = listOf(
            discordActionCache.createButton(user, emoji = Emoji.codepoints("U+2B05")) {
                if (--activeIndex < 0) activeIndex = embeds.size - 1
                it.update()
            },
            discordActionCache.createButton(user, emoji = Emoji.codepoints("U+27A1")) {
                if (++activeIndex >= embeds.size) activeIndex = 0
                it.update()
            }
        )
        return actions
    }

    private fun getEmbeds(): List<EmbedCreateSpec> {
        val baseSize = (title?.length ?: 0) + (author?.first?.length ?: 0) + 20
        val availableSize = EmbedLimits.TOTAL - baseSize
        var currentAvailableSize = availableSize
        val result = mutableListOf<List<EmbedCreateFields.Field>>()
        var current = mutableListOf<EmbedCreateFields.Field>()
        for ((field, size) in fields) {
            if (currentAvailableSize - size < 0 || current.size >= EmbedLimits.FIELDS) {
                result.add(current)
                current = mutableListOf()
                currentAvailableSize = availableSize
            }
            current.add(field)
            currentAvailableSize -= size
        }
        result.add(current)
        return result.mapIndexed { index, fields ->
            EmbedCreateSpec.builder()
                .apply {
                    title?.let { title(it) }
                    url?.let { url(it) }
                    author?.let { author(it.first, it.second, it.third) }
                    color?.let { color(it) }
                }
                .addAllFields(fields)
                .footer(EmbedCreateFields.Footer.of("Page ${index + 1} of ${result.size}", null))
                .build()
        }
    }

    override fun send(event: DeferrableInteractionEvent): Mono<Void> = mono {
        val embeds = getEmbeds()
        if (embeds.size <= 1) {
            event.editReply().clear().withEmbeds(embeds.firstOrNull()).awaitSingleOrNull()
        } else {
            val actions = createPagination(event.user(), embeds)
            event.editReply().clear().withEmbeds(embeds[0]).withComponents(ActionRow.of(actions)).awaitSingleOrNull()
        }
    }.then()
}