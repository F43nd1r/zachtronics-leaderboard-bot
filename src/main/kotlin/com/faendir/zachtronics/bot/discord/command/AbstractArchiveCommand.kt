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
import com.faendir.zachtronics.bot.discord.Colors
import com.faendir.zachtronics.bot.model.Solution
import com.faendir.zachtronics.bot.utils.SafeEmbedMessageBuilder
import discord4j.core.event.domain.interaction.InteractionCreateEvent
import reactor.core.publisher.Mono

abstract class AbstractArchiveCommand<T, S : Solution<*>> : AbstractSubCommand<T>(), SecuredSubCommand<T> {
    protected abstract val archive: Archive<*, S>

    override fun handle(event: InteractionCreateEvent, parameters: T): Mono<Void> {
        val solutions = parseSolutions(parameters)
        return archiveAll(solutions).send(event)
    }

    fun archiveAll(solutions: Collection<S>): SafeEmbedMessageBuilder {
        val results = archive.archiveAll(solutions)

        val successes = results.count { it is ArchiveResult.Success }
        val title = if (successes != 0) "Success: $successes solution(s) archived" else "Failure: no solutions archived"

        val embed = SafeEmbedMessageBuilder().title(title).color(if(successes != 0) Colors.SUCCESS else Colors.UNCHANGED)
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
            embed.addField(name, value, true)
        }
        return embed
    }

    abstract fun parseSolutions(parameters: T): List<S>
}