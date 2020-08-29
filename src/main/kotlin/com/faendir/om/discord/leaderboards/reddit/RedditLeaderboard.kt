package com.faendir.om.discord.leaderboards.reddit

import com.faendir.om.discord.leaderboards.GetResult
import com.faendir.om.discord.leaderboards.Leaderboard
import com.faendir.om.discord.leaderboards.UpdateResult
import com.faendir.om.discord.leaderboards.git.GitProperties
import com.faendir.om.discord.model.Category
import com.faendir.om.discord.model.Category.*
import com.faendir.om.discord.model.Score
import com.faendir.om.discord.model.ScorePart
import com.faendir.om.discord.puzzle.Group
import com.faendir.om.discord.puzzle.Puzzle
import com.faendir.om.discord.puzzle.Type
import com.faendir.om.discord.reddit.RedditService
import com.faendir.om.discord.utils.commitAndPushChanges
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.eclipse.jgit.api.Git
import org.springframework.stereotype.Component
import java.io.File
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Component
class RedditLeaderboard(private val redditService: RedditService, private val gitProperties: GitProperties) :
    Leaderboard {

    override val supportedCategories: Collection<Category> = listOf(
        GC, GA, GX, GCP, GI, GXP, CG, CA, CX, CGP, CI, CXP, AG, AC, AX, IG, IC, IX, SUM, SUMP
    )

    override fun update(
        user: String, puzzle: Puzzle, categories: List<Category>, score: Score, link: String
    ): UpdateResult {
        return redditService.accessRepo { git, repo ->
            val scoreFile = File(repo, "scores.json")
            val recordList: RecordList = Json.decodeFromString(scoreFile.readText())
            val betterExists = mutableMapOf<Category, Score>()
            val success = mutableMapOf<Category, Score?>()
            for (category in categories) {
                val oldRecord = recordList[puzzle]?.records?.find { it.category == category }
                if (oldRecord == null || score.isBetterOrEqualTo(category, oldRecord.score) && oldRecord.link != link) {
                    recordList[puzzle] = (recordList[puzzle] ?: PuzzleEntry(mutableListOf(), mutableListOf())).apply {
                        if (oldRecord != null) records.remove(oldRecord)
                        records.add(Record(category, category.normalizeScore(score), link))
                    }
                    success[category] = oldRecord?.score
                } else {
                    betterExists[category] = oldRecord.score
                }
            }
            var paretoUpdate = false
            val requiredParts = listOf(
                ScorePart.COST,
                ScorePart.CYCLES,
                if (puzzle.type == Type.PRODUCTION) ScorePart.INSTRUCTIONS else ScorePart.AREA
            )
            if (score.parts.keys.containsAll(requiredParts)) {
                val paretoScore = Score(requiredParts.map { it to score.parts[it]!! }.toMap(LinkedHashMap()))
                val entry = (recordList[puzzle] ?: PuzzleEntry(mutableListOf(), mutableListOf()))
                if (!entry.pareto.contains(paretoScore) && entry.pareto.all { !it.isStrictlyBetter(paretoScore) }) {
                    entry.pareto.removeIf { paretoScore.isStrictlyBetter(it) }
                    entry.pareto.add(paretoScore)
                    recordList[puzzle] = entry
                    paretoUpdate = true
                }
            }
            when {
                success.isNotEmpty() -> {
                    updateRemote(scoreFile, recordList, git, repo, user, puzzle, success.keys.map { it.displayName })
                    UpdateResult.Success(success)
                }
                paretoUpdate -> {
                    updateRemote(scoreFile, recordList, git, repo, user, puzzle, listOf("pareto"))
                    UpdateResult.ParetoUpdate
                }
                else -> {
                    UpdateResult.BetterExists(betterExists)
                }
            }
        }
    }

    private fun updateRemote(
        scoreFile: File, recordList: RecordList,
        git: Git, repo: File, user: String, puzzle: Puzzle,
        updated: Collection<String>
    ) {
        scoreFile.writeText(Json.encodeToString(recordList))
        git.add().addFilepattern(scoreFile.name).call()
        git.commitAndPushChanges(user, puzzle, updated, gitProperties)
        updateRedditWiki(recordList, repo)
    }

    private val costCategories = listOf(GC, GA, GX, GCP, GI, GXP)
    private val cycleCategories = listOf(CG, CA, CX, CGP, CI, CXP)
    private val areaInstructionCategories = listOf(AG, AC, AX, IG, IC, IX)
    private val sumCategories = listOf(SUM, SUMP)

    private fun updateRedditWiki(recordList: RecordList, repo: File) {
        var table = ""
        for (group in Group.values()) {
            table += "## ${group.displayName}\n\n"
            val puzzles = Puzzle.values().filter { it.group == group }
            val thirdCategory = puzzles.map {
                when (it.type) {
                    Type.NORMAL, Type.INFINITE -> "Area"
                    Type.PRODUCTION -> "Instructions"
                }
            }.distinct().joinToString("/")
            table += "Name|Cost|Cycles|${thirdCategory}|Sum\n:-|:-|:-|:-|:-\n"
            for (puzzle in puzzles) {
                val entry = recordList[puzzle] ?: PuzzleEntry(mutableListOf(), mutableListOf())
                table += "[**${puzzle.displayName}**](##Frontier: ${
                    entry.pareto.joinToString(" ") {
                        it.toString(
                            "/",
                            false
                        )
                    }
                }##)"

                val costScores = filterRecords(entry.records, costCategories)
                val cycleScores = filterRecords(entry.records, cycleCategories)
                val areaInstructionScores = filterRecords(entry.records, areaInstructionCategories)
                val sumScores = filterRecords(entry.records, sumCategories)
                while (costScores.isNotEmpty() || cycleScores.isNotEmpty() ||
                    areaInstructionScores.isNotEmpty() || sumScores.isNotEmpty()
                ) {
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
        table += "Table built on ${OffsetDateTime.now(ZoneOffset.UTC)}"
        val prefix = File(repo, "prefix.md").readText()
        val suffix = File(repo, "suffix.md").readText()
        redditService.reddit.subreddit("opus_magnum").wiki()
            .update("index", "$prefix\n$table\n$suffix", "bot update")
    }

    private fun filterRecords(records: List<Record>, filter: List<Category>): MutableList<Record> {
        return records.filter { filter.contains(it.category) }.sortedBy { it.category.ordinal }
            .reversed().distinctBy { it.link }.reversed().toMutableList()
    }

    private fun Record?.toMarkdown(): String {
        if (this == null) return ""
        return "[${
            score.reorderToStandard().toString("/", false)
        }]($link)${if (category.name.contains("X")) "*" else ""}"
    }

    override fun get(puzzle: Puzzle, category: Category): GetResult {
        return redditService.accessRepo { _, repo ->
            val scoreFile = File(repo, "scores.json")
            val recordList: RecordList = Json.decodeFromString(scoreFile.readText())
            recordList[puzzle]?.records?.find { it.category == category }?.let { GetResult.Success(it.score, it.link) }
                ?: GetResult.NoScore
        }
    }
}

@Serializable
data class Record(val category: Category, val score: Score, val link: String)

@Serializable
data class PuzzleEntry(val records: MutableList<Record>, val pareto: MutableList<Score>)

typealias RecordList = MutableMap<Puzzle, PuzzleEntry>

fun Score.isStrictlyBetter(other: Score): Boolean {
    val compares = parts.map { it.value.compareTo(other.parts[it.key]!!) }
    return compares.none { it > 0 } && compares.any { it < 0 }
}