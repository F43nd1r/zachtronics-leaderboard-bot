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
import com.faendir.zachtronics.bot.model.DisplayContext
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Record
import com.faendir.zachtronics.bot.model.StringFormat
import com.faendir.zachtronics.bot.repository.SolutionRepository
import com.faendir.zachtronics.bot.utils.Markdown
import com.faendir.zachtronics.bot.utils.MultiMessageSafeEmbedMessageBuilder
import com.faendir.zachtronics.bot.utils.SafeMessageBuilder
import com.faendir.zachtronics.bot.utils.SafePlainMessageBuilder
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent

abstract class AbstractShowCommand<C : Category, P : Puzzle<C>, R : Record<C>> : Command.BasicLeaf() {
    override val name = "show"
    override val description = "Show a record"
    override val secured = NotSecured
    abstract val repository: SolutionRepository<C, P, *, R>

    override fun handleEvent(event: ChatInputInteractionEvent): SafeMessageBuilder {
        val (puzzle, category) = findPuzzleAndCategory(event)
        val record = repository.find(puzzle, category)
        return if (record != null) {
            val lines = mutableListOf(
                Markdown.linkOrText("***${puzzle.displayName}***", puzzle.link, embed = false),
                "**${category.displayName}**",
                record.toDisplayString(DisplayContext(StringFormat.DISCORD, category))
            )
            val fullScore = record.score.toDisplayString(DisplayContext.discord())
            val smartScore = record.score.toDisplayString(DisplayContext(StringFormat.DISCORD, category))
            if (fullScore != smartScore) {
                lines.add("**Full Score**")
                lines.add(fullScore)
            }
            SafePlainMessageBuilder()
                .content(lines.joinToString("\n"))
                .files(record.attachments())
        } else {
            MultiMessageSafeEmbedMessageBuilder()
                .title("No score")
                .description("sorry, there is no score for ${puzzle.displayName} ${category.displayName}.")
                .color(Colors.UNCHANGED)
        }
    }

    abstract fun findPuzzleAndCategory(event: ChatInputInteractionEvent): Pair<P, C>
}