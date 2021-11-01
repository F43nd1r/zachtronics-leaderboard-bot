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

package com.faendir.zachtronics.bot.discord.command

import com.faendir.zachtronics.bot.archive.Archive
import com.faendir.zachtronics.bot.discord.Colors
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Solution
import com.faendir.zachtronics.bot.utils.SafeEmbedMessageBuilder
import discord4j.core.event.domain.interaction.InteractionCreateEvent
import discord4j.core.spec.EmbedCreateFields
import reactor.core.publisher.Mono

abstract class AbstractRetrieveCommand<T, P : Puzzle, S: Solution<P>> : AbstractSubCommand<T>() {
    abstract val archive: Archive<P, S>

    override fun handle(event: InteractionCreateEvent, parameters: T): Mono<Void> {
        val puzzle = findPuzzle(parameters)
        val solutions = archive.retrieve(puzzle)
        return SafeEmbedMessageBuilder()
            .title("*${puzzle.displayName}*")
            .color(Colors.READ)
            .addFields(solutions.map { (score, link) ->
                val value = if (link == null) score.toDisplayString() else "[${score.toDisplayString()}]($link)"
                EmbedCreateFields.Field.of("\u200b", value, true)
            })
            .send(event)
    }

    abstract fun findPuzzle(parameters: T): P
}