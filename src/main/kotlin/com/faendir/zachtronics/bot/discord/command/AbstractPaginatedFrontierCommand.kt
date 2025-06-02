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

package com.faendir.zachtronics.bot.discord.command

import com.faendir.zachtronics.bot.discord.Colors
import com.faendir.zachtronics.bot.discord.DiscordActionCache
import com.faendir.zachtronics.bot.discord.command.option.CommandOption
import com.faendir.zachtronics.bot.discord.command.security.NotSecured
import com.faendir.zachtronics.bot.discord.embed.PaginatedSafeEmbedMessageBuilder
import com.faendir.zachtronics.bot.discord.embed.SafeMessageBuilder
import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Record
import com.faendir.zachtronics.bot.repository.SolutionRepository
import com.faendir.zachtronics.bot.utils.embedRecords
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent

abstract class AbstractPaginatedFrontierCommand<C : Category, P : Puzzle<C>, R : Record<C>> : Command.BasicLeaf() {
    override val name = "frontier"
    override val description = "Displays the whole pareto frontier"
    override val secured = NotSecured
    protected abstract val puzzleOption: CommandOption<String, P>
    override val options: List<CommandOption<*, *>>
        get() = listOf(puzzleOption)
    abstract val discordActionCache: DiscordActionCache
    abstract val repository: SolutionRepository<C, P, *, R>

    override fun handleEvent(event: ChatInputInteractionEvent): SafeMessageBuilder {
        val puzzle = puzzleOption.get(event)
        val records = repository.findCategoryHolders(puzzle, includeFrontier = true)
        return PaginatedSafeEmbedMessageBuilder(discordActionCache)
            .title("*${puzzle.displayName}*")
            .url(puzzle.link)
            .color(Colors.READ)
            .embedRecords(records, puzzle.supportedCategories)
    }
}