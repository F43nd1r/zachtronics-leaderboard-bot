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

import com.faendir.zachtronics.bot.discord.DiscordActionCache
import com.faendir.zachtronics.bot.discord.command.AbstractFrontierCommand
import com.faendir.zachtronics.bot.discord.command.AbstractPaginatedFrontierCommand
import com.faendir.zachtronics.bot.discord.command.AbstractPaginatedListCommand
import com.faendir.zachtronics.bot.om.OmQualifier
import com.faendir.zachtronics.bot.om.model.OmCategory
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmRecord
import com.faendir.zachtronics.bot.om.omPuzzleOptionBuilder
import com.faendir.zachtronics.bot.repository.SolutionRepository
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import lombok.Getter
import org.springframework.stereotype.Component

@OmQualifier
@Component
class OmFrontierCommand(override val repository: SolutionRepository<OmCategory, OmPuzzle, *, OmRecord>, discordActionCache: DiscordActionCache) :
    AbstractPaginatedFrontierCommand<OmCategory, OmPuzzle, OmRecord>(discordActionCache) {
    override val puzzleOption = omPuzzleOptionBuilder().required().build()
}