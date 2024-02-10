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
import com.faendir.zachtronics.bot.model.Submission
import com.faendir.zachtronics.bot.repository.SubmitResult
import com.faendir.zachtronics.bot.utils.orEmpty
import com.faendir.zachtronics.bot.utils.smartFormat
import com.faendir.zachtronics.bot.utils.toMetricsTree
import com.faendir.zachtronics.bot.validation.ValidationResult
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent

abstract class AbstractMultiSubmitCommand<C : Category, P : Puzzle<C>, S : Submission<C, P>, R : Record<C>> :
    AbstractSubmitCommand<C, P, S, R>() {
    override val description = "Submit any number of solutions"

    override fun handleEvent(event: ChatInputInteractionEvent): SafeMessageBuilder {
        val validationResults = parseSubmissions(event)
        return if (validationResults.size == 1) {
            when (val result = validationResults.first()) {
                is ValidationResult.Valid -> submitToRepository(result.submission)
                else -> throw IllegalArgumentException(result.message)
            }
        } else submitAll(validationResults)
    }

    private fun submitAll(validationResults: Collection<ValidationResult<S>>): MultiMessageSafeEmbedMessageBuilder {
        val submissionResults = repository.submitAll(validationResults)

        val successes = submissionResults.count { it is SubmitResult.Success }
        val (title, color) = when {
            successes != 0 -> "Success: $successes solution${if (successes == 1) "" else "s"} added" to Colors.SUCCESS
            submissionResults.any { it is SubmitResult.NothingBeaten || it is SubmitResult.AlreadyPresent } -> "No solutions added" to Colors.UNCHANGED
            else -> "Failure: no solutions added" to Colors.FAILURE
        }

        val embed = MultiMessageSafeEmbedMessageBuilder().title(title).color(color)
        for ((validationResult, submitResult) in validationResults.zip(submissionResults)) {
            val name = when (validationResult) {
                is ValidationResult.Unparseable -> "*Failed*"
                else -> "*${validationResult.submission.puzzle.displayName}*" +
                        ((submitResult as? SubmitResult.Success)?.beatenRecords
                            ?.flatMap { it.categories }
                            ?.takeIf { it.isNotEmpty() }
                            ?.smartFormat(validationResult.submission.puzzle.supportedCategories.toMetricsTree())
                            .orEmpty(prefix = " "))
            }
            val value = when (validationResult) {
                is ValidationResult.Valid<S>, is ValidationResult.Invalid<S> -> {
                    val score = validationResult.submission.score.toDisplayString(DisplayContext.discord())
                    when (submitResult) {
                        is SubmitResult.Success -> "`$score`${validationResult.submission.author.orEmpty(prefix = " by ")} was added.\n${submitResult.message}"
                        is SubmitResult.Updated -> "`$score` was updated."
                        is SubmitResult.AlreadyPresent -> "`$score` was already present."
                        is SubmitResult.NothingBeaten -> "`$score` did not beat anything."
                        is SubmitResult.Failure -> "`$score` failed.\n${submitResult.message}"
                    }
                }
               is ValidationResult.Unparseable -> validationResult.message
            }
            embed.addField(name, value, true)
        }
        return embed
    }

    abstract fun parseSubmissions(event: ChatInputInteractionEvent): Collection<ValidationResult<S>>

    final override fun parseSubmission(event: ChatInputInteractionEvent): S {
        throw NotImplementedError("Unneeded")
    }
}