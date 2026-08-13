/*
 * Copyright (c) 2026
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
import com.faendir.zachtronics.bot.discord.embed.MultiMessageSafeEmbedMessageBuilder
import com.faendir.zachtronics.bot.mors.MorsService
import com.faendir.zachtronics.bot.om.OmQualifier
import com.faendir.zachtronics.bot.om.model.MeasurePoint
import com.faendir.zachtronics.bot.om.model.OmCategory
import com.faendir.zachtronics.bot.om.model.OmMetric
import com.faendir.zachtronics.bot.om.model.OmMetric.*
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmRecord
import com.faendir.zachtronics.bot.om.model.OmScore
import com.faendir.zachtronics.bot.om.model.OmScoreManifold
import com.faendir.zachtronics.bot.om.model.OmType
import com.faendir.zachtronics.bot.om.model.get
import com.faendir.zachtronics.bot.om.repository.OmSolutionRepository
import com.faendir.zachtronics.bot.om.sendDiscordMessage
import com.faendir.zachtronics.bot.om.validation.OmQL.QueryElement
import com.faendir.zachtronics.bot.repository.CategoryRecord
import com.faendir.zachtronics.bot.utils.embedRecords
import com.faendir.zachtronics.bot.utils.runIf
import com.faendir.zachtronics.bot.utils.url
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.entity.Message
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
@OmQualifier
class OmDailyPareto(
    private val discordClient: GatewayDiscordClient,
    private val repository: OmSolutionRepository,
) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(MorsService::class.java)
    }

    private data class RunInfo(
        val puzzle: OmPuzzle,
        val measurePoint: MeasurePoint,
        val frontier: List<QueryElement>,
        val post: Message,
    )

    private var previousRunInfo: RunInfo? = null

    @Scheduled(cron = "\${scheduling.pareto}", zone = "Europe/Berlin")
//    @Scheduled(cron = "0 * * * * *")
    fun dailyParetoTask() {
        synchronized(discordClient) {
            logger.info("Starting daily pareto bot posts")
            previous()
            current()
        }
    }

    private fun makePost(
        puzzle: OmPuzzle,
        measurePoint: MeasurePoint,
        frontier: List<QueryElement>,
        isCurrent: Boolean,
    ): List<Message> {
        val order = frontier.flatMap { it.metrics }.reduce(Comparator<OmScore>::then)
        val records = repository.executeOmQL(puzzle, measurePoint, frontier).sortedWith(compareBy(order) { it.record.score })
        return runBlocking {
            val title = if (isCurrent) "Today's frontier" else "Previous frontier"
            val description = if (isCurrent) "Current frontier:" else "See how it was back then: ${previousRunInfo!!.post.url}"
            discordClient.sendDiscordMessage(
                MultiMessageSafeEmbedMessageBuilder()
                    .title("$title: *${puzzle.displayName}* ${frontier.joinToString("", postfix = measurePoint.displayName) }")
                    .url(frontierLink(puzzle, frontier, records))
                    .color(Colors.SUCCESS)
                    .description(description)
                    .embedRecords(records, puzzle.supportedCategories)
                    .runIf(isCurrent) { action(RerollDailyParetoButton.createAction()) },
                Channel.PARETO
            )
        }
    }

    private fun frontierLink(
        puzzle: OmPuzzle,
        frontier: List<QueryElement>,
        records: List<CategoryRecord<OmRecord, OmCategory>>
    ): String {
        if (records.isEmpty())
            return puzzle.link
        return buildString {
            append("${puzzle.link}/visualizer?")
            for (element in frontier) {
                when (element) {
                    is QueryElement.Constraint -> {
                        for (modifier in listOf(OVERLAP, TRACKLESS) intersect element.metric.scoreParts) {
                            val name = this::class.simpleName!!.lowercase()
                            val value = records.first().record.score[modifier]
                            append("visualizerFilter-${puzzle.id}.modifiers.${name}=${value}&")
                        }
                    }
                    is QueryElement.Min -> {
                        if (element.metric is ScorePart<*>) {
                            val min = records.first().record.score[element.metric]
                            append("visualizerFilter-${puzzle.id}.range.${element.metric.description}.min=${min}&")
                            append("visualizerFilter-${puzzle.id}.range.${element.metric.description}.max=${min}&")
                        }
                    }
                    is QueryElement.Pareto -> {
                        append("visualizerConfig.mode=${element.metrics.size}D&")
                        for ((axis, metric) in listOf('x', 'y', 'z') zip element.metrics) {
                            append("visualizerConfig.${axis}.metric=${metric.description}&")
                        }
                    }
                }
            }
            append("visualizerFilter-${puzzle.id}.showOnlyFrontier=true")
        }
    }

    private fun previous() {
        if (previousRunInfo != null) {
            makePost(previousRunInfo!!.puzzle, previousRunInfo!!.measurePoint, previousRunInfo!!.frontier, isCurrent = false)
            previousRunInfo = null
            logger.warn("Posted previous frontier results")
        } else {
            logger.warn("No previous info found, bot was recently restarted")
        }
    }

    private fun current() {
        val puzzle = OmPuzzle.entries.random()
        val measurePoint = when (Random.nextInt(3)) {
            0, 1 -> MeasurePoint.VICTORY
            else -> MeasurePoint.INFINITY
        }
        val manifold = OmScoreManifold.entries
                .filter { puzzle.type in it.supportedTypes && it.measurePoint == measurePoint }.random()

        val frontierMetrics = manifold.scoreParts.filterIsInstanceTo(mutableListOf<Value<*>>()) // GCAI and so on

        val frontier = mutableListOf<QueryElement>()
        // modifiers
        frontier += QueryElement.Constraint(
            when (Random.nextInt(10)) {
                0 -> OmMetric.Constant("O||!O", true)
                in 1..2 -> NOVERLAP_TRACKLESS
                in 3..4 if (measurePoint == MeasurePoint.VICTORY) -> NOVERLAP_LOOPING
                3 if (puzzle.type != OmType.PRODUCTION) -> Custom(listOf(A2_INF), "A''==0") { it[A2_INF] == 0.0 }
                4 if (puzzle.type == OmType.NORMAL) -> Custom(listOf(A1_INF), "A'==0") { it[A1_INF] == 0.0 }
                else -> NOVERLAP
            })
        // minimized, or nearly so
        when (Random.nextInt(10)) {
            0, 1 -> {
                val m = frontierMetrics.random()
                frontier += QueryElement.Min(m)
                frontierMetrics.remove(m)
            }
            else -> {}
        }
        // frontier
        frontier += when (Random.nextInt(10)) {
            0 -> QueryElement.Pareto(frontierMetrics.randomN(3))
            else -> QueryElement.Pareto(frontierMetrics.randomN(2))
        }

        val messages = makePost(puzzle, measurePoint, frontier, isCurrent = true)
        logger.info("Posted new daily pareto frontier")
        previousRunInfo = RunInfo(puzzle, measurePoint, frontier, messages.first())
    }
}

private fun <T> List<T>.randomN(n: Int) = indices.shuffled().take(n).sorted().map(::get)
