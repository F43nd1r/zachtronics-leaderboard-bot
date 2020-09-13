package com.faendir.zachtronics.bot.leaderboards.reddit

import com.faendir.zachtronics.bot.git.GitRepository
import com.faendir.zachtronics.bot.leaderboards.Leaderboard
import com.faendir.zachtronics.bot.leaderboards.UpdateResult
import com.faendir.zachtronics.bot.model.om.*
import com.faendir.zachtronics.bot.model.om.OmCategory.*
import com.faendir.zachtronics.bot.model.om.OmScorePart.*
import com.faendir.zachtronics.bot.model.om.OmType.PRODUCTION
import com.faendir.zachtronics.bot.reddit.RedditService
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
class RedditLeaderboard(private val redditService: RedditService) : Leaderboard<OmCategory, OmScore, OmPuzzle> {
    companion object {
        private const val scoreFileName = "scores.json"
    }

    @PostConstruct
    fun onStartUp() {
        redditService.access { File(repo, scoreFileName).takeIf { it.exists() }?.readText()?.let { updateRedditWiki(Json.decodeFromString(it), repo) } }
    }

    override val game = OpusMagnum

    override val supportedCategories: Collection<OmCategory> = listOf(GC, GA, GX, GCP, GI, GXP, CG, CA, CX, CGP, CI, CXP, AG, AC, AX, IG, IC, IX, SG, SGP, SC, SCP, SA, SI)

    override fun update(user: String, puzzle: OmPuzzle, categories: List<OmCategory>, score: OmScore, link: String): UpdateResult<OmCategory, OmScore> {
        return redditService.access {
            val scoreFile = File(repo, scoreFileName)
            val recordList: RecordList = Json.decodeFromString(scoreFile.readText())
            val betterExists = mutableMapOf<OmCategory, OmScore>()
            val success = mutableMapOf<OmCategory, OmScore?>()
            for (category in categories) {
                val oldRecord = recordList[puzzle]?.records?.find { it.category == category }
                if (oldRecord == null || category.isBetterOrEqual(category.normalizeScore(score), category.normalizeScore(oldRecord.score)) && oldRecord.link != link) {
                    recordList[puzzle] = (recordList[puzzle] ?: PuzzleEntry(mutableListOf(), mutableListOf())).apply {
                        if (oldRecord != null) records.remove(oldRecord)
                        records.add(OmRecord(category, category.normalizeScore(score), link))
                    }
                    success[category] = oldRecord?.score
                } else {
                    betterExists[category] = oldRecord.score
                }
            }
            var paretoUpdate = false
            val requiredParts = listOf(COST, CYCLES, if (puzzle.type == PRODUCTION) INSTRUCTIONS else AREA)
            if (score.parts.keys.containsAll(requiredParts)) {
                val paretoScore = OmScore(requiredParts.map { it to score.parts[it]!! }.toMap(LinkedHashMap()))
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
                    UpdateResult.ParetoUpdate()
                }
                else -> {
                    UpdateResult.BetterExists(betterExists)
                }
            }
        }
    }

    private fun GitRepository.AccessScope.updateRemote(scoreFile: File, recordList: RecordList, user: String, puzzle: OmPuzzle, score: OmScore, updated: Collection<String>) {
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

    private fun filterRecords(records: List<OmRecord>, filter: List<OmCategory>): MutableList<List<OmRecord>> {
        return records.filter { filter.contains(it.category) }.groupBy { it.link }.values.map { it.sortedBy(OmRecord::category) }.sortedBy { it.first().category }.toMutableList()
    }

    private fun List<OmRecord>?.toMarkdown(): String {
        if (this == null || isEmpty()) return ""
        return "[${
            first().score.reorderToStandard().toString("/", false)
        }](${first().link})${if (any { it.category.name.contains("X") }) "*" else ""}"
    }

    override fun get(puzzle: OmPuzzle, category: OmCategory): OmRecord? {
        return redditService.access {
            val scoreFile = File(repo, scoreFileName)
            val recordList: RecordList = Json.decodeFromString(scoreFile.readText())
            recordList[puzzle]?.records?.find { it.category == category }?.let { OmRecord(category, it.score, it.link) }
        }
    }
}

@Serializable
data class PuzzleEntry(val records: MutableList<OmRecord>, val pareto: MutableList<OmScore>)

typealias RecordList = MutableMap<OmPuzzle, PuzzleEntry>

fun OmScore.isStrictlyBetter(other: OmScore): Boolean {
    val compares = parts.map { it.value.compareTo(other.parts[it.key]!!) }
    return compares.none { it > 0 } && compares.any { it < 0 }
}