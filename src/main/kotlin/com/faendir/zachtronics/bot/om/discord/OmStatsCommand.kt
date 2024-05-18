/*
 * Copyright (c) 2024
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

import com.faendir.zachtronics.bot.discord.Colors
import com.faendir.zachtronics.bot.discord.command.Command
import com.faendir.zachtronics.bot.discord.command.security.NotSecured
import com.faendir.zachtronics.bot.discord.embed.MultiMessageSafeEmbedMessageBuilder
import com.faendir.zachtronics.bot.discord.embed.SafeMessageBuilder
import com.faendir.zachtronics.bot.model.DisplayContext
import com.faendir.zachtronics.bot.om.OmQualifier
import com.faendir.zachtronics.bot.om.model.OmCategory
import com.faendir.zachtronics.bot.om.model.OmSubmission
import com.faendir.zachtronics.bot.om.omSolutionOptionBuilder
import com.faendir.zachtronics.bot.om.repository.OmSolutionRepository
import com.faendir.zachtronics.bot.om.validation.createSubmission
import com.faendir.zachtronics.bot.repository.SubmitResult
import com.faendir.zachtronics.bot.utils.embedCategoryRecords
import com.faendir.zachtronics.bot.utils.orEmpty
import com.faendir.zachtronics.bot.utils.smartFormat
import com.faendir.zachtronics.bot.utils.user
import com.roxstudio.utils.CUrl
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import org.springframework.stereotype.Component
import kotlin.jvm.optionals.getOrNull

@Component
@OmQualifier
class OmStatsCommand(private val repository: OmSolutionRepository) : Command.BasicLeaf() {
    override val name = "stats"
    override val description = "Get information about a solution"
    override val ephemeral: Boolean = true

    private val solutionOption = omSolutionOptionBuilder().required().build()
    override val options = listOf(solutionOption)

    override val secured = NotSecured

    override fun handleEvent(event: ChatInputInteractionEvent): SafeMessageBuilder {
        val submission = parseSubmission(event)
        when (val result = repository.submitDryRun(submission)) {
            is SubmitResult.Success -> {
                val beatenCategories: List<OmCategory> = result.beatenRecords.flatMap { it.categories }
                return MultiMessageSafeEmbedMessageBuilder()
                    .title("Stats: *${submission.puzzle.displayName}*")
                    .url(submission.puzzle.link)
                    .color(Colors.SUCCESS)
                    .description(
                        "`${submission.score.toDisplayString(DisplayContext.discord())}`"
                                + (if (beatenCategories.isEmpty()) " would be included in the pareto frontier." else " would be ${
                            beatenCategories.smartFormat(submission.puzzle.supportedCategories)
                        }")
                                + (result.message.orEmpty(prefix = "\n"))
                                + (if (result.beatenRecords.isNotEmpty()) "\nWould beat:" else "")
                    )
                    .embedCategoryRecords(result.beatenRecords, submission.puzzle.supportedCategories)
            }

            is SubmitResult.AlreadyPresent, is SubmitResult.Updated ->
                return MultiMessageSafeEmbedMessageBuilder()
                    .title("Stats: *${submission.puzzle.displayName}*")
                    .url(submission.puzzle.link)
                    .color(Colors.UNCHANGED)
                    .description("`${submission.score.toDisplayString(DisplayContext.discord())}` was already submitted.")

            is SubmitResult.NothingBeaten ->
                return MultiMessageSafeEmbedMessageBuilder()
                    .title("Stats: *${submission.puzzle.displayName}*")
                    .url(submission.puzzle.link)
                    .color(Colors.UNCHANGED)
                    .description("`${submission.score.toDisplayString(DisplayContext.discord())}` is beaten by:")
                    .embedCategoryRecords(result.records, submission.puzzle.supportedCategories)

            is SubmitResult.Failure -> throw IllegalArgumentException(result.message)
        }
    }

    fun parseSubmission(event: ChatInputInteractionEvent): OmSubmission {
        val bytes = try {
            CUrl(solutionOption.get(event).url).exec()
        } catch (e: Exception) {
            throw IllegalArgumentException("Could not load your solution file")
        }
        return createSubmission(null, event.user().let { it.globalName.getOrNull() ?: it.username }, bytes)
    }
}