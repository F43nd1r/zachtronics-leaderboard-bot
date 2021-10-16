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
import com.faendir.discord4j.command.parse.ApplicationCommandParser
import com.faendir.zachtronics.bot.discord.LinkConverter
import com.faendir.zachtronics.bot.discord.command.AbstractSubmitCommand
import com.faendir.zachtronics.bot.discord.command.Secured
import com.faendir.zachtronics.bot.leaderboards.Leaderboard
import com.faendir.zachtronics.bot.om.OmQualifier
import com.faendir.zachtronics.bot.om.model.OmModifier
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmRecord
import com.faendir.zachtronics.bot.om.model.OmScore
import discord4j.discordjson.json.ApplicationCommandOptionData
import org.springframework.stereotype.Component

@Component
@OmQualifier
class OmSubmitCommand(override val leaderboards: List<Leaderboard<*, OmPuzzle, OmRecord>>) :
    AbstractSubmitCommand<Submit, OmPuzzle, OmRecord>(),
    Secured by OmSecured,
    ApplicationCommandParser<Submit, ApplicationCommandOptionData> by SubmitParser {
    override fun parseSubmission(parameters: Submit): Pair<OmPuzzle, OmRecord> {
        return Pair(
            parameters.puzzle,
            OmRecord(
                OmScore.parse(parameters.puzzle, parameters.score).apply { modifier = parameters.modifier ?: OmModifier.NORMAL },
                parameters.gif
            )
        )
    }
}

@ApplicationCommand(description = "Submit a solution", subCommand = true)
data class Submit(
    @Description("Puzzle name. Can be shortened or abbreviated. E.g. `stab water`, `PMO`")
    @Converter(PuzzleConverter::class)
    val puzzle: OmPuzzle,
    @Description("Puzzle score. E.g. `100/32/14/22`, `3.5w/32c/100g`")
    val score: String,
    @Converter(LinkConverter::class)
    @Description("Link to your solution gif/mp4, can be `m1` to scrape it from your last message")
    val gif: String,
    @Description("Metric Modifier")
    val modifier: OmModifier?
)