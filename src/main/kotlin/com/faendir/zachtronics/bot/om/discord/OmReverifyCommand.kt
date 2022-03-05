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
import com.faendir.zachtronics.bot.discord.command.AbstractSubCommand
import com.faendir.zachtronics.bot.discord.command.security.DiscordUser
import com.faendir.zachtronics.bot.discord.command.security.DiscordUserSecured
import com.faendir.zachtronics.bot.model.DisplayContext
import com.faendir.zachtronics.bot.om.JNISolutionVerifier
import com.faendir.zachtronics.bot.om.OmQualifier
import com.faendir.zachtronics.bot.om.getAllAvailableMetrics
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmRecord
import com.faendir.zachtronics.bot.om.model.OmScore
import com.faendir.zachtronics.bot.om.model.OmScorePart
import com.faendir.zachtronics.bot.om.model.OmType
import com.faendir.zachtronics.bot.om.repository.OmSolutionRepository
import com.faendir.zachtronics.bot.om.toScore
import com.faendir.zachtronics.bot.utils.SafeEmbedMessageBuilder
import com.faendir.zachtronics.bot.utils.SafeMessageBuilder
import com.faendir.zachtronics.bot.utils.orEmpty
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent
import discord4j.discordjson.json.ApplicationCommandOptionData
import org.springframework.stereotype.Component
import kotlin.io.path.readBytes

@Component
@OmQualifier
class OmReverifyCommand(private val repository: OmSolutionRepository) : AbstractSubCommand<Reverify>(),
    ApplicationCommandParser<Reverify, ApplicationCommandOptionData> by ReverifyParser {
    override val secured = DiscordUserSecured(DiscordUser.BOT_OWNERS)

    override fun handleEvent(event: DeferrableInteractionEvent, parameters: Reverify): SafeMessageBuilder {
        val puzzles = (parameters.puzzle?.let { listOf(it) } ?: OmPuzzle.values().toList()).filter {
            parameters.type == null || parameters.type == it.type
        }
        val parts = parameters.part?.let { listOf(it) } ?: OmScorePart.values().toList()
        val overrideExisting = parameters.overrideExisting == true
        val overrideRecords = mutableListOf<Pair<OmRecord, OmScore>>()
        val errors = mutableListOf<String>()
        for (puzzle in puzzles) {
            val puzzleFile = puzzle.file

            val records = repository.findCategoryHolders(puzzle, true)
                .filter { (record, _) -> parameters.score == null || record.score.toDisplayString().equals(parameters.score, ignoreCase = true) }
            for ((record, _) in records) {
                if (record.dataPath == null) {
                    errors.add("No solution file for ${record.toDisplayString(DisplayContext.discord())}")
                    continue
                }
                if (overrideExisting || parts.any { it.getValue(record.score) == null }) {
                    val computed = JNISolutionVerifier.open(puzzleFile.readBytes(), record.dataPath.readBytes()).use { it.getAllAvailableMetrics() }.toScore()
                    val newScore = if (overrideExisting) {
                        OmScore(
                            cost = computed.cost.takeIf { parts.contains(OmScorePart.COST) } ?: record.score.cost,
                            cycles = computed.cycles.takeIf { parts.contains(OmScorePart.CYCLES) } ?: record.score.cycles,
                            area = computed.area.takeIf { parts.contains(OmScorePart.AREA) } ?: record.score.area,
                            areaAtInf = computed.areaAtInf.takeIf { parts.contains(OmScorePart.AREA_AT_INF) } ?: record.score.areaAtInf,
                            instructions = computed.instructions.takeIf { parts.contains(OmScorePart.INSTRUCTIONS) } ?: record.score.instructions,
                            height = computed.height.takeIf { parts.contains(OmScorePart.HEIGHT) } ?: record.score.height,
                            width = computed.width.takeIf { parts.contains(OmScorePart.WIDTH) } ?: record.score.width,
                            rate = computed.rate.takeIf { parts.contains(OmScorePart.RATE) } ?: record.score.rate,
                            trackless = record.score.trackless,
                            overlap = record.score.overlap,
                        )
                    } else {
                        OmScore(
                            cost = record.score.cost ?: computed.cost.takeIf { parts.contains(OmScorePart.COST) },
                            cycles = record.score.cycles ?: computed.cycles.takeIf { parts.contains(OmScorePart.CYCLES) },
                            area = record.score.area ?: computed.area.takeIf { parts.contains(OmScorePart.AREA) },
                            areaAtInf = record.score.areaAtInf ?: computed.areaAtInf.takeIf { parts.contains(OmScorePart.AREA_AT_INF) },
                            instructions = record.score.instructions ?: computed.instructions.takeIf { parts.contains(OmScorePart.INSTRUCTIONS) },
                            height = record.score.height ?: computed.height.takeIf { parts.contains(OmScorePart.HEIGHT) },
                            width = record.score.width ?: computed.width.takeIf { parts.contains(OmScorePart.WIDTH) },
                            rate = record.score.rate ?: computed.rate.takeIf { parts.contains(OmScorePart.RATE) },
                            trackless = record.score.trackless,
                            overlap = record.score.overlap,
                        )
                    }
                    if (newScore != record.score) {
                        overrideRecords.add(record to newScore)
                    }
                }
            }
        }
        if (overrideRecords.isNotEmpty()) {
            repository.overrideScores(overrideRecords)
        }
        return SafeEmbedMessageBuilder()
            .title(
                "Reverify" +
                        parameters.type?.displayName.orEmpty(" ") +
                        parameters.puzzle?.displayName.orEmpty(" ") +
                        parameters.part?.displayName.orEmpty(" ") +
                        if (parameters.overrideExisting == true) " overriding existing values!" else ""
            )
            .description("**Modified Records:** ${overrideRecords.size}\n\n**Errors:**\n${errors.joinToString("\n")}")
    }
}

@ApplicationCommand(name = "reverify", description = "Recompute metrics based on filters", subCommand = true)
data class Reverify(
    val type: OmType?,
    @Converter(OmPuzzleConverter::class)
    @AutoComplete(OmPuzzleAutoCompletionProvider::class)
    val puzzle: OmPuzzle?,
    val part: OmScorePart?,
    @Converter(OmScoreCleaner::class)
    @Description("full score of the submission, e.g. 65g/80c/12a/4i/4h/4w/12r")
    val score: String?,
    val overrideExisting: Boolean?,
)