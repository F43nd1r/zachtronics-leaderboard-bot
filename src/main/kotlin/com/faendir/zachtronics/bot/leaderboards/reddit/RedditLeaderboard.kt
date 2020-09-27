package com.faendir.zachtronics.bot.leaderboards.reddit

import com.faendir.zachtronics.bot.git.GitRepository
import com.faendir.zachtronics.bot.imgur.ImgurService
import com.faendir.zachtronics.bot.leaderboards.Leaderboard
import com.faendir.zachtronics.bot.leaderboards.UpdateResult
import com.faendir.zachtronics.bot.model.om.*
import com.faendir.zachtronics.bot.model.om.OmCategory.*
import com.faendir.zachtronics.bot.model.om.OmScorePart.*
import com.faendir.zachtronics.bot.model.om.OmType.PRODUCTION
import com.faendir.zachtronics.bot.reddit.RedditService
import com.faendir.zachtronics.bot.reddit.Subreddit
import com.faendir.zachtronics.bot.utils.toMap
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
class RedditLeaderboard(private val redditService: RedditService, private val imgurService: ImgurService) : Leaderboard<OmCategory, OmScore, OmPuzzle, OmRecord> {
    companion object {
        private const val scoreFileName = "scores.json"
        private const val wikiPage = "index"
        private const val dateLinePrefix = "Table built on "
    }

    @PostConstruct
    fun onStartUp() {
        redditService.access { File(repo, scoreFileName).takeIf { it.exists() }?.readText()?.let { updateRedditWiki(Json.decodeFromString(it), repo) } }
    }

    override val supportedCategories: List<OmCategory> = listOf(GC, GA, GX, GCP, GI, GXP, CG, CA, CX, CGP, CI, CXP, AG, AC, AX, IG, IC, IX, SG, SGP, SC, SCP, SA, SI)
    override fun update(puzzle: OmPuzzle, record: OmRecord): UpdateResult<OmCategory, OmScore> {
        return redditService.access {
            val scoreFile = File(repo, scoreFileName)
            val recordList: RecordList = Json.decodeFromString(scoreFile.readText())
            val betterExists = mutableMapOf<OmCategory, OmScore>()
            val success = mutableMapOf<OmCategory, OmScore?>()
            val rehostedLink by lazy { imgurService.tryRehost(record.link) }
            val categories = supportedCategories.filter { it.supportsPuzzle(puzzle) && it.supportsScore(record.score) }
            for (category in categories) {
                val oldRecord = recordList[puzzle]?.records?.get(category)
                if (oldRecord == null || category.isBetterOrEqual(record.score, oldRecord.score) && oldRecord.link != record.link) {
                    recordList[puzzle] = (recordList[puzzle] ?: PuzzleEntry()).apply {
                        records[category] = OmRecord(category.normalizeScore(record.score), rehostedLink)
                    }
                    success[category] = oldRecord?.score
                } else {
                    betterExists[category] = oldRecord.score
                }
            }
            var paretoUpdate = false
            val requiredParts = listOf(COST, CYCLES, if (puzzle.type == PRODUCTION) INSTRUCTIONS else AREA)
            if (record.score.parts.keys.containsAll(requiredParts)) {
                val paretoScore = OmScore(requiredParts.map { it to record.score.parts[it]!! }.toMap(LinkedHashMap()))
                val entry = (recordList[puzzle] ?: PuzzleEntry())
                if (!entry.pareto.contains(paretoScore) && entry.pareto.all { !it.isStrictlyBetter(paretoScore) }) {
                    entry.pareto.removeIf { paretoScore.isStrictlyBetter(it) }
                    entry.pareto.add(paretoScore)
                    recordList[puzzle] = entry
                    paretoUpdate = true
                }
            }
            when {
                success.isNotEmpty() -> {
                    updateRemote(scoreFile, recordList, record.author, puzzle, record.score, success.keys.map { it.displayName })
                    UpdateResult.Success(success)
                }
                paretoUpdate -> {
                    updateRemote(scoreFile, recordList, record.author, puzzle, record.score, listOf("PARETO"))
                    UpdateResult.ParetoUpdate()
                }
                else -> {
                    UpdateResult.BetterExists(betterExists)
                }
            }
        }
    }

    private fun GitRepository.AccessScope.updateRemote(scoreFile: File, recordList: RecordList, user: String?, puzzle: OmPuzzle, score: OmScore, updated: Collection<String>) {
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
                val entry = recordList[puzzle] ?: PuzzleEntry()
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
        table += dateLinePrefix + OffsetDateTime.now(ZoneOffset.UTC)
        val prefix = File(repo, "prefix.md").readText()
        val suffix = File(repo, "suffix.md").readText()
        val wiki = redditService.subreddit(Subreddit.OPUS_MAGNUM).wiki()
        val content = "$prefix\n$table\n$suffix".trim()
        if (content.lines().filter { !it.contains(dateLinePrefix) } != wiki.page(wikiPage).content.lines().filter { !it.contains(dateLinePrefix) }) {
            wiki.update(wikiPage, content, "bot update")
        }
    }

    private fun filterRecords(records: Map<OmCategory, OmRecord>, filter: List<OmCategory>): MutableList<Map<OmCategory, OmRecord>> {
        return records.filter { filter.contains(it.key) }.entries.groupBy { it.value.link }.values.map { list -> list.sortedBy { it.key }.toMap() }
            .sortedBy { it.keys.first() }
            .toMutableList()
    }

    private fun Map<OmCategory, OmRecord>?.toMarkdown(): String {
        if (this == null || isEmpty()) return ""
        return "[${values.first().score.toShortDisplayString()}](${values.first().link})${if (any { it.key.name.contains("X") }) "*" else ""}"
    }

    override fun get(puzzle: OmPuzzle, category: OmCategory): OmRecord? {
        if (!supportedCategories.contains(category)) return null
        return redditService.access {
            val scoreFile = File(repo, scoreFileName)
            val recordList: RecordList = Json.decodeFromString(scoreFile.readText())
            recordList[puzzle]?.records?.get(category)?.let { OmRecord(it.score, it.link) }
        }
    }
}

@Serializable
data class PuzzleEntry(val records: MutableMap<OmCategory, OmRecord> = mutableMapOf(), val pareto: MutableList<OmScore> = mutableListOf())

typealias RecordList = MutableMap<OmPuzzle, PuzzleEntry>

fun OmScore.isStrictlyBetter(other: OmScore): Boolean {
    val compares = parts.map { it.value.compareTo(other.parts[it.key]!!) }
    return compares.none { it > 0 } && compares.any { it < 0 }
}