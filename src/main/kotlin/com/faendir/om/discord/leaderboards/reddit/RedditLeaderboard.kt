package com.faendir.om.discord.leaderboards.reddit

import com.faendir.om.discord.config.GitProperties
import com.faendir.om.discord.git.GitRepository
import com.faendir.om.discord.leaderboards.Leaderboard
import com.faendir.om.discord.leaderboards.UpdateResult
import com.faendir.om.discord.model.Category
import com.faendir.om.discord.model.Category.*
import com.faendir.om.discord.model.Record
import com.faendir.om.discord.model.Score
import com.faendir.om.discord.model.ScorePart
import com.faendir.om.discord.puzzle.Group
import com.faendir.om.discord.puzzle.Puzzle
import com.faendir.om.discord.puzzle.Type
import com.faendir.om.discord.reddit.RedditService
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Component
import java.io.File
import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.annotation.PostConstruct

@Component
class RedditLeaderboard(private val redditService: RedditService, private val gitProperties: GitProperties) : Leaderboard {
    companion object {
        private const val scoreFileName = "scores.json"
    }

    @PostConstruct
    fun onStartUp() {
        redditService.access { File(repo, scoreFileName).takeIf { it.exists() }?.readText()?.let { updateRedditWiki(Json.decodeFromString(it), repo) } }
    }

    override val supportedCategories: Collection<Category> = listOf(GC, GA, GX, GCP, GI, GXP, CG, CA, CX, CGP, CI, CXP, AG, AC, AX, IG, IC, IX, SG, SGP, SC, SCP, SA, SI)

    override fun update(user: String, puzzle: Puzzle, categories: List<Category>, score: Score, link: String): UpdateResult {
        return redditService.access {
            val scoreFile = File(repo, scoreFileName)
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
            val requiredParts = listOf(ScorePart.COST, ScorePart.CYCLES, if (puzzle.type == Type.PRODUCTION) ScorePart.INSTRUCTIONS else ScorePart.AREA)
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
                    updateRemote(scoreFile, recordList, user, puzzle, score, success.keys.map { it.displayName })
                    UpdateResult.Success(success)
                }
                paretoUpdate -> {
                    updateRemote(scoreFile, recordList, user, puzzle, score, listOf("PARETO"))
                    UpdateResult.ParetoUpdate
                }
                else -> {
                    UpdateResult.BetterExists(betterExists)
                }
            }
        }
    }

    private fun GitRepository.AccessScope.updateRemote(scoreFile: File, recordList: RecordList, user: String, puzzle: Puzzle, score: Score, updated: Collection<String>) {
        scoreFile.writeText(Json { prettyPrint = true }.encodeToString(recordList))
        add(scoreFile)
        commitAndPush(user, puzzle, score, updated)
        updateRedditWiki(recordList, repo)
    }

    private val costCategories = listOf(GC, GA, GX, GCP, GI, GXP)
    private val cycleCategories = listOf(CG, CA, CX, CGP, CI, CXP)
    private val areaInstructionCategories = listOf(AG, AC, AX, IG, IC, IX)
    private val sumCategories = listOf(SG, SGP, SC, SCP, SA, SI)

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
                        it.toString("/", false)
                    }
                }##)"

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
        table += "Table built on ${OffsetDateTime.now(ZoneOffset.UTC)}"
        val prefix = File(repo, "prefix.md").readText()
        val suffix = File(repo, "suffix.md").readText()
        redditService.reddit.subreddit("opus_magnum").wiki().update("index", "$prefix\n$table\n$suffix", "bot update")
    }

    private fun filterRecords(records: List<Record>, filter: List<Category>): MutableList<List<Record>> {
        return records.filter { filter.contains(it.category) }.groupBy { it.link }.values.map { it.sortedBy(Record::category) }.sortedBy { it.first().category }.toMutableList()
    }

    private fun List<Record>?.toMarkdown(): String {
        if (this == null || isEmpty()) return ""
        return "[${
            first().score.reorderToStandard().toString("/", false)
        }](${first().link})${if (any { it.category.name.contains("X") }) "*" else ""}"
    }

    override fun get(puzzle: Puzzle, category: Category): Record? {
        return redditService.access {
            val scoreFile = File(repo, scoreFileName)
            val recordList: RecordList = Json.decodeFromString(scoreFile.readText())
            recordList[puzzle]?.records?.find { it.category == category }?.let { Record(category, it.score, it.link) }
        }
    }
}

@Serializable
data class PuzzleEntry(val records: MutableList<Record>, val pareto: MutableList<Score>)

typealias RecordList = MutableMap<Puzzle, PuzzleEntry>

fun Score.isStrictlyBetter(other: Score): Boolean {
    val compares = parts.map { it.value.compareTo(other.parts[it.key]!!) }
    return compares.none { it > 0 } && compares.any { it < 0 }
}