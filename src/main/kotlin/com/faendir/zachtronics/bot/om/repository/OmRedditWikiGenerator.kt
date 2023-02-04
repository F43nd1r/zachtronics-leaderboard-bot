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

package com.faendir.zachtronics.bot.om.repository

import com.faendir.zachtronics.bot.git.GitRepository
import com.faendir.zachtronics.bot.model.DisplayContext
import com.faendir.zachtronics.bot.model.StringFormat
import com.faendir.zachtronics.bot.om.model.OmCategory
import com.faendir.zachtronics.bot.om.model.OmCategory.AC
import com.faendir.zachtronics.bot.om.model.OmCategory.AG
import com.faendir.zachtronics.bot.om.model.OmCategory.AX
import com.faendir.zachtronics.bot.om.model.OmCategory.CA
import com.faendir.zachtronics.bot.om.model.OmCategory.CG
import com.faendir.zachtronics.bot.om.model.OmCategory.CGP
import com.faendir.zachtronics.bot.om.model.OmCategory.CI
import com.faendir.zachtronics.bot.om.model.OmCategory.CX
import com.faendir.zachtronics.bot.om.model.OmCategory.CXP
import com.faendir.zachtronics.bot.om.model.OmCategory.GA
import com.faendir.zachtronics.bot.om.model.OmCategory.GC
import com.faendir.zachtronics.bot.om.model.OmCategory.GCP
import com.faendir.zachtronics.bot.om.model.OmCategory.GI
import com.faendir.zachtronics.bot.om.model.OmCategory.GX
import com.faendir.zachtronics.bot.om.model.OmCategory.GXP
import com.faendir.zachtronics.bot.om.model.OmCategory.IC
import com.faendir.zachtronics.bot.om.model.OmCategory.IG
import com.faendir.zachtronics.bot.om.model.OmCategory.IX
import com.faendir.zachtronics.bot.om.model.OmCategory.SUM_A
import com.faendir.zachtronics.bot.om.model.OmCategory.SUM_C
import com.faendir.zachtronics.bot.om.model.OmCategory.SUM_CP
import com.faendir.zachtronics.bot.om.model.OmCategory.SUM_G
import com.faendir.zachtronics.bot.om.model.OmCategory.SUM_GP
import com.faendir.zachtronics.bot.om.model.OmCategory.SUM_I
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
    private val categories = listOf(GC, GA, GX, GCP, GI, GXP, CG, CA, CX, CGP, CI, CXP, AG, AC, AX, IG, IC, IX, SUM_G, SUM_GP, SUM_C, SUM_CP, SUM_A, SUM_I)
    companion object {
        private const val wikiPage = "index"
    }

    private val costCategories = listOf(GC, GA, GX, GCP, GI, GXP)
    private val cycleCategories = listOf(CG, CA, CX, CGP, CI, CXP)
    private val areaInstructionCategories = listOf(AG, AC, AX, IG, IC, IX)
    private val sumCategories = listOf(SUM_G, SUM_GP, SUM_C, SUM_CP, SUM_A, SUM_I)

    private fun filterRecords(records: Collection<OmMemoryRecord>, filter: List<OmCategory>): MutableList<Pair<OmRecord, List<OmCategory>>> {
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
            for (group in OmGroup.values()) {
                table += "## ${group.displayName}\n\n"
                val puzzles = OmPuzzle.values().filter { it.group == group }
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