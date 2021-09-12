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

package com.faendir.zachtronics.bot.om.discord

import com.faendir.discord4j.command.annotation.ApplicationCommand
import com.faendir.discord4j.command.annotation.Converter
import com.faendir.discord4j.command.annotation.Description
import com.faendir.zachtronics.bot.discord.command.AbstractListCommand
import com.faendir.zachtronics.bot.leaderboards.Leaderboard
import com.faendir.zachtronics.bot.om.OmQualifier
import com.faendir.zachtronics.bot.om.model.OmCategory
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmRecord
import discord4j.core.event.domain.interaction.SlashCommandEvent
import discord4j.discordjson.json.ApplicationCommandOptionData
import org.springframework.stereotype.Component

@Component
@OmQualifier
class OmListCommand(override val leaderboards: List<Leaderboard<OmCategory, OmPuzzle, OmRecord>>) :
    AbstractListCommand<OmCategory, OmPuzzle, OmRecord>() {

    override fun buildData(): ApplicationCommandOptionData = ListCommandParser.buildData()

    override fun findPuzzleAndCategories(interaction: SlashCommandEvent): Pair<OmPuzzle, List<OmCategory>> {
        val show = ListCommandParser.parse(interaction)
        return Pair(show.puzzle, OmCategory.values().filter { it.supportsPuzzle(show.puzzle) })
    }
}

@ApplicationCommand(name = "list", description = "List records", subCommand = true)
data class ListCommand(
    @Description("Puzzle name. Can be shortened or abbreviated. E.g. `stab water`, `PMO`")
    @Converter(PuzzleConverter::class)
    val puzzle: OmPuzzle
)

