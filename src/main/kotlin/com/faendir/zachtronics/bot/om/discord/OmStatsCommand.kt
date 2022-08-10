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

import com.faendir.discord4j.command.annotation.ApplicationCommand
import com.faendir.discord4j.command.annotation.Converter
import com.faendir.discord4j.command.annotation.Description
import com.faendir.discord4j.command.parse.ApplicationCommandParser
import com.faendir.zachtronics.bot.discord.Colors
import com.faendir.zachtronics.bot.discord.LinkConverter
import com.faendir.zachtronics.bot.discord.command.AbstractSubCommand
import com.faendir.zachtronics.bot.discord.command.security.NotSecured
import com.faendir.zachtronics.bot.model.DisplayContext
import com.faendir.zachtronics.bot.om.OmQualifier
import com.faendir.zachtronics.bot.om.createSubmission
import com.faendir.zachtronics.bot.om.model.OmCategory
import com.faendir.zachtronics.bot.om.model.OmSubmission
import com.faendir.zachtronics.bot.om.repository.OmSolutionRepository
import com.faendir.zachtronics.bot.repository.SubmitResult
import com.faendir.zachtronics.bot.utils.SafeEmbedMessageBuilder
import com.faendir.zachtronics.bot.utils.SafeMessageBuilder
import com.faendir.zachtronics.bot.utils.embedCategoryRecords
import com.faendir.zachtronics.bot.utils.orEmpty
import com.faendir.zachtronics.bot.utils.smartFormat
import com.faendir.zachtronics.bot.utils.toMetricsTree
import com.faendir.zachtronics.bot.utils.user
import com.roxstudio.utils.CUrl
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent
import discord4j.discordjson.json.ApplicationCommandOptionData
import org.springframework.stereotype.Component

@Component
@OmQualifier
class OmStatsCommand(private val repository: OmSolutionRepository) : AbstractSubCommand<StatsParams>(),
    ApplicationCommandParser<StatsParams, ApplicationCommandOptionData> by StatsParamsParser {
    override val secured = NotSecured

    override fun handleEvent(event: DeferrableInteractionEvent, parameters: StatsParams): SafeMessageBuilder {
        val submission = parseSubmission(event, parameters)
        when (val result = repository.submitDryRun(submission)) {
            is SubmitResult.Success -> {
                val beatenCategories: List<OmCategory> = result.beatenRecords.flatMap { it.categories }
                return SafeEmbedMessageBuilder()
                    .title("Stats: *${submission.puzzle.displayName}*")
                    .color(Colors.SUCCESS)
                    .description(
                        "`${submission.score.toDisplayString(DisplayContext.discord())}`"
                                + (if (beatenCategories.isEmpty()) " would be included in the pareto frontier." else " would be ${
                            beatenCategories.smartFormat(
                                submission.puzzle.supportedCategories.toMetricsTree()
                            )
                        }")
                                + (result.message.orEmpty(prefix = "\n"))
                                + (if (result.beatenRecords.isNotEmpty()) "\nWould beat:" else "")
                    )
                    .embedCategoryRecords(result.beatenRecords, submission.puzzle.supportedCategories)
            }
            is SubmitResult.AlreadyPresent, is SubmitResult.Updated ->
                return SafeEmbedMessageBuilder()
                    .title("Stats: *${submission.puzzle.displayName}*")
                    .color(Colors.UNCHANGED)
                    .description("`${submission.score.toDisplayString(DisplayContext.discord())}` was already submitted.")
            is SubmitResult.NothingBeaten ->
                return SafeEmbedMessageBuilder()
                    .title("Stats: *${submission.puzzle.displayName}*")
                    .color(Colors.UNCHANGED)
                    .description("`${submission.score.toDisplayString(DisplayContext.discord())}` is beaten by:")
                    .embedCategoryRecords(result.records, submission.puzzle.supportedCategories)
            is SubmitResult.Failure -> throw IllegalArgumentException(result.message)
        }
    }

    fun parseSubmission(event: DeferrableInteractionEvent, parameters: StatsParams): OmSubmission {
        val bytes = try {
            CUrl(parameters.solution).exec()
        } catch (e: Exception) {
            throw IllegalArgumentException("Could not load your solution file")
        }
        return createSubmission(null, null, event.user().username, bytes)
    }
}

@ApplicationCommand(name = "stats", description = "Get information about a solution", subCommand = true)
data class StatsParams(
    @Converter(LinkConverter::class)
    @Description("Link to your solution file, can be `m1` to scrape it from your last message")
    val solution: String,
)