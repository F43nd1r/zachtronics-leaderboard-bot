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
import com.faendir.zachtronics.bot.model.DisplayContext
import com.faendir.zachtronics.bot.model.Record
import com.faendir.zachtronics.bot.model.StringFormat
import com.faendir.zachtronics.bot.model.Submission
import com.faendir.zachtronics.bot.repository.SolutionRepository
import com.faendir.zachtronics.bot.repository.SubmitResult
import com.faendir.zachtronics.bot.utils.SafeEmbedMessageBuilder
import com.faendir.zachtronics.bot.utils.embedCategoryRecords
import com.faendir.zachtronics.bot.utils.orEmpty
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent
import reactor.core.publisher.Mono

abstract class AbstractSubmitCommand<T, C : Category, S : Submission<C, *>, R : Record<C>> : AbstractSubCommand<T>(), SecuredSubCommand<T> {
    protected abstract val repository: SolutionRepository<C, *, S, R>

    override fun handle(event: DeferrableInteractionEvent, parameters: T): Mono<Void> {
        val submission = parseSubmission(event, parameters)
        return submitToLeaderboards(submission).send(event)
    }

    private fun submitToLeaderboards(submission: S): SafeEmbedMessageBuilder {
        when (val result = repository.submit(submission)) {
            is SubmitResult.Success -> {
                val beatenCategories: List<C> = result.beatenRecords.flatMap { it.categories }
                return SafeEmbedMessageBuilder()
                    .title(
                        "Success: *${submission.puzzle.displayName}* ${
                            beatenCategories.takeIf { it.isNotEmpty() }?.joinToString { it.displayName } ?: "Pareto"
                        }")
                    .color(Colors.SUCCESS)
                    .description(
                        "`${submission.score.toDisplayString(DisplayContext(StringFormat.DISCORD, beatenCategories))}`"
                                + submission.author?.orEmpty(prefix = " by ")
                                + (result.message?.let{"\n${it}"} ?: "")
                                + (if (beatenCategories.isEmpty()) " was included in the pareto frontier." else "")
                                + (if (result.beatenRecords.isNotEmpty()) "\npreviously:" else "")
                    )
                    .embedCategoryRecords(result.beatenRecords)
                    .link(submission.displayLink)
            }
            is SubmitResult.AlreadyPresent ->
                return SafeEmbedMessageBuilder()
                    .title("Already present: *${submission.puzzle.displayName}* `${submission.score.toDisplayString(DisplayContext.discord())}`")
                    .color(Colors.UNCHANGED)
                    .description("No action was taken.")
            is SubmitResult.NothingBeaten ->
                return SafeEmbedMessageBuilder()
                    .title("No Scores beaten by *${submission.puzzle.displayName}* `${submission.score.toDisplayString(DisplayContext.discord())}`")
                    .color(Colors.UNCHANGED)
                    .description("Existing scores:")
                    .embedCategoryRecords(result.records)
            is SubmitResult.Failure -> throw IllegalArgumentException(result.message)
        }
    }

    abstract fun parseSubmission(event: DeferrableInteractionEvent, parameters: T): S
}