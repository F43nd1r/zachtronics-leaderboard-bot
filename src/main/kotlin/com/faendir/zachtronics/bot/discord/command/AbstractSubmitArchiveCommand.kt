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

import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Record
import com.faendir.zachtronics.bot.model.Solution
import discord4j.core.event.domain.interaction.InteractionCreateEvent
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.*

@Component
abstract class AbstractSubmitArchiveCommand<T, P : Puzzle, R : Record, S : Solution<P>> : AbstractSubCommand<T>(),
    SecuredSubCommand<T> {
    protected abstract val submitCommand: AbstractSubmitCommand<*, P, R>
    protected abstract val archiveCommand: AbstractArchiveCommand<*, S>

    override fun handle(event: InteractionCreateEvent, parameters: T): Mono<Void> {
        val (record, solution) = parseToRS(parameters)
        val submitOut = submitCommand.submitToLeaderboards(solution.puzzle, record)
        val archiveOut = archiveCommand.archiveAll(Collections.singleton(solution))
        return (submitOut + archiveOut).send(event)
    }

    abstract fun parseToRS(parameters: T): Pair<R, S>
}