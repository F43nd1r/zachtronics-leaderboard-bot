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

package com.faendir.zachtronics.bot.om.model

import com.faendir.zachtronics.bot.model.Record
import com.faendir.zachtronics.bot.utils.PathSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.io.InputStream
import java.nio.file.Path

@Serializable
data class OmRecord(
    override val puzzle: OmPuzzle,
    override val score: OmScore,
    override val displayLink: String?,
    override val dataLink: String? = null,
    @Serializable(with = PathSerializer::class)
    override val dataPath: Path?,
) : Record<OmCategory> {
    @Transient
    override val author: String? = null

    override fun attachments(): List<Pair<String, InputStream>> = emptyList()
}