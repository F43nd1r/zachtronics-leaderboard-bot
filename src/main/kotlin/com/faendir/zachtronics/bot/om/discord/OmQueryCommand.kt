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
import com.faendir.zachtronics.bot.discord.command.Command
import com.faendir.zachtronics.bot.discord.command.option.CommandOptionBuilder
import com.faendir.zachtronics.bot.discord.command.security.NotSecured
import com.faendir.zachtronics.bot.discord.embed.MultiMessageSafeEmbedMessageBuilder
import com.faendir.zachtronics.bot.discord.embed.SafeMessageBuilder
import com.faendir.zachtronics.bot.om.OmQualifier
import com.faendir.zachtronics.bot.om.model.OmMetric
import com.faendir.zachtronics.bot.om.model.OmMetrics
import com.faendir.zachtronics.bot.om.omMeasurePointOptionBuilder
import com.faendir.zachtronics.bot.om.omPuzzleOptionBuilder
import com.faendir.zachtronics.bot.om.repository.OmSolutionRepository
import com.faendir.zachtronics.bot.utils.embedCategoryRecords
import com.faendir.zachtronics.bot.utils.runIf
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import org.springframework.stereotype.Component

@Component
@OmQualifier
class OmQueryCommand(private val repository: OmSolutionRepository) : Command.BasicLeaf() {
    override val name = "query"
    override val description = "Query the pareto frontier"
    override val secured = NotSecured

    val puzzleOption = omPuzzleOptionBuilder().required().build()
    val measurePointOption = omMeasurePointOptionBuilder().required().build()
    val queryOption = CommandOptionBuilder.string("query")
        .description("Query string in the form ABCD, spaces to separate ambiguous metrics, O is false unless specified")
        .required().build()

    override val options = listOf(puzzleOption, measurePointOption, queryOption)

    override fun handleEvent(event: ChatInputInteractionEvent): SafeMessageBuilder {
        val puzzle = puzzleOption.get(event)
        val measurePoint = measurePointOption.get(event)
        val query = queryOption.get(event)

        val possibleMetrics = (OmMetrics.BY_MEASURE_POINT[measurePoint]!! +
                OmMetrics.COMPUTED_BY_TYPE[puzzle.type]!!.filter { it.measurePoint == measurePoint })
            .sortedByDescending { it.displayName.length }

        val metrics = mutableListOf<OmMetric<*>>()
        for (p in query.split(" ")) {
            var part = p
            while (part.isNotEmpty()) {
                val foundMetric = possibleMetrics.firstOrNull {
                    part.startsWith(it.displayName, ignoreCase = true)
                } ?: throw IllegalArgumentException("Invalid metric(s): $part")

                metrics.add(foundMetric)
                part = part.substring(foundMetric.displayName.length)
            }
        }
        if (!metrics.contains(OmMetric.OVERLAP))
            metrics.addFirst(OmMetric.NOVERLAP)

        val description = metrics.joinToString("") { it.displayName.runIf(it.displayName.length > 1) { " $this " } }.trim()
        val records = repository.findBestByMetrics(puzzle, metrics)
        return MultiMessageSafeEmbedMessageBuilder()
            .title("*${puzzle.displayName}* $description")
            .url(puzzle.link)
            .color(Colors.READ)
            .embedCategoryRecords(records, puzzle.supportedCategories)
    }
}
