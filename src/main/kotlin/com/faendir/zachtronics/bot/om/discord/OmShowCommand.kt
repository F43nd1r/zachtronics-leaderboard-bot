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

package com.faendir.zachtronics.bot.om.discord

import com.faendir.zachtronics.bot.discord.command.AbstractShowCommand
import com.faendir.zachtronics.bot.discord.command.option.CommandOptionBuilder
import com.faendir.zachtronics.bot.om.OmQualifier
import com.faendir.zachtronics.bot.om.model.OmCategory
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmRecord
import com.faendir.zachtronics.bot.om.omPuzzleOptionBuilder
import com.faendir.zachtronics.bot.om.repository.OmSolutionRepository
import com.faendir.zachtronics.bot.utils.fuzzyMatch
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import org.springframework.stereotype.Component

@Component
@OmQualifier
class OmShowCommand(override val repository: OmSolutionRepository) :
    AbstractShowCommand<OmCategory, OmPuzzle, OmRecord>() {

    private val puzzleOption = omPuzzleOptionBuilder().required().build()
    private val categoryOption = CommandOptionBuilder.string("category")
        .description("Category. E.g. `GC`, `sum`")
        .required()
        .autoComplete { partial -> OmCategory.entries.fuzzyMatch(partial) { displayName }.map { it.displayName }.distinct().associateWith { it } }
        .build()
    override val options = listOf(puzzleOption, categoryOption)

    private fun findCategoryCandidates(category: String): List<OmCategory> {
        return OmCategory.entries.filter { it.displayName.startsWith(category, ignoreCase = true) }.takeIf { it.isNotEmpty() }
            ?: throw IllegalArgumentException("$category is not a tracked category.")
    }

    override fun findPuzzleAndCategory(event: ChatInputInteractionEvent): Pair<OmPuzzle, OmCategory> {
        val puzzle = puzzleOption.get(event)
        val categories = findCategoryCandidates(categoryOption.get(event))
        val filtered = categories.filter { it.supportsPuzzle(puzzle) }.takeIf { it.isNotEmpty() }
            ?: throw IllegalArgumentException("Category ${categories.first().displayName} does not support ${puzzle.displayName}")
        return puzzle to filtered.first()
    }
}

