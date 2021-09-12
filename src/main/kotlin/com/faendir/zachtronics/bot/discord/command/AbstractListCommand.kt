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

package com.faendir.zachtronics.bot.discord.command

import com.faendir.zachtronics.bot.leaderboards.Leaderboard
import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Record
import com.faendir.zachtronics.bot.utils.asMultipartRequest
import com.faendir.zachtronics.bot.utils.embedCategoryRecords
import discord4j.core.event.domain.interaction.SlashCommandEvent
import discord4j.discordjson.json.EmbedData
import discord4j.discordjson.json.EmbedFieldData
import discord4j.discordjson.json.WebhookExecuteRequest
import discord4j.rest.util.MultipartRequest

abstract class AbstractListCommand<C : Category, P : Puzzle, R : Record> : AbstractCommand() {
    abstract val leaderboards: List<Leaderboard<C, P, R>>

    override fun handle(event: SlashCommandEvent): MultipartRequest<WebhookExecuteRequest> {
        val (puzzle, categories) = findPuzzleAndCategories(event)
        val records = leaderboards.map { it.getAll(puzzle, categories) }.reduce { acc, map -> acc + map }
        return WebhookExecuteRequest.builder()
            .addEmbed(EmbedData.builder()
                .title("*${puzzle.displayName}*")
                .embedCategoryRecords(records.entries)
                .apply {
                    val missing = categories.minus(records.keys)
                    if (missing.isNotEmpty()) {
                        addField(
                            EmbedFieldData.builder()
                                .name(missing.joinToString("/") { it.displayName })
                                .value("None")
                                .inline(false)
                                .build()
                        )
                    }
                }
                .build()
            )
            .build()
            .asMultipartRequest()
    }

    /** @return pair of Puzzle and all the categories that support it */
    abstract fun findPuzzleAndCategories(interaction: SlashCommandEvent): Pair<P, List<C>>
}