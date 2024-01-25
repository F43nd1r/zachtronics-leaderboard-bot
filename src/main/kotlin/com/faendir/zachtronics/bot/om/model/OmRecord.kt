/*
 * Copyright (c) 2023
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

package com.faendir.zachtronics.bot.om.model

import com.faendir.zachtronics.bot.model.DisplayContext
import com.faendir.zachtronics.bot.model.Record
import com.faendir.zachtronics.bot.model.StringFormat
import com.faendir.zachtronics.bot.utils.Markdown
import com.faendir.zachtronics.bot.utils.PathSerializer
import com.faendir.zachtronics.bot.utils.orEmpty
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.nio.file.Path

@Serializable
data class OmRecord(
    override val puzzle: OmPuzzle,
    override val score: OmScore,
    override val displayLink: String?,
    override val dataLink: String,
    /** we relativize it to the repo root at (de)serialization */
    @Serializable(with = PathSerializer::class)
    override val dataPath: Path,
    val lastModified: Instant? = null,
    @Transient val displayLinkEmbed: String? = null,
    @Transient override val author: String? = null
) : Record<OmCategory> {

    override fun toDisplayString(context: DisplayContext<OmCategory>): String {
        return when (context.format) {
            StringFormat.DISCORD, StringFormat.REDDIT -> Markdown.fileLinkOrEmpty(dataLink) +
                    Markdown.linkOrText(score.toDisplayString(context), displayLink) + author.orEmpty(prefix = " by ")
            else -> score.toDisplayString(context) + author.orEmpty(prefix = " by ") + displayLink.orEmpty(prefix = " ")
        }
    }
}