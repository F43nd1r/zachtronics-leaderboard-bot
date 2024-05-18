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

package com.faendir.zachtronics.bot.om

import com.faendir.zachtronics.bot.discord.Colors
import com.faendir.zachtronics.bot.discord.command.option.CommandOptionBuilder
import com.faendir.zachtronics.bot.discord.command.option.enumOptionBuilder
import com.faendir.zachtronics.bot.discord.embed.MultiMessageSafeEmbedMessageBuilder
import com.faendir.zachtronics.bot.model.DisplayContext
import com.faendir.zachtronics.bot.model.StringFormat
import com.faendir.zachtronics.bot.om.discord.Channel
import com.faendir.zachtronics.bot.om.discord.SendToMainChannelButton
import com.faendir.zachtronics.bot.om.model.OmCategory
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmRecord
import com.faendir.zachtronics.bot.repository.CategoryRecord
import com.faendir.zachtronics.bot.repository.SubmitResult
import com.faendir.zachtronics.bot.utils.embedCategoryRecords
import com.faendir.zachtronics.bot.utils.smartFormat
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.channel.MessageChannel
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory


private val logger = LoggerFactory.getLogger("OM Utils")

fun OmRecord.withCategory(category: OmCategory) = CategoryRecord(this, setOf(category))

fun omPuzzleOptionBuilder() = enumOptionBuilder<OmPuzzle>("puzzle") { displayName }
    .description("Puzzle name. Can be shortened or abbreviated. E.g. `stab water`, `PMO`")

fun omScoreOptionBuilder() = CommandOptionBuilder.string("score")
    .description("full score of the submission, e.g. 100g/35c/9a/12i/2h/3w/T/L@V 100g/6r/9a/12i/2h/3w/T@∞")
    .convert { it.replace("\\u200B".toRegex(), "").trim() }

fun omSolutionOptionBuilder() = CommandOptionBuilder.attachment("solution").description("Your solution file")

suspend fun GatewayDiscordClient.notifyOf(submitResult: SubmitResult<OmRecord, OmCategory>): List<Message> {
    return when (submitResult) {
        is SubmitResult.Success -> {
            val record = submitResult.record!!
            val beatenCategories: List<OmCategory> = submitResult.beatenRecords.flatMap { it.categories }
            if (beatenCategories.isEmpty()) {
                sendDiscordMessage(
                    MultiMessageSafeEmbedMessageBuilder()
                        .title("New Submission: *${record.puzzle.displayName}* Pareto")
                        .url(record.puzzle.link)
                        .color(Colors.SUCCESS)
                        .description(
                            record.toDisplayString(DisplayContext(StringFormat.DISCORD, beatenCategories))
                                    + " was included in the pareto frontier."
                                    + (if (submitResult.beatenRecords.isNotEmpty()) "\nPreviously:" else "")
                        )
                        .embedCategoryRecords(submitResult.beatenRecords, record.puzzle.supportedCategories)
                        .link(record.displayLinkEmbed ?: record.displayLink)
                        .action(SendToMainChannelButton.createAction()), Channel.PARETO
                )
            } else {
                sendDiscordMessage(
                    MultiMessageSafeEmbedMessageBuilder()
                        .title("New Submission: *${record.puzzle.displayName}* ${beatenCategories.smartFormat(record.puzzle.supportedCategories)}")
                        .url(record.puzzle.link)
                        .color(Colors.SUCCESS)
                        .description(
                            record.toDisplayString(DisplayContext(StringFormat.DISCORD, beatenCategories))
                                    + (if (submitResult.beatenRecords.isNotEmpty()) "\nPreviously:" else "")
                        )
                        .embedCategoryRecords(submitResult.beatenRecords, record.puzzle.supportedCategories)
                        .link(record.displayLinkEmbed ?: record.displayLink)
                        .action(SendToMainChannelButton.createAction()), Channel.RECORD
                )
            }
        }

        is SubmitResult.Updated -> {
            val record = submitResult.record!!
            val puzzle = record.puzzle
            sendDiscordMessage(
                MultiMessageSafeEmbedMessageBuilder()
                    .title(
                        "Updated: *${puzzle.displayName}* ${
                            submitResult.oldRecord.categories.takeIf { it.isNotEmpty() }?.smartFormat(puzzle.supportedCategories) ?: "Pareto"
                        }"
                    )
                    .color(Colors.SUCCESS)
                    .description(
                        "${record.toDisplayString(DisplayContext(StringFormat.DISCORD, submitResult.oldRecord.categories))} was updated.\nPreviously:"
                    )
                    .embedCategoryRecords(listOf(submitResult.oldRecord), puzzle.supportedCategories)
                    .link(record.displayLinkEmbed ?: record.displayLink)
                    .action(SendToMainChannelButton.createAction()), Channel.UPDATE
            )
        }

        else -> emptyList()
    }.also {
        if (it.isNotEmpty()) {
            logger.info("Successfully notified discord of $submitResult with ${it.size} messages.")
        } else {
            logger.warn("No discord messages sent for $submitResult")
        }
    }
}

private suspend fun GatewayDiscordClient.sendDiscordMessage(message: MultiMessageSafeEmbedMessageBuilder, channel: Channel): List<Message> {
    val discordChannel = guilds.asFlow().mapNotNull {
        try {
            it.getChannelById(channel.id).awaitSingleOrNull()
        } catch (e: Exception) {
            logger.debug("Failed to get channel $channel in guild ${it.name}", e)
            null
        }
    }.singleOrNull()
    if (discordChannel == null) {
        logger.warn("Did not find channel $channel in any guild, unable to send messages")
        return emptyList()
    }
    if (discordChannel !is MessageChannel) {
        logger.warn("Channel $channel is not a message channel, unable to send messages")
        return emptyList()
    }
    return message.send(discordChannel).awaitSingleOrNull().orEmpty()

}
