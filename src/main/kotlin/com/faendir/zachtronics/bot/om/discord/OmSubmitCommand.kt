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
import com.faendir.om.parser.solution.model.Position
import com.faendir.zachtronics.bot.discord.LinkConverter
import com.faendir.zachtronics.bot.discord.command.AbstractSubmitCommand
import com.faendir.zachtronics.bot.discord.command.Secured
import com.faendir.zachtronics.bot.discord.command.TrustedLeaderboardPosterRoleSecured
import com.faendir.zachtronics.bot.om.OmQualifier
import com.faendir.zachtronics.bot.om.createSubmission
import com.faendir.zachtronics.bot.om.model.OmCategory
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmRecord
import com.faendir.zachtronics.bot.om.model.OmSubmission
import com.faendir.zachtronics.bot.om.repository.OmSolutionRepository
import com.faendir.zachtronics.bot.utils.user
import com.roxstudio.utils.CUrl
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent
import discord4j.discordjson.json.ApplicationCommandOptionData
import org.springframework.stereotype.Component
import java.util.*

@Component
@OmQualifier
class OmSubmitCommand(override val repository: OmSolutionRepository) : AbstractSubmitCommand<SubmitParams, OmCategory, OmPuzzle, OmSubmission, OmRecord>(),
    ApplicationCommandParser<SubmitParams, ApplicationCommandOptionData> by SubmitParamsParser {
    override val secured: Secured = TrustedLeaderboardPosterRoleSecured

    override fun parseSubmission(event: DeferrableInteractionEvent, parameters: SubmitParams): OmSubmission {
        val bytes = try {
            CUrl(parameters.solution).exec()
        } catch (e: Exception) {
            throw IllegalArgumentException("Could not load your solution file")
        }
        return createSubmission(parameters.gif, event.user().username, bytes)
    }
}

@ApplicationCommand(name = "submit", description = "Submit a solution", subCommand = true)
data class SubmitParams(
    @Converter(LinkConverter::class)
    @Description("Link to your solution file, can be `m1` to scrape it from your last message")
    val solution: String,
    @Converter(LinkConverter::class)
    @Description("Link to your solution gif/mp4, can be `m1` to scrape it from your last message")
    val gif: String,
)

infix operator fun Position.plus(other: Position) = Position(this.x + other.x, this.y + other.y)

data class CubicPosition(val x: Int, val y: Int, val z: Int) {

    fun rotate(times: Int): CubicPosition {
        val t = Math.floorMod(times, 6)
        val coords = mutableListOf(x, y, z)
        if (t % 2 != 0) {
            coords.replaceAll { -it }
        }
        Collections.rotate(coords, t % 3)
        return CubicPosition(coords[0], coords[1], coords[2])
    }

    fun toAxial(): Position = Position(x, z)
}

fun Position.toCubic(): CubicPosition = CubicPosition(x, -x - y, y)

fun Position.rotate(times: Int) = this.toCubic().rotate(times).toAxial()