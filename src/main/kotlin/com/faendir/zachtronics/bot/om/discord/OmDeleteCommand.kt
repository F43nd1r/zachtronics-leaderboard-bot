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

import com.faendir.zachtronics.bot.discord.Colors
import com.faendir.zachtronics.bot.discord.command.Command
import com.faendir.zachtronics.bot.discord.command.security.DiscordUser
import com.faendir.zachtronics.bot.discord.command.security.DiscordUserSecured
import com.faendir.zachtronics.bot.model.DisplayContext
import com.faendir.zachtronics.bot.om.OmQualifier
import com.faendir.zachtronics.bot.om.omPuzzleOptionBuilder
import com.faendir.zachtronics.bot.om.omScoreOptionBuilder
import com.faendir.zachtronics.bot.om.repository.OmSolutionRepository
import com.faendir.zachtronics.bot.utils.MultiMessageSafeEmbedMessageBuilder
import com.faendir.zachtronics.bot.utils.SafeMessageBuilder
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import org.springframework.stereotype.Component

@Component
@OmQualifier
class OmDeleteCommand(private val repository: OmSolutionRepository) : Command.BasicLeaf() {
    override val name = "delete"
    override val description = "Remove a submission"
    override val secured = DiscordUserSecured(DiscordUser.BOT_OWNERS)

    private val puzzleOption = omPuzzleOptionBuilder().required().build()
    private val scoreOption = omScoreOptionBuilder().required().build()
    override val options = listOf(puzzleOption, scoreOption)

    override fun handleEvent(event: ChatInputInteractionEvent): SafeMessageBuilder {
        val puzzle = puzzleOption.get(event)
        val score = scoreOption.get(event)
        val record = repository.findCategoryHolders(puzzle, true)
            .find { (record, _) -> record.score.toDisplayString().equals(score, ignoreCase = true) }?.record
            ?: throw IllegalArgumentException("Could not find a record for `${score}`")

        repository.delete(record)
        return MultiMessageSafeEmbedMessageBuilder()
            .title("Success: Removed")
            .color(Colors.SUCCESS)
            .description(record.toDisplayString(DisplayContext.discord()))
    }
}