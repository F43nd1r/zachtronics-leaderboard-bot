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

package com.faendir.zachtronics.bot.om.discord

import com.faendir.discord4j.command.annotation.ApplicationCommand
import com.faendir.discord4j.command.annotation.Converter
import com.faendir.discord4j.command.annotation.Description
import com.faendir.discord4j.command.parse.CombinedParseResult
import com.faendir.zachtronics.bot.discord.command.AbstractShowCommand
import com.faendir.zachtronics.bot.om.OmQualifier
import com.faendir.zachtronics.bot.om.repository.OmSolutionRepository
import com.faendir.zachtronics.bot.om.model.OmCategory
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmRecord
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import org.springframework.stereotype.Component

@Component
@OmQualifier
class OmShowCommand(override val repository: OmSolutionRepository) :
    AbstractShowCommand<Pair<OmPuzzle, OmCategory>, OmCategory, OmPuzzle, OmRecord>() {

    private fun findCategoryCandidates(category: String): List<OmCategory> {
        return OmCategory.values().filter { it.displayName.startsWith(category, ignoreCase = true) }.takeIf { it.isNotEmpty() }
            ?: throw IllegalArgumentException("$category is not a tracked category.")
    }

    override fun buildData() = ShowParser.buildData()

    override fun map(parameters: Map<String, Any?>): Pair<OmPuzzle, OmCategory>? {
        return ShowParser.map(parameters)?.toPuzzleCategoryPair()
    }

    override fun parse(event: ChatInputInteractionEvent): CombinedParseResult<Pair<OmPuzzle, OmCategory>> {
        return when (val parseResult = ShowParser.parse(event)) {
            is CombinedParseResult.Failure -> parseResult.typed()
            is CombinedParseResult.Ambiguous -> try {
                parseResult.partialResult["category"]?.let { findCategoryCandidates(it.toString()) }
                parseResult.typed()
            } catch (e: IllegalArgumentException) {
                CombinedParseResult.Failure(listOf(e.message!!))
            }
            is CombinedParseResult.Success -> try {
                CombinedParseResult.Success(parseResult.value.toPuzzleCategoryPair())
            } catch (e: IllegalArgumentException) {
                CombinedParseResult.Failure(listOf(e.message!!))
            }
        }
    }

    private fun Show.toPuzzleCategoryPair(): Pair<OmPuzzle, OmCategory> {
        val puzzle = puzzle
        val categories = findCategoryCandidates(category)
        val filtered = categories.filter { it.supportsPuzzle(puzzle) }.takeIf { it.isNotEmpty() }
            ?: throw IllegalArgumentException("Category ${categories.first().displayName} does not support ${puzzle.displayName}")
        return puzzle to filtered.first()
    }

    override fun findPuzzleAndCategory(parameters: Pair<OmPuzzle, OmCategory>) = parameters
}

@ApplicationCommand(description = "Show a record", subCommand = true)
data class Show(
    @Description("Puzzle name. Can be shortened or abbreviated. E.g. `stab water`, `PMO`")
    @Converter(PuzzleConverter::class)
    val puzzle: OmPuzzle,
    @Description("Category. E.g. `GC`, `sum`")
    val category: String
)

