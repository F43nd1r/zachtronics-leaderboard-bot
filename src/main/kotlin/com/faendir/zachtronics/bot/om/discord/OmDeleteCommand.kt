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
import com.faendir.discord4j.command.annotation.AutoComplete
import com.faendir.discord4j.command.annotation.Converter
import com.faendir.discord4j.command.annotation.Description
import com.faendir.discord4j.command.parse.ApplicationCommandParser
import com.faendir.zachtronics.bot.discord.Colors
import com.faendir.zachtronics.bot.discord.command.AbstractSubCommand
import com.faendir.zachtronics.bot.discord.command.security.DiscordUser
import com.faendir.zachtronics.bot.discord.command.security.DiscordUserSecured
import com.faendir.zachtronics.bot.model.DisplayContext
import com.faendir.zachtronics.bot.om.OmQualifier
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.repository.OmSolutionRepository
import com.faendir.zachtronics.bot.utils.SafeEmbedMessageBuilder
import com.faendir.zachtronics.bot.utils.SafeMessageBuilder
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent
import discord4j.discordjson.json.ApplicationCommandOptionData
import org.springframework.stereotype.Component

@Component
@OmQualifier
class OmDeleteCommand(private val repository: OmSolutionRepository) : AbstractSubCommand<Delete>(),
    ApplicationCommandParser<Delete, ApplicationCommandOptionData> by DeleteParser {
    override val secured = DiscordUserSecured(DiscordUser.BOT_OWNERS)

    override fun handleEvent(event: DeferrableInteractionEvent, parameters: Delete): SafeMessageBuilder {
        val record = repository.findCategoryHolders(parameters.puzzle, true)
            .find { (record, _) -> record.score.toDisplayString().equals(parameters.score, ignoreCase = true) }?.record
            ?: throw IllegalArgumentException("Could not find a record for `${parameters.score}`")

        repository.delete(record)
        return SafeEmbedMessageBuilder()
            .title("Success: Removed")
            .color(Colors.SUCCESS)
            .description(record.toDisplayString(DisplayContext.discord()))
    }
}

@ApplicationCommand(name = "delete", description = "Remove a submission", subCommand = true)
data class Delete(
    @Converter(PuzzleConverter::class)
    @AutoComplete(PuzzleAutoCompletionProvider::class)
    val puzzle: OmPuzzle,
    @Description("full score of the submission, e.g. 65g/80c/12a/4i/4h/4w/12r")
    val score: String,
)