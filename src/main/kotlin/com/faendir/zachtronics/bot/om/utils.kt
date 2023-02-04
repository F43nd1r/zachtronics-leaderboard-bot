/*
 * Copyright (c) 2023
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
import com.faendir.om.parser.solution.model.SolvedSolution
import com.faendir.om.parser.solution.model.part.Arm
import com.faendir.om.parser.solution.model.part.ArmType
import com.faendir.om.parser.solution.model.part.Glyph
import com.faendir.om.parser.solution.model.part.GlyphType
import com.faendir.om.parser.solution.model.part.IO
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
import com.faendir.zachtronics.bot.utils.InfinInt
import com.faendir.zachtronics.bot.utils.InfinInt.Companion.toInfinInt
import com.faendir.zachtronics.bot.utils.MultiMessageSafeEmbedMessageBuilder
import com.faendir.zachtronics.bot.utils.ceil
import com.faendir.zachtronics.bot.utils.embedCategoryRecords
import com.faendir.zachtronics.bot.utils.filterIsInstance
import com.faendir.zachtronics.bot.utils.smartFormat
import com.faendir.zachtronics.bot.utils.toMetricsTree
import discord4j.common.util.Snowflake
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.channel.MessageChannel
import kotlinx.coroutines.reactor.awaitSingleOrNull
import okio.buffer
import okio.sink
import okio.source
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream


private val logger = LoggerFactory.getLogger("OM Utils")

fun JNISolutionVerifier.getMetricSafe(metric: OmSimMetric) = try {
    getMetric(metric)
} catch (e: OmSimException) {
    logger.info("Verifier threw exception for `${metric.id}`: ${e.message}")
    null
}

fun JNISolutionVerifier.getScore(type: OmType): OmScore {
    // null or 0 THROUGHPUT_OUTPUTS means the solution doesn't output infinite products, hence cannot have a rate
    // infinity rate is reserved for sublinear solutions, which don't really exist and aren't supported by omsim
    val rate: Double? = getMetricSafe(OmSimMetric.THROUGHPUT_OUTPUTS)?.takeIf { it != 0 }?.let {
        (getMetric(OmSimMetric.THROUGHPUT_CYCLES).toDouble() / it).ceil(precision = 2)
    }

    return OmScore(
        cost = getMetric(OmSimMetric.COST).also {
            if (it != getMetric(OmSimMetric.PARSED_COST))
                throw IllegalArgumentException("Stored cost value does not match simulation. Run your solution to completion before submitting.")
        },
        instructions = getMetric(OmSimMetric.INSTRUCTIONS).also {
            if (it != getMetric(OmSimMetric.PARSED_INSTRUCTIONS))
                throw IllegalArgumentException("Stored instructions value does not match simulation. Run your solution to completion before submitting.")
        },

        overlap = getMetric(OmSimMetric.OVERLAP) != 0,
        trackless = getMetric(OmSimMetric.NUMBER_OF_TRACK_SEGMENTS) == 0,

        cycles = getMetric(OmSimMetric.CYCLES).also {
            if (it != getMetric(OmSimMetric.PARSED_CYCLES))
                throw IllegalArgumentException("Stored cycles value does not match simulation. Run your solution to completion before submitting.")
        },
        area = getMetric(OmSimMetric.AREA).also {
            if (it != getMetric(OmSimMetric.PARSED_AREA))
                throw IllegalArgumentException("Stored area value does not match simulation. Run your solution to completion before submitting.")
        },
        height = if (type != OmType.PRODUCTION) getMetric(OmSimMetric.HEIGHT) else null,
        width = if (type != OmType.PRODUCTION) getMetric(OmSimMetric.WIDTH_TIMES_TWO).toDouble() / 2 else null,

        rate = rate,
        areaINF = if (rate == null || type == OmType.POLYMER) null
        else getMetricSafe(OmSimMetric.STEADY_STATE(OmSimMetric.AREA))?.toInfinInt() ?: InfinInt.INFINITY,
        heightINF = if (rate == null || type == OmType.PRODUCTION) null
        else getMetricSafe(OmSimMetric.STEADY_STATE(OmSimMetric.HEIGHT))?.toInfinInt() ?: InfinInt.INFINITY,
        widthINF = if (rate == null || type != OmType.NORMAL) null
        else getMetricSafe(OmSimMetric.STEADY_STATE(OmSimMetric.WIDTH_TIMES_TWO))?.toDouble()?.div(2)
            ?: Double.POSITIVE_INFINITY,
    )
}

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
        val gifCycles = verifier.getMetricSafe(OmSimMetric.VISUAL_LOOP_START_CYCLE)?.let { it to verifier.getMetric(OmSimMetric.VISUAL_LOOP_END_CYCLE) }
            ?: (0 to if (puzzle.type == OmType.POLYMER && verifier.errorCycle > solution.cycles + 1) solution.cycles + 1 else solution.cycles)
        return OmSubmission(
            puzzle,
            verifier.getScore(puzzle.type),
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
    .description("full score of the submission, e.g. 100g/35c/9a/12i/2h/3w/T/L@V 100g/6r/9a/12i/2h/3w/T@INF")
    .convert { it?.replace("""[\s\u200B]""".toRegex(), "") }

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
                    MultiMessageSafeEmbedMessageBuilder()
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
                MultiMessageSafeEmbedMessageBuilder()
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

private suspend fun GatewayDiscordClient.sendDiscordMessage(message: MultiMessageSafeEmbedMessageBuilder, channel: Channel): List<Message> {
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