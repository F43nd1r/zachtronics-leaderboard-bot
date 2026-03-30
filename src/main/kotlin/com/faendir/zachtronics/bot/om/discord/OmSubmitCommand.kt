/*
 * Copyright (c) 2026
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
import com.faendir.zachtronics.bot.discord.command.option.CommandOptionBuilder
import com.faendir.zachtronics.bot.discord.command.option.displayLinkOptionBuilder
import com.faendir.zachtronics.bot.discord.command.security.NotSecured
import com.faendir.zachtronics.bot.discord.command.security.Secured
import com.faendir.zachtronics.bot.discord.embed.MultiMessageSafeEmbedMessageBuilder
import com.faendir.zachtronics.bot.model.DisplayContext
import com.faendir.zachtronics.bot.om.OmQualifier
import com.faendir.zachtronics.bot.om.model.OmCategory
import com.faendir.zachtronics.bot.om.model.OmSubmission
import com.faendir.zachtronics.bot.om.notifyOf
import com.faendir.zachtronics.bot.om.omSolutionOptionBuilder
import com.faendir.zachtronics.bot.om.repository.OmSolutionRepository
import com.faendir.zachtronics.bot.om.validation.createSubmission
import com.faendir.zachtronics.bot.repository.SubmitResult
import com.faendir.zachtronics.bot.utils.Utils.downloadFile
import com.faendir.zachtronics.bot.utils.embedCategoryRecords
import com.faendir.zachtronics.bot.utils.smartFormat
import com.faendir.zachtronics.bot.utils.url
import com.faendir.zachtronics.bot.utils.user
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.springframework.stereotype.Component

@Component
@OmQualifier
class OmSubmitCommand(private val repository: OmSolutionRepository, private val discordClient: GatewayDiscordClient) : Command.Leaf() {
    override val name = "submit"
    override val description = "Submit a solution, checks only if there is no gif"
    override val ephemeral = true

    private val solutionOption = omSolutionOptionBuilder().required().build()
    private val allowGifUpdateOption = CommandOptionBuilder.boolean("allow-gif-update").build()
    private val gifLinkOption = displayLinkOptionBuilder("gif-link")
        .description("Link to your solution gif/mp4")
        .build()
    private val gifDataOption = CommandOptionBuilder.attachment("gif-data")
        .description("Your solution gif")
        .build()
    override val options = listOf(solutionOption, allowGifUpdateOption, gifLinkOption, gifDataOption)

    override val secured: Secured = NotSecured

    override fun handle(event: ChatInputInteractionEvent) = mono {
        val data = downloadFile(solutionOption.get(event).url).data
        val allowGifUpdate = allowGifUpdateOption.get(event) ?: false
        val gifLink = gifLinkOption.get(event)
        val gifData = gifDataOption.get(event)?.run { downloadFile(this.url).data }

        val submission = createSubmission(data, event.user().username, allowGifUpdate, gifLink, gifData)
        if (gifLink != null || gifData != null)
            submitToRepository(submission).send(event).awaitSingleOrNull()
        else
            checkStats(submission).send(event).awaitSingleOrNull()
    }

    private suspend fun submitToRepository(submission: OmSubmission): MultiMessageSafeEmbedMessageBuilder {
        val result = repository.submit(submission)
        val messages = discordClient.notifyOf(result)
        return when (result) {
            is SubmitResult.Success ->
                MultiMessageSafeEmbedMessageBuilder()
                    .title("Successfully submitted!")
                    .color(Colors.SUCCESS)
                    .description("I have posted about your solution ${messages.firstOrNull()?.url?.let { "[here]($it)" } ?: "nowhere :thinking:"}")

            is SubmitResult.Updated ->
                MultiMessageSafeEmbedMessageBuilder()
                    .title("Successfully Updated!")
                    .color(Colors.SUCCESS)
                    .description("I have posted about your solution ${messages.firstOrNull()?.url?.let { "[here]($it)" } ?: "nowhere :thinking:"}")

            is SubmitResult.AlreadyPresent ->
                MultiMessageSafeEmbedMessageBuilder()
                    .title("Already present: *${submission.puzzle.displayName}* `${submission.score.toDisplayString(DisplayContext.discord())}`")
                    .color(Colors.UNCHANGED)
                    .description("No action was taken.")

            is SubmitResult.NothingBeaten ->
                MultiMessageSafeEmbedMessageBuilder()
                    .title("No Scores beaten by *${submission.puzzle.displayName}* `${submission.score.toDisplayString(DisplayContext.discord())}`")
                    .color(Colors.UNCHANGED)
                    .description("Beaten by:")
                    .embedCategoryRecords(result.records, submission.puzzle.supportedCategories)

            is SubmitResult.Failure -> throw IllegalArgumentException(result.message)
        }
    }

    private fun checkStats(submission: OmSubmission): MultiMessageSafeEmbedMessageBuilder {
        when (val result = repository.submitDryRun(submission)) {
            is SubmitResult.Success -> {
                val beatenCategories: List<OmCategory> = result.beatenRecords.flatMap { it.categories }
                return MultiMessageSafeEmbedMessageBuilder()
                    .title("Stats: *${submission.puzzle.displayName}*")
                    .url(submission.puzzle.link)
                    .color(Colors.SUCCESS)
                    .description(
                        "`${submission.score.toDisplayString(DisplayContext.discord())}`"
                                + (if (beatenCategories.isEmpty()) " would be included in the pareto frontier."
                        else " would be ${beatenCategories.smartFormat(submission.puzzle.supportedCategories)}")
                                + "\nRecord a gif and call submit with it."
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
}
