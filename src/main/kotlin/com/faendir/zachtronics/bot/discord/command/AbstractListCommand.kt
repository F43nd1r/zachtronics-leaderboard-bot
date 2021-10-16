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
import com.faendir.zachtronics.bot.utils.embedCategoryRecords
import com.faendir.zachtronics.bot.utils.interactionReplyReplaceSpecBuilder
import discord4j.core.spec.EmbedCreateFields
import discord4j.core.spec.EmbedCreateSpec
import discord4j.core.spec.InteractionReplyEditSpec

abstract class AbstractListCommand<T, C : Category, P : Puzzle, R : Record> : AbstractSubCommand<T>() {
    abstract val leaderboards: List<Leaderboard<C, P, R>>

    override fun handle(parameters: T): InteractionReplyEditSpec {
        val (puzzle, categories) = findPuzzleAndCategories(parameters)
        val records = leaderboards.map { it.getAll(puzzle, categories) }.reduce { acc, map -> acc + map }
        return interactionReplyReplaceSpecBuilder()
            .addEmbed(EmbedCreateSpec.builder()
                .title("*${puzzle.displayName}*")
                .embedCategoryRecords(records.entries)
                .apply {
                    val missing = categories.minus(records.keys)
                    if (missing.isNotEmpty()) {
                        addField(EmbedCreateFields.Field.of(missing.joinToString("/") { it.displayName }, "None", false))
                    }
                }
                .build()
            )
            .build()
    }

    /** @return pair of Puzzle and all the categories that support it */
    abstract fun findPuzzleAndCategories(parameters: T): Pair<P, List<C>>
}