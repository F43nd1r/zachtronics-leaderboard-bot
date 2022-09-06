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

package com.faendir.zachtronics.bot.discord.command

import com.faendir.zachtronics.bot.discord.Colors
import com.faendir.zachtronics.bot.discord.command.security.NotSecured
import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Record
import com.faendir.zachtronics.bot.repository.SolutionRepository
import com.faendir.zachtronics.bot.utils.SafeEmbedMessageBuilder
import com.faendir.zachtronics.bot.utils.SafeMessageBuilder
import com.faendir.zachtronics.bot.utils.embedCategoryRecords
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent

abstract class AbstractListCommand<C : Category, P : Puzzle<C>, R : Record<C>> : Command.BasicLeaf() {
    override val name = "list"
    override val description = "List records"
    override val secured = NotSecured
    abstract val repository: SolutionRepository<C, P, *, R>

    override fun handleEvent(event: ChatInputInteractionEvent): SafeMessageBuilder {
        val puzzle = findPuzzle(event)
        val records = repository.findCategoryHolders(puzzle, includeFrontier = false)
        return SafeEmbedMessageBuilder()
            .title("*${puzzle.displayName}*")
            .apply { puzzle.link?.let { url(it) } }
            .color(Colors.READ)
            .embedCategoryRecords(records, puzzle.supportedCategories)
            .apply {
                val missing = puzzle.supportedCategories - records.flatMap { it.categories }.toSet()
                if (missing.isNotEmpty()) {
                    addField(missing.joinToString(", ") { it.displayName }, "None")
                }
            }
    }

    abstract fun findPuzzle(event: ChatInputInteractionEvent): P
}