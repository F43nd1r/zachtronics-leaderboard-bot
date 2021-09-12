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

import com.faendir.zachtronics.bot.archive.Archive
import com.faendir.zachtronics.bot.archive.ArchiveResult
import com.faendir.zachtronics.bot.model.Solution
import discord4j.core.event.domain.interaction.SlashCommandEvent
import discord4j.discordjson.json.EmbedData
import discord4j.discordjson.json.EmbedFieldData
import discord4j.discordjson.json.EmbedFooterData
import discord4j.discordjson.json.WebhookExecuteRequest
import discord4j.rest.util.MultipartRequest

abstract class AbstractArchiveCommand<S : Solution> : AbstractCommand(), SecuredCommand {
    protected abstract val archive: Archive<S>

    override fun handle(event: SlashCommandEvent): MultipartRequest<WebhookExecuteRequest> {
        val solutions = parseSolutions(event)
        val embed = archiveAll(solutions)
        return MultipartRequest.ofRequest(WebhookExecuteRequest.builder().addEmbed(embed).build())
    }

    fun archiveAll(solutions: Collection<S>): EmbedData {
        val results = archive.archiveAll(solutions)

        val successes = results.count { it !is ArchiveResult.Failure }
        val title = if (successes != 0) "Success: $successes solution(s) archived" else "Failure: no solutions archived"

        // Discord cries if an embed is bigger than 6k or we have more tha 25 embed fields:
        // https://discord.com/developers/docs/resources/channel#embed-limits
        var totalSize = title.length
        var totalFields = 0

        val embed = EmbedData.builder().title(title)
        for ((solution, result) in solutions.zip(results)) {
            val name = "*${solution.puzzle.displayName}*"
            val value = when (result) {
                is ArchiveResult.Success -> {
                    "`${solution.score.toDisplayString()}` has been archived.\n" + result.message
                }
                is ArchiveResult.AlreadyArchived -> {
                    "`${solution.score.toDisplayString()}` was already in the archive."
                }
                is ArchiveResult.Failure -> {
                    "`${solution.score.toDisplayString()}` did not qualify for archiving."
                }
            }

            totalFields++
            totalSize += name.length + value.length
            if (totalFields > 25 || totalSize > 5900) {
                embed.footer(EmbedFooterData.builder().text("${results.size - totalFields - 1} more results hidden").build())
                break
            }

            embed.addField(
                EmbedFieldData.builder()
                    .name(name)
                    .value(value)
                    .inline(true)
                    .build()
            )
        }
        return embed.build()
    }

    abstract fun parseSolutions(interaction: SlashCommandEvent): List<S>
}