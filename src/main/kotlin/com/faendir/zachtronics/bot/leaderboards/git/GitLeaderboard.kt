package com.faendir.zachtronics.bot.leaderboards.git

import com.faendir.zachtronics.bot.config.GitProperties
import com.faendir.zachtronics.bot.git.GitRepository
import com.faendir.zachtronics.bot.leaderboards.Leaderboard
import com.faendir.zachtronics.bot.leaderboards.UpdateResult
import com.faendir.zachtronics.bot.model.om.*
import com.faendir.zachtronics.bot.model.om.OmCategory.*
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
class GitLeaderboard(gitProperties: GitProperties) : GitRepository(gitProperties, "om-leaderboard", "https://github.com/F43nd1r/om-leaderboard.git"),
                                                     Leaderboard<OmCategory, OmScore, OmPuzzle, OmRecord> {
    companion object {
        private const val scoreFileName = "scores.json"
    }

    override val supportedCategories: List<OmCategory> = listOf(WIDTH, HEIGHT)

    @PostConstruct
    fun onStartUp() {
        access {
            File(repo, scoreFileName).takeIf { it.exists() }?.let { file ->
                generatePage(Json.decodeFromString(file.readText()))
                if (status().changed.isNotEmpty()) {
                    commitAndPush("Update page formatting")
                }
            }
        }
    }

    override fun update(puzzle: OmPuzzle, record: OmRecord): UpdateResult<OmCategory, OmScore> {
        return access {
            val scoreFile = File(repo, scoreFileName)
            val recordList: RecordList = Json.decodeFromString(scoreFile.readText())
            val betterExists = mutableMapOf<OmCategory, OmScore>()
            val success = mutableMapOf<OmCategory, OmScore?>()
            val categories = supportedCategories.filter { it.supportsPuzzle(puzzle) && it.supportsScore(record.score) }
            for (category in categories) {
                val oldRecord = recordList[puzzle]?.get(category)
                if (oldRecord == null || category.isBetterOrEqual(record.score, oldRecord.score) && oldRecord.link != record.link) {
                    recordList[puzzle] = (recordList[puzzle] ?: mutableMapOf()).also { records ->
                        records[category] = OmRecord(category.normalizeScore(record.score), record.link)
                    }
                    success[category] = oldRecord?.score
                } else {
                    betterExists[category] = oldRecord.score
                }
            }
            return@access if (success.isNotEmpty()) {
                updateRemote(scoreFile, recordList, record.author, puzzle, record.score, success.keys.map { it.displayName })
                UpdateResult.Success(success)
            } else {
                UpdateResult.BetterExists(betterExists)
            }
        }
    }

    private fun GitRepository.AccessScope.updateRemote(scoreFile: File, recordList: RecordList, user: String?, puzzle: OmPuzzle, score: OmScore, updated: Collection<String>) {
        scoreFile.writeText(Json { prettyPrint = true }.encodeToString(recordList))
        add(scoreFile)
        generatePage(recordList)
        commitAndPush(user, puzzle, score, updated)
    }

    private fun AccessScope.generatePage(recordList: RecordList) {
        val folder = File(repo, "templates")
        val mainTemplate = File(folder, "main.html").readText()
        val groupTemplate = File(folder, "group.html").readText()
        val puzzleTemplate = File(folder, "puzzle.html").readText()
        val blockTemplate = File(folder, "block.html").readText()
        val scoreTemplate = File(folder, "score.html").readText()
        val gifTemplate = File(folder, "gif.html").readText()
        val videoTemplate = File(folder, "video.html").readText()
        fun generateRecord(category: OmCategory, record: OmRecord): String {
            return scoreTemplate.format(record.link,
                record.score.toDisplayString({ category.sortScoreParts(this) }) { _, value -> format(value) },
                if (record.link.endsWith("mp4") || record.link.endsWith("webm")) {
                    videoTemplate.format(record.link)
                } else {
                    gifTemplate.format(record.link)
                })
        }

        val text = mainTemplate.format(OffsetDateTime.now(ZoneOffset.UTC), OmGroup.values().joinToString("\n") { group ->
            val puzzles = OmPuzzle.values().filter { it.group == group && it.type != OmType.PRODUCTION }
            if (puzzles.isNotEmpty()) {
                groupTemplate.format(group.displayName, puzzles.joinToString("\n") { puzzle ->
                    val heightRecord = recordList[puzzle]?.get(HEIGHT)
                    val widthRecord = recordList[puzzle]?.get(WIDTH)
                    puzzleTemplate.format(puzzle.displayName,
                        heightRecord?.let { generateRecord(HEIGHT, it) } ?: "",
                        if (puzzle.type == OmType.INFINITE) blockTemplate else widthRecord?.let { generateRecord(WIDTH, it) } ?: "")
                })
            } else {
                ""
            }
        })
        val file = File(repo, "index.html")
        val old = file.readText()
        if (old.lines().filter { !it.contains("last updated on") } != text.lines().filter { !it.contains("last updated on") }) {
            file.writeText(text)
            add(file)
        }
    }

    override fun get(puzzle: OmPuzzle, category: OmCategory): OmRecord? {
        return access {
            val scoreFile = File(repo, scoreFileName)
            val recordList: RecordList = Json.decodeFromString(scoreFile.readText())
            recordList[puzzle]?.get(category)?.let { OmRecord(it.score, it.link) }
        }
    }
}

typealias RecordList = MutableMap<OmPuzzle, MutableMap<OmCategory, OmRecord>>