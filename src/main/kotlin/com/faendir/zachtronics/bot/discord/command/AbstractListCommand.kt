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

import com.faendir.zachtronics.bot.discord.Colors
import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Record
import com.faendir.zachtronics.bot.repository.SolutionRepository
import com.faendir.zachtronics.bot.utils.SafeEmbedMessageBuilder
import com.faendir.zachtronics.bot.utils.embedCategoryRecords
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent
import reactor.core.publisher.Mono

abstract class AbstractListCommand<T, C : Category, P : Puzzle, R : Record<C>> : AbstractSubCommand<T>() {
    abstract val repository: SolutionRepository<C, P, *, R>

    override fun handle(event: DeferrableInteractionEvent, parameters: T): Mono<Void> {
        val (puzzle, categories) = findPuzzleAndCategories(parameters)
        val records = repository.findCategoryHolders(puzzle, includeFrontier = false)
        return SafeEmbedMessageBuilder()
            .title("*${puzzle.displayName}*")
            .color(Colors.READ)
            .embedCategoryRecords(records)
            .apply {
                val missing = categories - records.flatMap { it.categories }
                if (missing.isNotEmpty()) {
                    addField(missing.joinToString("/") { it.displayName }, "None")
                }
            }.send(event)
    }

    /** @return pair of Puzzle and all the categories that support it */
    abstract fun findPuzzleAndCategories(parameters: T): Pair<P, List<C>>
}