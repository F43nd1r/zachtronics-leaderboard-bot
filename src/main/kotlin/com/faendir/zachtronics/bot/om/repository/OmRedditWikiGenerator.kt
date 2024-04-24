/*
 * Copyright (c) 2024
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

package com.faendir.zachtronics.bot.om.repository

import com.faendir.zachtronics.bot.git.GitRepository
import com.faendir.zachtronics.bot.model.DisplayContext
import com.faendir.zachtronics.bot.model.StringFormat
import com.faendir.zachtronics.bot.om.model.OmCategory
import com.faendir.zachtronics.bot.om.model.OmCategory.*
import com.faendir.zachtronics.bot.om.model.OmGroup
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmRecord
import com.faendir.zachtronics.bot.om.model.OmType
import com.faendir.zachtronics.bot.om.model.OmType.PRODUCTION
import com.faendir.zachtronics.bot.reddit.RedditService
import com.faendir.zachtronics.bot.reddit.Subreddit.OPUS_MAGNUM
import com.faendir.zachtronics.bot.utils.Markdown
import org.springframework.stereotype.Component
import java.io.File
import java.util.*

@Component
class OmRedditWikiGenerator(private val reddit: RedditService) {
    companion object {
        private const val wikiPage = "index"
    }

    private val costCategories = setOf(GC, GC_P, GA, GX, GX_P, GI_P)
    private val cycleCategories = setOf(CG, CG_P, CA, CX, CX_P, CI_P)
    private val areaInstructionCategories = setOf(AG, AC, AX, IG_P, IC_P, IX_P)
    private val sumCategories = setOf(SUM, SUM_P)
    private val categories = costCategories + cycleCategories + areaInstructionCategories + sumCategories

    private fun filterRecords(records: Collection<OmMemoryRecord>, filter: Set<OmCategory>): MutableList<Pair<OmRecord, List<OmCategory>>> {
        return records.map { mr -> Pair(mr.record, mr.categories.filter { filter.contains(it) }.sorted()) }
            .filter { it.second.isNotEmpty() }
            .sortedBy { (_, categories) -> categories.first() }
            .toMutableList()
    }

    private fun Pair<OmRecord, List<OmCategory>>?.toMarkdown(): String {
        if (this == null) return ""
        val score = first.score.toDisplayString(DisplayContext(StringFormat.REDDIT, second))
        return "${Markdown.linkOrText(score, first.displayLink)}${if (second.any { it.name.contains("X") }) "*" else ""}"
    }

    internal fun update(readAccess: GitRepository.ReadAccess, categories: List<OmCategory>, data: Map<OmPuzzle, SortedSet<OmMemoryRecord>>) {
        if (categories.any { this.categories.contains(it) }) {
            val prefix = File(readAccess.repo, "reddit/prefix.md").readText()
            val suffix = File(readAccess.repo, "reddit/suffix.md").readText()
            var table = ""
            for (group in OmGroup.entries) {
                table += "## ${group.displayName}\n\n"
                val puzzles = OmPuzzle.entries.filter { it.group == group }
                val thirdCategory = puzzles.map {
                    when (it.type) {
                        OmType.NORMAL, OmType.POLYMER -> "Area"
                        PRODUCTION -> "Instructions"
                    }
                }.distinct().joinToString("/")
                table += "Name|Cost|Cycles|${thirdCategory}|Sum\n:-|:-|:-|:-|:-\n"
                for (puzzle in puzzles) {
                    table += "[**${puzzle.displayName}**](${puzzle.link})"

                    val entry = data[puzzle] ?: emptySet()
                    val costScores = filterRecords(entry, costCategories)
                    val cycleScores = filterRecords(entry, cycleCategories)
                    val areaInstructionScores = filterRecords(entry, areaInstructionCategories)
                    val sumScores = filterRecords(entry, sumCategories)
                    while (costScores.isNotEmpty() || cycleScores.isNotEmpty() || areaInstructionScores.isNotEmpty() || sumScores.isNotEmpty()) {
                        table += "|${costScores.removeFirstOrNull().toMarkdown()}|${
                            cycleScores.removeFirstOrNull().toMarkdown()
                        }|${areaInstructionScores.removeFirstOrNull().toMarkdown()}|${
                            sumScores.removeFirstOrNull().toMarkdown()
                        }|\n|"
                    }
                    table += "\n"
                }
                table += "\n"
            }
            val content = "$prefix\n$table\n$suffix".trim()
            if (content.lines() != reddit.getWikiPage(OPUS_MAGNUM, wikiPage).lines()) {
                reddit.updateWikiPage(OPUS_MAGNUM, wikiPage, content, "bot update")
            }
        }
    }

}