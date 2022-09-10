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

package com.faendir.zachtronics.bot.om

import com.faendir.om.parser.solution.SolutionParser
import com.faendir.om.parser.solution.model.Position
import com.faendir.om.parser.solution.model.Solution
import com.faendir.om.parser.solution.model.SolvedSolution
import com.faendir.om.parser.solution.model.part.Arm
import com.faendir.om.parser.solution.model.part.ArmType
import com.faendir.om.parser.solution.model.part.Glyph
import com.faendir.om.parser.solution.model.part.GlyphType
import com.faendir.om.parser.solution.model.part.IO
import com.faendir.om.parser.solution.model.part.Track
import com.faendir.zachtronics.bot.discord.Colors
import com.faendir.zachtronics.bot.discord.command.option.CommandOptionBuilder
import com.faendir.zachtronics.bot.discord.command.option.enumOptionBuilder
import com.faendir.zachtronics.bot.model.DisplayContext
import com.faendir.zachtronics.bot.model.StringFormat
import com.faendir.zachtronics.bot.om.model.OmCategory
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmRecord
import com.faendir.zachtronics.bot.om.model.OmScore
import com.faendir.zachtronics.bot.om.model.OmSubmission
import com.faendir.zachtronics.bot.om.model.OmType
import com.faendir.zachtronics.bot.repository.CategoryRecord
import com.faendir.zachtronics.bot.repository.SubmitResult
import com.faendir.zachtronics.bot.rest.dto.SubmitResultType
import com.faendir.zachtronics.bot.utils.SafeEmbedMessageBuilder
import com.faendir.zachtronics.bot.utils.ceil
import com.faendir.zachtronics.bot.utils.embedCategoryRecords
import com.faendir.zachtronics.bot.utils.filterIsInstance
import com.faendir.zachtronics.bot.utils.fuzzyMatch
import com.faendir.zachtronics.bot.utils.orEmpty
import com.faendir.zachtronics.bot.utils.smartFormat
import com.faendir.zachtronics.bot.utils.toMetricsTree
import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.channel.MessageChannel
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import okio.buffer
import okio.sink
import okio.source
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.math.max
import kotlin.math.min


private val logger = LoggerFactory.getLogger("OM Utils")

fun Solution.isTrackless(): Boolean = parts.none { it is Track }

fun JNISolutionVerifier.getMetricSafe(metric: OmSimMetric) = try {
    getMetric(metric)
} catch (e: Exception) {
    logger.info("Verifier threw exception for $metric: ${e.message}")
    null
}

fun SolvedSolution.getScore(verifier: JNISolutionVerifier) =
    OmScore(
        cost = verifier.getMetricSafe(OmSimMetric.COST).also {
            if (it != cost) throw IllegalArgumentException("Stored cost value does not match simulation. Run your solution to completion before submitting.")
        },
        cycles = verifier.getMetricSafe(OmSimMetric.CYCLES).also {
            if (it != cycles) throw IllegalArgumentException("Stored cycles value does not match simulation. Run your solution to completion before submitting.")
        },
        area = verifier.getMetricSafe(OmSimMetric.AREA_APPROXIMATE).also {
            if (it == null || min(it, area).toDouble() / max(it, area) < 0.95) {
                throw IllegalArgumentException("Stored area value does not match simulation. Run your solution to completion before submitting.")
            }
        },
        instructions = verifier.getMetricSafe(OmSimMetric.INSTRUCTIONS).also {
            if (it != instructions) throw IllegalArgumentException("Stored instructions value does not match simulation. Run your solution to completion before submitting.")
        },
        height = verifier.getMetricSafe(OmSimMetric.HEIGHT),
        width = verifier.getMetricSafe(OmSimMetric.WIDTH_TIMES_TWO)?.let { it.toDouble() / 2 },
        rate = verifier.getMetricSafe(OmSimMetric.THROUGHPUT_CYCLES)?.let {
            (it.toDouble() / (verifier.getMetricSafe(OmSimMetric.THROUGHPUT_OUTPUTS) ?: 0)).ceil(precision = 2)
        },
        trackless = isTrackless(),
        overlap = verifier.getMetricSafe(OmSimMetric.OVERLAP) != 0
    )

fun createSubmission(gif: String?, gifData: ByteArray?, author: String, inputBytes: ByteArray): OmSubmission {
    val solution = try {
        SolutionParser.parse(ByteArrayInputStream(inputBytes).source().buffer())
    } catch (e: Exception) {
        throw IllegalArgumentException("I could not parse your solution")
    }
    if (solution !is SolvedSolution) {
        throw IllegalArgumentException("only solved solutions are accepted")
    }
    if (solution.parts.count { it is Arm && it.type == ArmType.VAN_BERLOS_WHEEL } > 1) {
        throw IllegalArgumentException("Multiple Van Berlo's Wheels are banned.")
    }
    if (solution.parts.count { it is Glyph && it.type == GlyphType.DISPOSAL } > 1) {
        throw IllegalArgumentException("Multiple Disposal glyphs are banned.")
    }
    if (solution.parts.filterIsInstance<IO>().groupBy { it.type to it.index }.values.any { it.size > 1 }) {
        throw IllegalArgumentException("Duplicated Inputs or Outputs are banned.")
    }
    val (puzzle, solutionBytes) = OmPuzzle.values().find { it.id == solution.puzzle }?.let { it to inputBytes }
        ?: OmPuzzle.values().find { it.altIds.contains(solution.puzzle) }?.let {
            solution.puzzle = it.id
            val out = ByteArrayOutputStream()
            SolutionParser.write(solution, out.sink().buffer(), writeSolved = true)
            it to out.toByteArray()
        }
        ?: throw IllegalArgumentException("I do not know the puzzle \"${solution.puzzle}\"")
    val puzzleFile = puzzle.file
    JNISolutionVerifier.open(puzzleFile.readBytes(), solutionBytes).use { verifier ->
        if ((verifier.getMetricSafe(OmSimMetric.MAXIMUM_TRACK_GAP_POW_2) ?: 0) > 1) {
            throw IllegalArgumentException("Quantum Tracks are banned.")
        }
        if ((verifier.getMetricSafe(OmSimMetric.MAXIMUM_ABSOLUTE_ARM_ROTATION) ?: 0) >= 4096) {
            throw IllegalArgumentException("Maximum arm rotations over 4096 are banned.")
        }
        val gifCycles = verifier.getMetricSafe(OmSimMetric.VISUAL_LOOP_START_CYCLE)?.let { it to verifier.getMetricSafe(OmSimMetric.VISUAL_LOOP_END_CYCLE)!! }
            ?: (0 to if (puzzle.type == OmType.INFINITE && verifier.errorCycle > solution.cycles + 1) solution.cycles + 1 else solution.cycles)
        return OmSubmission(
            puzzle,
            solution.getScore(verifier),
            author,
            gif,
            gifData,
            gifCycles,
            solutionBytes
        )
    }
}

fun OmRecord.withCategory(category: OmCategory) = CategoryRecord(this, setOf(category))

fun omPuzzleOptionBuilder() = enumOptionBuilder<OmPuzzle>("puzzle") { displayName }
    .description("Puzzle name. Can be shortened or abbreviated. E.g. `stab water`, `PMO`")

fun omScoreOptionBuilder() = CommandOptionBuilder.string("score")
    .description("full score of the submission, e.g. 65g/80c/12a/4i/4h/4w/12r")
    .convert { it?.replace("[\\s\u200B]".toRegex(), "") }

fun omSolutionOptionBuilder() = CommandOptionBuilder.attachment("solution").description("Your solution file")

suspend fun GatewayDiscordClient.notifyOf(submitResult: SubmitResult<OmRecord, OmCategory>): List<Message> {
    return when (submitResult) {
        is SubmitResult.Success -> {
            val record = submitResult.record!!
            val beatenCategories: List<OmCategory> = submitResult.beatenRecords.flatMap { it.categories }
            if (beatenCategories.isEmpty()) {
                sendDiscordMessage(
                    SafeEmbedMessageBuilder()
                        .title("New Submission: *${record.puzzle.displayName}* Pareto")
                        .color(Colors.SUCCESS)
                        .description(
                            record.toDisplayString(DisplayContext(StringFormat.DISCORD, beatenCategories))
                                    + " was included in the pareto frontier."
                                    + (if (submitResult.beatenRecords.isNotEmpty()) "\nPreviously:" else "")
                        )
                        .embedCategoryRecords(submitResult.beatenRecords, record.puzzle.supportedCategories)
                        .link(record.displayLink), Channel.PARETO
                )
            } else {
                sendDiscordMessage(
                    SafeEmbedMessageBuilder()
                        .title("New Submission: *${record.puzzle.displayName}* ${beatenCategories.smartFormat(record.puzzle.supportedCategories.toMetricsTree())}")
                        .color(Colors.SUCCESS)
                        .description(
                            record.toDisplayString(DisplayContext(StringFormat.DISCORD, beatenCategories))
                                    + (if (submitResult.beatenRecords.isNotEmpty()) "\nPreviously:" else "")
                        )
                        .embedCategoryRecords(submitResult.beatenRecords, record.puzzle.supportedCategories)
                        .link(record.displayLink), Channel.RECORD
                )
            }
        }

        is SubmitResult.Updated -> {
            val record = submitResult.record!!
            val puzzle = record.puzzle
            sendDiscordMessage(
                SafeEmbedMessageBuilder()
                    .title(
                        "Updated: *${puzzle.displayName}* ${
                            submitResult.oldRecord.categories.takeIf { it.isNotEmpty() }?.smartFormat(puzzle.supportedCategories.toMetricsTree()) ?: "Pareto"
                        }"
                    )
                    .color(Colors.SUCCESS)
                    .description(
                        "${record.toDisplayString(DisplayContext(StringFormat.DISCORD, submitResult.oldRecord.categories))} was updated.\nPreviously:"
                    )
                    .embedCategoryRecords(listOf(submitResult.oldRecord), puzzle.supportedCategories)
                    .link(record.displayLink), Channel.UPDATE
            )
        }

        else -> emptyList()
    }
}

private suspend fun GatewayDiscordClient.sendDiscordMessage(message: SafeEmbedMessageBuilder, channel: Channel): List<Message> {
    return guilds
        .flatMap { it.getChannelById(channel.id).onErrorResume { Mono.empty() } }
        .filterIsInstance<MessageChannel>()
        .flatMap { message.send(it) }
        .collectList()
        .awaitSingleOrNull()
        ?.flatten()
        ?: emptyList()
}


enum class Channel(idLong: Long) {
    RECORD(370367639073062922),
    PARETO(909638277756243978),
    UPDATE(1006543549346611251),
    ;

    val id = Snowflake.of(idLong)
}