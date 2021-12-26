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
import com.faendir.zachtronics.bot.model.Submission
import com.faendir.zachtronics.bot.repository.SolutionRepository
import com.faendir.zachtronics.bot.repository.SubmitResult
import com.faendir.zachtronics.bot.utils.SafeEmbedMessageBuilder
import com.faendir.zachtronics.bot.validation.ValidationResult
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent
import reactor.core.publisher.Mono

abstract class AbstractArchiveCommand<T, C: Category, S : Submission<C, *>> : AbstractSubCommand<T>() {
    protected abstract val repository: SolutionRepository<*, *, S, *>

    override fun handle(event: DeferrableInteractionEvent, parameters: T): Mono<Void> {
        val validationResults = parseSubmissions(parameters)
        return archiveAll(validationResults).send(event)
    }

    private fun archiveAll(validationResults: Collection<ValidationResult<S>>): SafeEmbedMessageBuilder {
        val submissionResults = repository.submitAll(validationResults)

        val successes = submissionResults.count { it is SubmitResult.Success<*,*> }
        val (title, color) = when {
            successes != 0 -> "Success: $successes solution(s) archived" to Colors.SUCCESS
            submissionResults.any { it is SubmitResult.NothingBeaten<*, *> || it is SubmitResult.AlreadyPresent } -> "No solutions archived" to Colors.UNCHANGED
            else -> "Failure: no solutions archived" to Colors.FAILURE
        }

        val embed = SafeEmbedMessageBuilder().title(title).color(color)
        for ((validationResult, submitResult) in validationResults.zip(submissionResults)) {
            val name = if (validationResult is ValidationResult.Unparseable) "*Failed*" else "*${validationResult.submission.puzzle.displayName}*"
            val value = when (validationResult) {
                is ValidationResult.Valid<S>, is ValidationResult.Invalid<S> -> {
                    val score = validationResult.submission.score.toDisplayString(DisplayContext.discord())
                    when (submitResult) {
                        is SubmitResult.Success -> "`$score` has been archived.\n${submitResult.message}"
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

    abstract fun parseSubmissions(parameters: T): Collection<ValidationResult<S>>
}