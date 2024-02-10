/*
 * Copyright (c) 2024
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

import com.faendir.zachtronics.bot.utils.clear
import com.faendir.zachtronics.bot.utils.truncateWithEllipsis
import com.google.common.annotations.VisibleForTesting
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent
import discord4j.core.spec.MessageCreateFields
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Mono
import java.io.InputStream

class SafePlainMessageBuilder : SafeMessageBuilder {
    private var content: String? = null
    private var files: List<Pair<String, InputStream>> = mutableListOf()

    fun content(content: String) = apply {
        val safeContent = content.truncateWithEllipsis(MessageLimits.CONTENT)
        this.content = safeContent
    }

    fun files(files: List<Pair<String, InputStream>>) = apply {
        this.files = files
    }

    @VisibleForTesting
    internal fun getContent() = content

    @VisibleForTesting
    internal fun getFiles() = files.map { (name, data) -> MessageCreateFields.File.of(name, data) }


    override fun send(event: DeferrableInteractionEvent): Mono<Void> = mono {
        event.editReply()
            .clear()
            .withContentOrNull(getContent())
            .withFiles(getFiles())
            .awaitSingleOrNull()
    }.then()
}

object MessageLimits {
    const val CONTENT = 2000
}