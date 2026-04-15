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
import com.faendir.zachtronics.bot.discord.DiscordActionCache
import com.faendir.zachtronics.bot.discord.command.Command
import com.faendir.zachtronics.bot.discord.command.option.CommandOptionBuilder
import com.faendir.zachtronics.bot.discord.command.security.NotSecured
import com.faendir.zachtronics.bot.discord.embed.PaginatedSafeEmbedMessageBuilder
import com.faendir.zachtronics.bot.discord.embed.SafeMessageBuilder
import com.faendir.zachtronics.bot.discord.embed.SafePlainMessageBuilder
import com.faendir.zachtronics.bot.model.DisplayContext
import com.faendir.zachtronics.bot.om.OmQualifier
import com.faendir.zachtronics.bot.om.model.OmMetrics
import com.faendir.zachtronics.bot.om.omMeasurePointOptionBuilder
import com.faendir.zachtronics.bot.om.omPuzzleOptionBuilder
import com.faendir.zachtronics.bot.om.repository.OmMemoryRecord
import com.faendir.zachtronics.bot.om.repository.OmSolutionRepository
import com.faendir.zachtronics.bot.om.validation.OmQL
import com.faendir.zachtronics.bot.utils.Markdown
import com.faendir.zachtronics.bot.utils.embedRecords
import com.faendir.zachtronics.bot.utils.smartFormat
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import org.springframework.stereotype.Component
import java.util.*

@Component
@OmQualifier
class OmQueryCommand(private val repository: OmSolutionRepository, val discordActionCache: DiscordActionCache) :
    Command.BasicLeaf() {
    override val name = "query"
    override val description = "Query the pareto frontier using OmQL"
    override val secured = NotSecured

    val puzzleOption = omPuzzleOptionBuilder().required().build()
    val measurePointOption = omMeasurePointOptionBuilder().required().build()
    val queryOption = CommandOptionBuilder.string("query")
        .description("Query string in the form A{B=3}[C*D]E(FG), O is false unless specified")
        .required().build()

    override val options = listOf(puzzleOption, measurePointOption, queryOption)

    override fun handleEvent(event: ChatInputInteractionEvent): SafeMessageBuilder {
        val puzzle = puzzleOption.get(event)
        val measurePoint = measurePointOption.get(event)
        val query = queryOption.get(event)

        val data: Collection<OmMemoryRecord> = repository.immutableData[puzzle]!!
        val possibleMetrics = (OmMetrics.BY_MEASURE_POINT[measurePoint]!! +
                OmMetrics.COMPUTED_BY_TYPE[puzzle.type]!!.filter { it.measurePoint == measurePoint })
            .sortedByDescending { it.displayName.length }

        val queryElements = OmQL.parseQuery(query.replace(" ", ""), possibleMetrics)
        val records = queryElements.fold(data) { acc, el -> el.filter(acc) }
        val name = Markdown.escape(queryElements.joinToString("")) + measurePoint.displayName

        return if (records.size == 1) {
            val record = records.first().record
            val categories = records.first().categories

            val lines = StringJoiner("\n")
            lines.add(Markdown.linkOrText("***${puzzle.displayName}***", puzzle.link, embed = false) + " **$name**")
            if (categories.isNotEmpty())
                lines.add("**${categories.smartFormat(puzzle.supportedCategories)}**")
            lines.add(record.toDisplayString(DisplayContext.discord()))
            SafePlainMessageBuilder()
                .content(lines.toString())
        }
        else PaginatedSafeEmbedMessageBuilder(discordActionCache)
            .title("*${puzzle.displayName}* $name")
            .url(puzzle.link)
            .color(Colors.READ)
            .embedRecords(records.map { it.toCategoryRecord() }, puzzle.supportedCategories)
    }
}
