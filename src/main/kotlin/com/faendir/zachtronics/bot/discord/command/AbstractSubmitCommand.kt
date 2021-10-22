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

import com.faendir.zachtronics.bot.discord.Colors
import com.faendir.zachtronics.bot.leaderboards.Leaderboard
import com.faendir.zachtronics.bot.leaderboards.UpdateResult
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Record
import com.faendir.zachtronics.bot.utils.SafeEmbedMessageBuilder
import com.faendir.zachtronics.bot.utils.embedCategoryRecords
import com.faendir.zachtronics.bot.utils.findInstance
import com.faendir.zachtronics.bot.utils.ifNotEmpty
import discord4j.core.event.domain.interaction.InteractionCreateEvent
import reactor.core.publisher.Mono

abstract class AbstractSubmitCommand<T, P : Puzzle, R : Record> : AbstractSubCommand<T>(), SecuredSubCommand<T> {
    protected abstract val leaderboards: List<Leaderboard<*, P, R>>

    override fun handle(event: InteractionCreateEvent, parameters: T): Mono<Void> {
        val (puzzle, record) = parseSubmission(parameters)
        return submitToLeaderboards(puzzle, record).send(event)
    }

    fun submitToLeaderboards(puzzle: P, record: R): SafeEmbedMessageBuilder {
        val results = leaderboards.map { it.update(puzzle, record) }
        results.filterIsInstance<UpdateResult.Success>().ifNotEmpty { successes ->
            return SafeEmbedMessageBuilder()
                    .title("Success: *${puzzle.displayName}* ${successes.flatMap { it.oldRecords.keys }.joinToString { it.displayName }}")
                    .color(Colors.SUCCESS)
                    .description("`${record.score.toDisplayString()}` ${record.author?.let { " by $it" } ?: ""}\npreviously:")
                    .embedCategoryRecords(successes.flatMap { it.oldRecords.entries })
                    .link(record.link)
        }
        results.findInstance<UpdateResult.ParetoUpdate> {
            return SafeEmbedMessageBuilder()
                        .title("Pareto *${puzzle.displayName}*")
                        .color(Colors.SUCCESS)
                        .description("${record.score.toDisplayString()} was included in the pareto frontier.")
                        .link(record.link)
        }
        results.filterIsInstance<UpdateResult.BetterExists>().ifNotEmpty { betterExists ->
            return SafeEmbedMessageBuilder()
                        .title("No Scores beaten by *${puzzle.displayName}* `${record.score.toDisplayString()}`")
                        .color(Colors.UNCHANGED)
                        .description("Existing scores:")
                        .embedCategoryRecords(betterExists.flatMap {it.records.entries})
        }
        results.findInstance<UpdateResult.NotSupported> {
            throw IllegalArgumentException("No leaderboard supporting your submission found")
        }
        throw IllegalArgumentException("sorry, something went wrong")
    }

    private val allowedImageTypes = setOf("gif", "png", "jpg")

    private fun SafeEmbedMessageBuilder.link(link: String) = apply {
        if (allowedImageTypes.contains(link.substringAfterLast(".", ""))) {
            image(link)
        } else {
            addField("Link", "[$link]($link)")
        }
    }

    abstract fun parseSubmission(parameters: T): Pair<P, R>
}