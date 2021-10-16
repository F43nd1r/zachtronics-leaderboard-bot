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
import com.faendir.zachtronics.bot.utils.interactionReplyReplaceSpecBuilder
import discord4j.core.spec.InteractionReplyEditSpec
import org.springframework.stereotype.Component
import java.util.*

@Component
abstract class AbstractSubmitArchiveCommand<T, P : Puzzle, R : Record, S : Solution> : AbstractSubCommand<T>(), SecuredSubCommand<T> {
    protected abstract val submitCommand: AbstractSubmitCommand<*, P, R>
    protected abstract val archiveCommand: AbstractArchiveCommand<*, S>

    override fun handle(parameters: T): InteractionReplyEditSpec {
        val (puzzle, record, solution) = parseToPRS(parameters)
        val submitOut = submitCommand.submitToLeaderboards(puzzle, record)
        val archiveOut = archiveCommand.archiveAll(Collections.singleton(solution))
        return interactionReplyReplaceSpecBuilder()
            .embeds(submitOut.embeds())
            .addEmbed(archiveOut)
            .build()
    }

    abstract fun parseToPRS(parameters: T): Triple<P, R, S>
}