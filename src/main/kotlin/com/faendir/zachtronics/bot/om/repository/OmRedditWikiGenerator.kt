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

package com.faendir.zachtronics.bot.om.repository

import com.faendir.zachtronics.bot.git.GitRepository
import com.faendir.zachtronics.bot.model.DisplayContext
import com.faendir.zachtronics.bot.model.Puzzle
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
import com.faendir.zachtronics.bot.om.model.OmCategory.SA
import com.faendir.zachtronics.bot.om.model.OmCategory.SC
import com.faendir.zachtronics.bot.om.model.OmCategory.SCP
import com.faendir.zachtronics.bot.om.model.OmCategory.SG
import com.faendir.zachtronics.bot.om.model.OmCategory.SGP
import com.faendir.zachtronics.bot.om.model.OmCategory.SI
import com.faendir.zachtronics.bot.om.model.OmGroup
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmRecord
import com.faendir.zachtronics.bot.om.model.OmType
import com.faendir.zachtronics.bot.om.model.OmType.PRODUCTION
import com.faendir.zachtronics.bot.reddit.RedditService
import com.faendir.zachtronics.bot.reddit.Subreddit.OPUS_MAGNUM
import org.springframework.stereotype.Component
import java.io.File
import java.time.OffsetDateTime
import java.time.ZoneOffset

const val prefix = """# Explanation of Table

## Displayed Scores

Each puzzle has up to 12 entries in the table, 4 primary metrics and 3 tiebreakers each.

The primary metrics are cost, cycles, area (free space puzzles) / instructions (production puzzles), and the sum of all 3.  Scores will only be displayed in the table if they beat or match the record in a primary metric.

Tiebreakers for minimum cost are lowest cycles, lowest area, and lowest product cycles*area.  Tiebreakers for cycles and area are the same with metrics permuted, and prodution puzzles replace area with instructions.  An asterisk denotes the product tiebreaker.

Tiebreakers for the sum are cost, cycles, and area individually.

If a solution holds multiple tiebreakers, it will only be displayed once per primary metric.

On 24 March 2018, /u/12345ieee helped tremendously with revamping the update bot. It now includes gifs when they are linked in the comment, and the puzzle name is a fake link containing the current pareto frontier (mouse over to see frontier scores).

Winter 2019 featured [the first Opus Magnum tournament](https://www.reddit.com/r/opus_magnum/comments/abpxj8/opus_magnum_tourney/), hosted by /u/RandomPanda0.  These puzzles exist [on the steam workshop here](https://steamcommunity.com/sharedfiles/filedetails/?id=1700218647).  Scores for these puzzles are now on the leaderboard as well, for all 10 accepted categories.  Note the Week 6 puzzle requires a template file as a starting point, which can be found [here](https://www.dropbox.com/s/n5iuf7rroapr1za/miraculous-autosalt-week6-TEMPLATE.solution?dl=0).

## Looping vs nonlooping, waste chain vs clean, etc.

A solution need only bring up the completion screen to count.
If there are many requests, I can open separate entries for some puzzles.

## How to add your solution

Post your score as a comment to [this post](https://www.reddit.com/r/opus_magnum/comments/i411ul/official_record_submission_thread/).

Comments should have the puzzle name, any scores associated with the puzzle, and any gifs associated with the scores all on the same line.
Gifs are expected to be boxed links, use the format

    name : [scoreA](linkA), [scoreB](linkB),...
    name2: [score2A](link2A)
    ...etc.

if uncertain.  Links will be added automatically if posted by trusted users, if you want to be added as a trusted user join the discord and message me.

Scores may be triplets or quadruplets.  u/GltyBystndr made [a script](https://www.reddit.com/r/opus_magnum/comments/7scj7i/official_record_submission_thread/dve413n/) that scrapes your solution folder and generates quadruplets in the form

    <cost>/<cycles>/<area>/<instructions>

Triplets should be

    <cost>/<cycles>/<area>

for free space puzzles, and

    <cost>/<cycles>/<instructions>

for production puzzles.

## Attribution

This is not intended to be a competitive leaderboard, but rather an informative one.  The community as a whole can help improve the known bests.  People shouldn't worry about sharing their solutions for fear someone else snipes a piece off the tiebreaker.  As such, scores are not attributed.

# Alternate leaderboard site

jinyou, a player with many contributions to this leaderboard but no access to post to reddit from his country, has created a second site with a backup leaderboard.  The gifs on this second site are hosted in a manner which may for some users be more responsive than web.opendrive where his solutions are uploaded for linking here.

Find the backup leaderboards at http://jinyou.byethost5.com/Opus_Magnum_gif.html

# Leaderboards
"""

const val suffix = """

#Theoretical limits

Below are tables listing the known record and the theoretical limit for each metric individually.

[Cost](https://www.reddit.com/r/opus_magnum/comments/cl7303/list_of_current_cost_optimal_scores/)

[Cycles](https://www.reddit.com/r/opus_magnum/comments/7qmkv6/list_of_current_cycle_optimal_scores/)

[Area](https://www.reddit.com/r/opus_magnum/comments/7house/list_of_current_area_optimal_scores/)"""

@Component
class OmRedditWikiGenerator(private val reddit: RedditService) : AbstractOmPageGenerator(
    mapOf("." to listOf(GC, GA, GX, GCP, GI, GXP, CG, CA, CX, CGP, CI, CXP, AG, AC, AX, IG, IC, IX, SG, SGP, SC, SCP, SA, SI)),
) {
    companion object {
        private const val wikiPage = "index"
        private const val datePrefix = "Table built on "
    }

    private val costCategories = listOf(GC, GA, GX, GCP, GI, GXP)
    private val cycleCategories = listOf(CG, CA, CX, CGP, CI, CXP)
    private val areaInstructionCategories = listOf(AG, AC, AX, IG, IC, IX)
    private val sumCategories = listOf(SG, SGP, SC, SCP, SA, SI)

    private fun filterRecords(records: Map<OmRecord, Set<OmCategory>>, filter: List<OmCategory>): MutableList<Pair<OmRecord, List<OmCategory>>> {
        return records.map { (record, categories) -> Pair(record, categories.filter { filter.contains(it) }.sorted()) }
            .filter { it.second.isNotEmpty() }
            .sortedBy { (_, categories) -> categories.first() }
            .toMutableList()
    }

    private fun Pair<OmRecord, List<OmCategory>>?.toMarkdown(): String {
        if (this == null) return ""
        val score = first.score.toDisplayString(DisplayContext(StringFormat.MARKDOWN, second))
        return "${first.displayLink ?.let { "[$score](it)" } ?: score}${if (second.any { it.name.contains("X") }) "*" else ""}"
    }

    override fun GitRepository.ReadWriteAccess.updatePage(dir: File, categories: List<OmCategory>, data: Map<Puzzle, Map<OmRecord, Set<OmCategory>>>) {
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
                val entry = data[puzzle] ?: emptyMap()
                table += "[**${puzzle.displayName}**](##Frontier: ${entry.keys.joinToString(" ") { it.score.toDisplayString() }}##)"

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
        table += datePrefix + OffsetDateTime.now(ZoneOffset.UTC)
        val content = "$prefix\n$table\n$suffix".trim()
        if (content.lines().filter { !it.contains(datePrefix) } != reddit.getWikiPage(OPUS_MAGNUM, wikiPage).lines().filter { !it.contains(datePrefix) }) {
            reddit.updateWikiPage(OPUS_MAGNUM, wikiPage, content, "bot update")
        }
    }
}