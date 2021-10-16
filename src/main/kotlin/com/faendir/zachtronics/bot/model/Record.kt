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

import reactor.util.function.Tuple2
import java.io.InputStream


interface Record {
    val score: Score
    val link: String
    val author: String?
    fun toShowDisplayString(): String = "${score.toDisplayString()}${author?.let { " by $it" } ?: ""} $link"
    fun toEmbedDisplayString(): String = "[${score.toDisplayString()}](${link})"
    fun attachments(): List<Pair<String, InputStream>> = emptyList()
}