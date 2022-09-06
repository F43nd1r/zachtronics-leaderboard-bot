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

import com.faendir.zachtronics.bot.discord.command.AbstractListCommand
import com.faendir.zachtronics.bot.om.OmQualifier
import com.faendir.zachtronics.bot.om.model.OmCategory
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmRecord
import com.faendir.zachtronics.bot.om.omPuzzleOptionBuilder
import com.faendir.zachtronics.bot.om.repository.OmSolutionRepository
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import org.springframework.stereotype.Component

@Component
@OmQualifier
class OmListCommand(override val repository: OmSolutionRepository) : AbstractListCommand<OmCategory, OmPuzzle, OmRecord>() {
    private val puzzleOption = omPuzzleOptionBuilder().required().build()
    override val options = listOf(puzzleOption)


    override fun findPuzzle(event: ChatInputInteractionEvent): OmPuzzle = puzzleOption.get(event)
}

