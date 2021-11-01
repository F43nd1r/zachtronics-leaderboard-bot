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

package com.faendir.zachtronics.bot.om.leaderboards

import com.faendir.zachtronics.bot.git.GitRepository
import com.faendir.zachtronics.bot.om.imgur.ImgurService
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
import com.faendir.zachtronics.bot.om.model.OmCategory.SA
import com.faendir.zachtronics.bot.om.model.OmCategory.SC
import com.faendir.zachtronics.bot.om.model.OmCategory.SCP
import com.faendir.zachtronics.bot.om.model.OmCategory.SG
import com.faendir.zachtronics.bot.om.model.OmCategory.SGP
import com.faendir.zachtronics.bot.om.model.OmCategory.SI
import com.faendir.zachtronics.bot.om.model.OmGroup
import com.faendir.zachtronics.bot.om.model.OmModifier
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmRecord
import com.faendir.zachtronics.bot.om.model.OmScore
import com.faendir.zachtronics.bot.om.model.OmScorePart.AREA
import com.faendir.zachtronics.bot.om.model.OmScorePart.COST
import com.faendir.zachtronics.bot.om.model.OmScorePart.CYCLES
import com.faendir.zachtronics.bot.om.model.OmScorePart.INSTRUCTIONS
import com.faendir.zachtronics.bot.om.model.OmType
import com.faendir.zachtronics.bot.om.model.OmType.PRODUCTION
import com.faendir.zachtronics.bot.reddit.RedditService
import com.faendir.zachtronics.bot.reddit.Subreddit.OPUS_MAGNUM
import com.faendir.zachtronics.bot.utils.toMap
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.io.File
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Component
class OmRedditWikiLeaderboard(
    @Qualifier("omRedditLeaderboardRepository") gitRepository: GitRepository, private val reddit: RedditService,
    imgurService: ImgurService
) : AbstractOmJsonLeaderboard<MutableMap<OmPuzzle, PuzzleEntry>>(
    gitRepository,
    imgurService,
    mapOf("." to listOf(GC, GA, GX, GCP, GI, GXP, CG, CA, CX, CGP, CI, CXP, AG, AC, AX, IG, IC, IX, SG, SGP, SC, SCP, SA, SI)),
    serializer()
) {
    companion object {
        private const val wikiPage = "index"
        private const val datePrefix = "Table built on "
    }

    private val costCategories = listOf(GC, GA, GX, GCP, GI, GXP)
    private val cycleCategories = listOf(CG, CA, CX, CGP, CI, CXP)
    private val areaInstructionCategories = listOf(AG, AC, AX, IG, IC, IX)
    private val sumCategories = listOf(SG, SGP, SC, SCP, SA, SI)

    private fun filterRecords(records: Map<OmCategory, OmRecord>, filter: List<OmCategory>): MutableList<Map<OmCategory, OmRecord>> {
        return records.filter { filter.contains(it.key) }.entries.groupBy { it.value.link }.values.map { list -> list.sortedBy { it.key }.toMap() }
            .sortedBy { it.keys.first() }
            .toMutableList()
    }

    private fun Map<OmCategory, OmRecord>?.toMarkdown(): String {
        if (this == null || isEmpty()) return ""
        return "[${values.first().score.toShortDisplayString()}](${values.first().link})${if (any { it.key.name.contains("X") }) "*" else ""}"
    }

    override fun MutableMap<OmPuzzle, PuzzleEntry>.getRecord(puzzle: OmPuzzle, category: OmCategory) =
        get(puzzle)?.records?.get(category)?.apply { score.updateTransient(category) }

    override fun MutableMap<OmPuzzle, PuzzleEntry>.setRecord(puzzle: OmPuzzle, category: OmCategory, record: OmRecord) {
        set(puzzle, (get(puzzle) ?: PuzzleEntry()).apply { records[category] = record })
    }

    override fun GitRepository.AccessScope.updatePage(dir: File, categories: List<OmCategory>, records: MutableMap<OmPuzzle, PuzzleEntry>) {
        var table = ""
        for (group in OmGroup.values()) {
            table += "## ${group.displayName}\n\n"
            val puzzles = OmPuzzle.values().filter { it.group == group }
            val thirdCategory = puzzles.map {
                when (it.type) {
                    OmType.NORMAL, OmType.INFINITE -> "Area"
                    PRODUCTION -> "Instructions"
                }
            }.distinct().joinToString("/")
            table += "Name|Cost|Cycles|${thirdCategory}|Sum\n:-|:-|:-|:-|:-\n"
            for (puzzle in puzzles) {
                val entry = records[puzzle] ?: PuzzleEntry()
                table += "[**${puzzle.displayName}**](##Frontier: ${entry.pareto.joinToString(" ") { it.toShortDisplayString() }}##)"

                val costScores = filterRecords(entry.records, costCategories)
                val cycleScores = filterRecords(entry.records, cycleCategories)
                val areaInstructionScores = filterRecords(entry.records, areaInstructionCategories)
                val sumScores = filterRecords(entry.records, sumCategories)
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
        table += datePrefix + OffsetDateTime.now(ZoneOffset.UTC)
        val prefix = File(repo, "prefix.md").readText()
        val suffix = File(repo, "suffix.md").readText()
        val content = "$prefix\n$table\n$suffix".trim()
        if (content.lines().filter { !it.contains(datePrefix) } != reddit.getWikiPage(OPUS_MAGNUM, wikiPage).lines().filter { !it.contains(datePrefix) }) {
            reddit.updateWikiPage(OPUS_MAGNUM, wikiPage, content, "bot update")
        }
    }

    override fun paretoUpdate(puzzle: OmPuzzle, record: OmRecord, records: MutableMap<OmPuzzle, PuzzleEntry>): Boolean {
        val requiredParts = listOf(COST, CYCLES, if (puzzle.type == PRODUCTION) INSTRUCTIONS else AREA)
        if (record.score.parts.keys.containsAll(requiredParts) && record.score.modifier <= OmModifier.NORMAL) {
            val paretoScore = OmScore(requiredParts.map { it to record.score.parts[it]!! })
            val entry = (records[puzzle] ?: PuzzleEntry())
            if (!entry.pareto.contains(paretoScore) && entry.pareto.all { !it.isStrictlyBetter(paretoScore) }) {
                entry.pareto.removeIf { paretoScore.isStrictlyBetter(it) }
                entry.pareto.add(paretoScore)
                records[puzzle] = entry
                return true
            }
        }
        return false
    }

    override fun MutableMap<OmPuzzle, PuzzleEntry>.getPareto(puzzle: OmPuzzle): List<OmScore> = this[puzzle]?.pareto ?: emptyList()
}

@Serializable
data class PuzzleEntry(val records: MutableMap<OmCategory, OmRecord> = mutableMapOf(), val pareto: MutableList<OmScore> = mutableListOf())

fun OmScore.isStrictlyBetter(other: OmScore): Boolean {
    val compares = parts.map { it.value.compareTo(other.parts[it.key]!!) }
    return compares.none { it > 0 } && compares.any { it < 0 }
}