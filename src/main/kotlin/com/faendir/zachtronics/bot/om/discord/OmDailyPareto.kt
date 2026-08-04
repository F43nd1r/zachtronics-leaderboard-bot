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
import com.faendir.zachtronics.bot.om.model.OmMetric
import com.faendir.zachtronics.bot.om.model.OmMetric.*
import com.faendir.zachtronics.bot.om.model.OmMetrics
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmScoreManifold
import com.faendir.zachtronics.bot.om.repository.OmSolutionRepository
import com.faendir.zachtronics.bot.om.sendDiscordMessage
import com.faendir.zachtronics.bot.om.validation.OmQL.QueryElement
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
        val records = repository.executeOmQL(puzzle, measurePoint, frontier)
        return runBlocking {
            val title = if (isCurrent) "Today's frontier" else "Previous frontier"
            val description = if (isCurrent) "Current frontier:" else "See how it was back then: ${previousRunInfo!!.post.url}"
            discordClient.sendDiscordMessage(
                MultiMessageSafeEmbedMessageBuilder()
                    .title("$title: *${puzzle.displayName}* ${frontier.joinToString("", postfix = measurePoint.displayName) }")
                    .url(puzzle.link)
                    .color(Colors.SUCCESS)
                    .description(description)
                    .embedRecords(records, puzzle.supportedCategories)
                    .runIf(isCurrent) { action(RerollDailyParetoButton.createAction()) },
                Channel.PARETO
            )
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

        val minimizedMetrics = OmMetrics.userFacing(puzzle.type)
            .filterTo(mutableListOf()) { it !is Modifier && it.measurePoint == measurePoint }
        val frontierMetrics = manifold.scoreParts.filterIsInstanceTo(mutableListOf<Value<*>>()) // GCAI and so on

        val frontier = mutableListOf<QueryElement>()
        // modifiers
        frontier.add(QueryElement.Constraint(
            when (Random.nextInt(10)) {
                0 -> OmMetric.Constant("O||!O", true)
                in 1..2 -> NOVERLAP_TRACKLESS
                in 3..4 if (measurePoint == MeasurePoint.VICTORY) -> NOVERLAP_LOOPING
                else -> NOVERLAP
            }))
        // minimized
        fun addMin(m: OmMetric<*>) {
            frontier.add(QueryElement.Min(m))
            minimizedMetrics.remove(m)
            frontierMetrics.remove(m)
        }
        when (Random.nextInt(10)) {
            0 -> {
                addMin(minimizedMetrics.random())
                addMin(minimizedMetrics.random())
            }
            1, 2 -> addMin(minimizedMetrics.random())
            else -> {}
        }
        // frontier
        when (Random.nextInt(10)) {
            0 -> frontier.add(QueryElement.Pareto(frontierMetrics.shuffled().take(3)))
            else -> frontier.add(QueryElement.Pareto(frontierMetrics.shuffled().take(2)))
        }

        val messages = makePost(puzzle, measurePoint, frontier, isCurrent = true)
        logger.info("Posted new daily pareto frontier")
        previousRunInfo = RunInfo(puzzle, measurePoint, frontier, messages.first())
    }
}
