/*
 * Copyright (c) 2022
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

package com.faendir.zachtronics.bot.om.rest

import com.faendir.zachtronics.bot.model.DisplayContext
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmScore
import com.faendir.zachtronics.bot.rest.UrlMapper
import org.springframework.stereotype.Component

@Component
class OmUrlMapper : UrlMapper {
    override val pathId = "om"

    override fun map(shortUrl: String): String? {
        val segments = shortUrl.split("/").filter { it.isNotBlank() }
        if (segments.size != 3) return null
        val (commit, puzzleId, record) = segments
        val puzzle = OmPuzzle.values().find { it.id.equals(puzzleId, ignoreCase = true) } ?: return null
        val fileName = if(record.endsWith(puzzle.name)) record else "${record}_${puzzle.name}"
        return "https://raw.githubusercontent.com/f43nd1r/om-leaderboard/${commit}/${puzzle.group.name}/${puzzle.name}/${fileName}.solution"
    }

    fun createShortUrl(commitId: String, puzzle: OmPuzzle, score: OmScore): String = buildUrl("$commitId/${puzzle.id}/${score.toDisplayString(DisplayContext.fileName())}")
}