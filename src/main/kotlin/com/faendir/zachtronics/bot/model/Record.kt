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

package com.faendir.zachtronics.bot.model

import com.faendir.zachtronics.bot.utils.orEmpty
import lombok.SneakyThrows
import java.nio.file.Path
import kotlin.io.path.inputStream

/** Read interface, used to store data from the repositories that needs to be displayed */
interface Record<C : Category> {
    val puzzle: Puzzle
    val score: Score<C>
    val author: String?

    /** Human-readable solution representation */
    val displayLink: String?

    /** Machine-readable solution representation */
    val dataLink: String?

    /** Machine-readable solution representation */
    val dataPath: Path?

    fun toDisplayString(context: DisplayContext<C>): String {
        return when (context.format) {
            StringFormat.DISCORD, StringFormat.REDDIT -> {
                val scoreAndAuthor = score.toDisplayString(context) + author.orEmpty(prefix = " ")
                dataLink.orEmpty(prefix = "[\uD83D\uDCC4](", suffix = ") ") + (displayLink?.let { "[$scoreAndAuthor]($it)" } ?: scoreAndAuthor)
            }
            else -> score.toDisplayString(context) + author.orEmpty(prefix = " by ") + displayLink.orEmpty(prefix = " ")
        }
    }

    @SneakyThrows
    fun attachments() = dataPath?.let {
        listOf(it.fileName.toString() to it.inputStream())
    } ?: emptyList()
}