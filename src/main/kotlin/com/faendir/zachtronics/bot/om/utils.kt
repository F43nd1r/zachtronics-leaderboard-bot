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
import com.faendir.zachtronics.bot.discord.embed.MultiMessageSafeEmbedMessageBuilder
import com.faendir.zachtronics.bot.model.DisplayContext
import com.faendir.zachtronics.bot.model.StringFormat
import com.faendir.zachtronics.bot.om.discord.Channel
import com.faendir.zachtronics.bot.om.discord.SendToMainChannelButton
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
import com.faendir.zachtronics.bot.utils.LevelValue
import com.faendir.zachtronics.bot.utils.embedCategoryRecords
import com.faendir.zachtronics.bot.utils.smartFormat
import com.faendir.zachtronics.bot.utils.toMetricsTree
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.channel.MessageChannel
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingleOrNull
import okio.buffer
import okio.sink
import okio.source
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.math.ceil


private val logger = LoggerFactory.getLogger("OM Utils")

fun JNISolutionVerifier.getMetricSafe(metric: OmSimMetric) = try {
    getMetric(metric)
} catch (e: OmSimException) {
    logger.debug("Verifier threw exception for `${metric.id}`: ${e.message}")
    null
}

private fun JNISolutionVerifier.getAreaINF(outputsAtINF: Int?): LevelValue? {
    if (outputsAtINF == null)
        return null
    val a2 = getApproximateMetric(OmSimMetric.PER_REPETITION_SQUARED_AREA)
    if (a2 != 0.0) return LevelValue(2, a2 / (outputsAtINF * outputsAtINF))
    val a1 = getMetric(OmSimMetric.PER_REPETITION_AREA)
    if (a1 != 0) return LevelValue(1, a1.toDouble() / outputsAtINF)
    val a0 = getMetric(OmSimMetric.STEADY_STATE(OmSimMetric.AREA))
    return LevelValue(0, a0.toDouble())
}

fun JNISolutionVerifier.getScore(type: OmType): OmScore {
    // null or 0 PER_REPETITION_OUTPUTS means the solution doesn't output infinite products, hence cannot have a @INF point
    val outputsAtINF: Int? = getMetricSafe(OmSimMetric.PER_REPETITION_OUTPUTS)?.takeIf { it != 0 }

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

        rate = outputsAtINF?.run {
            val precision = 100.0
            ceil((precision * getMetric(OmSimMetric.PER_REPETITION_CYCLES).toDouble() / this)) / precision
        },
        areaINF = getAreaINF(outputsAtINF),
        heightINF = if (outputsAtINF == null || type == OmType.PRODUCTION) null
        else getMetricSafe(OmSimMetric.STEADY_STATE(OmSimMetric.HEIGHT))?.toInfinInt() ?: InfinInt.INFINITY,
        widthINF = if (outputsAtINF == null || type != OmType.NORMAL) null
        else getMetricSafe(OmSimMetric.STEADY_STATE(OmSimMetric.WIDTH_TIMES_TWO))?.toDouble()?.div(2)
            ?: Double.POSITIVE_INFINITY,
    )
}

fun createSubmission(gif: String?, author: String, inputBytes: ByteArray): OmSubmission {
    val solution = try {
        SolutionParser.parse(ByteArrayInputStream(inputBytes).source().buffer())
    } catch (e: Exception) {
        throw IllegalArgumentException("I could not parse your solution", e)
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
    val (puzzle, solutionBytes) = OmPuzzle.entries.find { it.id == solution.puzzle }?.let { it to inputBytes }
        ?: OmPuzzle.entries.find { it.altIds.contains(solution.puzzle) }?.let {
            solution.puzzle = it.id
            val out = ByteArrayOutputStream()
            SolutionParser.write(solution, out.sink().buffer(), writeSolved = true)
            it to out.toByteArray()
        }
        ?: throw IllegalArgumentException("I do not know the puzzle \"${solution.puzzle}\"")
    val puzzleFile = puzzle.file
    JNISolutionVerifier.open(puzzleFile.readBytes(), solutionBytes).use { verifier ->
        if (verifier.getMetric(OmSimMetric.MAXIMUM_TRACK_GAP_POW_2) > 1) {
            throw IllegalArgumentException("Quantum Tracks are banned.")
        }
        if (verifier.getMetric(OmSimMetric.MAXIMUM_ABSOLUTE_ARM_ROTATION) >= 4096) {
            throw IllegalArgumentException("Maximum arm rotations over 4096 are banned.")
        }
        return OmSubmission(
            puzzle,
            verifier.getScore(puzzle.type),
            author,
            gif,
            solutionBytes
        )
    }
}

fun OmRecord.withCategory(category: OmCategory) = CategoryRecord(this, setOf(category))

fun omPuzzleOptionBuilder() = enumOptionBuilder<OmPuzzle>("puzzle") { displayName }
    .description("Puzzle name. Can be shortened or abbreviated. E.g. `stab water`, `PMO`")

fun omScoreOptionBuilder() = CommandOptionBuilder.string("score")
    .description("full score of the submission, e.g. 100g/35c/9a/12i/2h/3w/T/L@V 100g/6r/9a/12i/2h/3w/T@INF")
    .convert { it?.replace("\\u200B".toRegex(), "")?.trim() }

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
                        .link(record.displayLinkEmbed ?: record.displayLink)
                        .action(SendToMainChannelButton.createAction()), Channel.PARETO
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
                        .link(record.displayLinkEmbed ?: record.displayLink)
                        .action(SendToMainChannelButton.createAction()), Channel.RECORD
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
                    .link(record.displayLinkEmbed ?: record.displayLink)
                    .action(SendToMainChannelButton.createAction()), Channel.UPDATE
            )
        }

        else -> emptyList()
    }.also {
        if (it.isNotEmpty()) {
            logger.info("Successfully notified discord of $submitResult with ${it.size} messages.")
        } else {
            logger.warn("No discord messages sent for $submitResult")
        }
    }
}

private suspend fun GatewayDiscordClient.sendDiscordMessage(message: MultiMessageSafeEmbedMessageBuilder, channel: Channel): List<Message> {
    val discordChannel = guilds.asFlow().mapNotNull {
        try {
            it.getChannelById(channel.id).awaitSingleOrNull()
        } catch (e: Exception) {
            logger.debug("Failed to get channel $channel in guild ${it.name}", e)
            null
        }
    }.singleOrNull()
    if (discordChannel == null) {
        logger.warn("Did not find channel $channel in any guild, unable to send messages")
        return emptyList()
    }
    if (discordChannel !is MessageChannel) {
        logger.warn("Channel $channel is not a message channel, unable to send messages")
        return emptyList()
    }
    return message.send(discordChannel).awaitSingleOrNull().orEmpty()

}
