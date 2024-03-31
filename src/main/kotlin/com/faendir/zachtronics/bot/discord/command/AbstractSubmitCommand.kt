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
import com.faendir.zachtronics.bot.discord.embed.MultiMessageSafeEmbedMessageBuilder
import com.faendir.zachtronics.bot.discord.embed.SafeMessageBuilder
import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.model.DisplayContext
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Record
import com.faendir.zachtronics.bot.model.StringFormat
import com.faendir.zachtronics.bot.model.Submission
import com.faendir.zachtronics.bot.repository.SolutionRepository
import com.faendir.zachtronics.bot.repository.SubmitResult
import com.faendir.zachtronics.bot.utils.embedCategoryRecords
import com.faendir.zachtronics.bot.utils.orEmpty
import com.faendir.zachtronics.bot.utils.smartFormat
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent

abstract class AbstractSubmitCommand<C : Category, P : Puzzle<C>, S : Submission<C, P>, R : Record<C>> : Command.BasicLeaf() {
    override val name = "submit"
    override val description = "Submit a solution"

    protected abstract val repository: SolutionRepository<C, P, S, R>

    override fun handleEvent(event: ChatInputInteractionEvent): SafeMessageBuilder {
        val submission = parseSubmission(event)
        return submitToRepository(submission)
    }

    protected fun submitToRepository(submission: S): MultiMessageSafeEmbedMessageBuilder {
        when (val result = repository.submit(submission)) {
            is SubmitResult.Success -> {
                val beatenCategories: List<C> = result.beatenRecords.flatMap { it.categories }
                return MultiMessageSafeEmbedMessageBuilder()
                    .title(
                        "Success: *${submission.puzzle.displayName}* ${
                            beatenCategories.takeIf { it.isNotEmpty() }?.smartFormat(submission.puzzle.supportedCategories) ?: "Pareto"
                        }"
                    )
                    .url(submission.puzzle.link)
                    .color(Colors.SUCCESS)
                    .description(
                        "`${submission.score.toDisplayString(DisplayContext(StringFormat.DISCORD, beatenCategories))}`"
                                + submission.author.orEmpty(prefix = " by ")
                                + (if (beatenCategories.isEmpty()) " was included in the pareto frontier." else "")
                                + (result.message.orEmpty(prefix = "\n"))
                                + (if (result.beatenRecords.isNotEmpty()) "\nPreviously:" else "")
                    )
                    .embedCategoryRecords(result.beatenRecords, submission.puzzle.supportedCategories)
                    .link(submission.displayLink)
            }
            is SubmitResult.Updated ->
                return MultiMessageSafeEmbedMessageBuilder()
                    .title("Updated: *${submission.puzzle.displayName}* ${
                        result.oldRecord.categories.takeIf { it.isNotEmpty() }?.smartFormat(submission.puzzle.supportedCategories) ?: "Pareto"
                    }")
                    .url(submission.puzzle.link)
                    .color(Colors.SUCCESS)
                    .description(
                        "`${submission.score.toDisplayString(DisplayContext(StringFormat.DISCORD, result.oldRecord.categories))}`"
                                + (" was updated.")
                                + ("\nPreviously:")
                    )
                    .embedCategoryRecords(listOf(result.oldRecord), submission.puzzle.supportedCategories)
                    .link(submission.displayLink)
            is SubmitResult.AlreadyPresent ->
                return MultiMessageSafeEmbedMessageBuilder()
                    .title("Already present: *${submission.puzzle.displayName}* `${submission.score.toDisplayString(DisplayContext.discord())}`")
                    .url(submission.puzzle.link)
                    .color(Colors.UNCHANGED)
                    .description("No action was taken.")
            is SubmitResult.NothingBeaten ->
                return MultiMessageSafeEmbedMessageBuilder()
                    .title("No Scores beaten by *${submission.puzzle.displayName}* `${submission.score.toDisplayString(DisplayContext.discord())}`")
                    .url(submission.puzzle.link)
                    .color(Colors.UNCHANGED)
                    .description("Beaten by:")
                    .embedCategoryRecords(result.records, submission.puzzle.supportedCategories)
            is SubmitResult.Failure -> throw IllegalArgumentException(result.message)
        }
    }

    abstract fun parseSubmission(event: ChatInputInteractionEvent): S
}
