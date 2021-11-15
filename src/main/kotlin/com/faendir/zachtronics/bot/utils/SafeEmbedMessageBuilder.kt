/*
 * Copyright (c) 2021
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

import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent
import discord4j.core.spec.EmbedCreateFields
import discord4j.core.spec.EmbedCreateSpec
import discord4j.rest.util.Color
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Mono

class SafeEmbedMessageBuilder {
    private val result = mutableListOf<Pair<EmbedCreateSpec, Int>>()
    private var current = EmbedCreateSpec.builder()
    private var total = 0
    private var fields = 0
    private var color: Color? = null

    fun title(title: String) = apply {
        val safeTitle = title.truncateWithEllipsis(Limits.TITLE).ifEmptyZeroWidthSpace()
        increaseTotal(safeTitle.length)
        current.title(safeTitle)
    }

    fun description(description: String) = apply {
        val safeDescription = description.truncateWithEllipsis(Limits.DESCRIPTION).ifEmptyZeroWidthSpace()
        increaseTotal(safeDescription.length)
        current.description(safeDescription)
    }

    fun addField(name: String, value: String, inline: Boolean = false) = apply {
        if (fields >= Limits.FIELDS) {
            nextBuilder()
        }
        val safeName = name.truncateWithEllipsis(Limits.FIELD_NAME).ifEmptyZeroWidthSpace()
        val safeValue = value.truncateWithEllipsis(Limits.FIELD_VALUE).ifEmptyZeroWidthSpace()
        increaseTotal(safeName.length + safeValue.length)
        current.addField(EmbedCreateFields.Field.of(safeName, safeValue, inline))
        fields++
    }

    fun addFields(fields: Iterable<EmbedCreateFields.Field>) = apply {
        fields.forEach { addField(it.name(), it.value(), it.inline()) }
    }

    fun footer(footer: String, iconUrl: String? = null) = apply {
        val safeFooter = footer.truncateWithEllipsis(Limits.FOOTER).ifEmptyZeroWidthSpace()
        increaseTotal(safeFooter.length)
        current.footer(safeFooter, iconUrl)
    }

    fun author(author: String, url: String? = null, iconUrl: String? = null) = apply {
        val safeAuthor = author.truncateWithEllipsis(Limits.AUTHOR).ifEmptyZeroWidthSpace()
        increaseTotal(safeAuthor.length)
        current.author(safeAuthor, url, iconUrl)
    }

    fun color(color: Color) = apply {
        this.color = color
        current.color(color)
    }

    fun image(image: String) = apply {
        current.image(image)
    }

    private val allowedImageTypes = setOf("gif", "png", "jpg")

    fun link(link: String?) = apply {
        if (link != null) {
            if (allowedImageTypes.contains(link.substringAfterLast(".", ""))) {
                image(link)
            } else {
                addField("Link", "[$link]($link)")
            }
        }
    }

    private fun increaseTotal(by: Int) {
        if (total + by > Limits.TOTAL) {
            nextBuilder()
        }
        total += by
    }

    private fun nextBuilder() {
        result.add(buildCurrent())
        current = EmbedCreateSpec.builder()
        color?.let { current.color(it) }
        total = 0
        fields = 0
    }

    private fun buildCurrent() = current.build() to total

    operator fun plus(other: SafeEmbedMessageBuilder): SafeEmbedMessageBuilder {
        return SafeEmbedMessageBuilder().also {
            it.result += result + buildCurrent() + other.result
            it.current = other.current
            it.total = other.total
            it.fields = other.fields
        }
    }

    fun send(event: DeferrableInteractionEvent): Mono<Void> = mono {
        val embeds = getEmbeds().toMutableList()
        event.editReply().clear().withEmbedsOrNull(embeds.removeFirst()).awaitSingleOrNull()
        for (embed in embeds) {
            event.createFollowup().withEmbeds(embed).awaitSingleOrNull()
        }
    }.then()

    fun send(channel: MessageChannel): Mono<Void> = mono {
        for (embed in getEmbeds()) {
            channel.createMessage().withEmbeds(embed).awaitSingleOrNull()
        }
    }.then()

    private fun getEmbeds(): List<List<EmbedCreateSpec>> {
        if (total > 0) {
            nextBuilder()
        }
        return result.fold(listOf(Pair(emptyList<EmbedCreateSpec>(), 0))) { acc, (spec, total) ->
            val last = acc.last()
            val (list, lastTotal) = last
            if (lastTotal + total <= Limits.TOTAL) {
                acc - last + ((list + spec) to (lastTotal + total))
            } else {
                acc + (listOf(spec) to total)
            }
        }.map { it.first }
    }
}

object Limits {
    const val TITLE = 256
    const val DESCRIPTION = 4096
    const val FIELDS = 25
    const val FIELD_NAME = 256
    const val FIELD_VALUE = 1024
    const val FOOTER = 2048
    const val AUTHOR = 256
    const val TOTAL = 5900
}