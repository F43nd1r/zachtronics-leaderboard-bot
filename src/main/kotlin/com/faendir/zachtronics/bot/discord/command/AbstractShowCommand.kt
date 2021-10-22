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

import com.faendir.zachtronics.bot.leaderboards.Leaderboard
import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Record
import com.faendir.zachtronics.bot.utils.clear
import discord4j.core.event.domain.interaction.InteractionCreateEvent
import discord4j.core.spec.MessageCreateFields
import reactor.core.publisher.Mono

abstract class AbstractShowCommand<T, C : Category, P : Puzzle, R : Record> : AbstractSubCommand<T>() {
    abstract val leaderboards: List<Leaderboard<C, P, R>>

    override fun handle(event: InteractionCreateEvent, parameters: T): Mono<Void> {
        val (puzzle, category) = findPuzzleAndCategory(parameters)
        val record = leaderboards.asSequence().mapNotNull { it.get(puzzle, category) }.firstOrNull()
            ?: throw IllegalArgumentException("sorry, there is no score for ${puzzle.displayName} ${category.displayName}.")
        return event.editReply().clear()
            .withContentOrNull("*${puzzle.displayName}* **${category.displayName}**\n${record.toShowDisplayString()}")
            .withFiles(record.attachments().map { (name, data) -> MessageCreateFields.File.of(name, data) })
            .then()
    }

    abstract fun findPuzzleAndCategory(parameters: T): Pair<P, C>
}