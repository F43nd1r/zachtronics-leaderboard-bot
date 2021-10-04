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
import com.faendir.zachtronics.bot.leaderboards.UpdateResult
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Record
import com.faendir.zachtronics.bot.utils.asMultipartRequest
import com.faendir.zachtronics.bot.utils.embedCategoryRecords
import com.faendir.zachtronics.bot.utils.findInstance
import com.faendir.zachtronics.bot.utils.ifNotEmpty
import discord4j.core.event.domain.interaction.SlashCommandEvent
import discord4j.discordjson.json.*
import discord4j.rest.util.MultipartRequest

abstract class AbstractSubmitCommand<P : Puzzle, R : Record> : AbstractCommand(), SecuredCommand {
    protected abstract val leaderboards: List<Leaderboard<*, P, R>>

    override fun handle(event: SlashCommandEvent): MultipartRequest<WebhookExecuteRequest> {
        val (puzzle, record) = parseSubmission(event)
        return submitToLeaderboards(puzzle, record).asMultipartRequest()
    }

    fun submitToLeaderboards(puzzle: P, record: R): WebhookExecuteRequest {
        val results = leaderboards.map { it.update(puzzle, record) }
        results.filterIsInstance<UpdateResult.Success>().ifNotEmpty { successes ->
            return WebhookExecuteRequest.builder().addEmbed(
                EmbedData.builder()
                    .title("Success: *${puzzle.displayName}* ${successes.flatMap { it.oldRecords.keys }.joinToString { it.displayName }}")
                    .description("`${record.score.toDisplayString()}` ${record.author?.let { " by $it" } ?: ""}\npreviously:")
                    .embedCategoryRecords(successes.flatMap { it.oldRecords.entries })
                    .link(record.link)
                    .build()
            )
                .build()
        }
        results.findInstance<UpdateResult.ParetoUpdate> {
            return WebhookExecuteRequest.builder()
                .addEmbed(
                    EmbedData.builder()
                        .title("Pareto *${puzzle.displayName}*")
                        .description("${record.score.toDisplayString()} was included in the pareto frontier.")
                        .link(record.link)
                        .build()
                )
                .build()
        }
        results.filterIsInstance<UpdateResult.BetterExists>().ifNotEmpty { betterExists ->
            return WebhookExecuteRequest.builder()
                .addEmbed(
                    EmbedData.builder()
                        .title("No Scores beaten by *${puzzle.displayName}* `${record.score.toDisplayString()}`")
                        .description("Existing scores:")
                        .embedCategoryRecords(betterExists.flatMap {it.records.entries})
                        .build()
                )
                .build()
        }
        results.findInstance<UpdateResult.NotSupported> {
            throw IllegalArgumentException("No leaderboard supporting your submission found")
        }
        throw IllegalArgumentException("sorry, something went wrong")
    }

    private val allowedImageTypes = setOf("gif", "png", "jpg")
    private val allowedVideoHosts = setOf("https://www.youtube.com/", "https://youtu.be/")

    private fun ImmutableEmbedData.Builder.link(link: String) = apply {
        if (allowedImageTypes.contains(link.substringAfterLast(".", ""))) {
            image(EmbedImageData.builder().url(link).build())
        } else if (allowedVideoHosts.any { link.startsWith(it) }) {
            video(EmbedVideoData.builder().url(link).build())
        } else {
            addField(EmbedFieldData.builder().name("Link").value("[$link]($link)").inline(false).build())
        }
    }

    abstract fun parseSubmission(interaction: SlashCommandEvent): Pair<P, R>
}