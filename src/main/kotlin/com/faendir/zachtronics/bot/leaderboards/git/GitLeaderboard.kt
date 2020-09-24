package com.faendir.zachtronics.bot.leaderboards.git

import com.faendir.zachtronics.bot.config.GitProperties
import com.faendir.zachtronics.bot.git.GitRepository
import com.faendir.zachtronics.bot.leaderboards.Leaderboard
import com.faendir.zachtronics.bot.leaderboards.UpdateResult
import com.faendir.zachtronics.bot.model.om.*
import com.faendir.zachtronics.bot.model.om.OmCategory.*
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
        private const val whDir = "wh"
        private const val overlapDir = "overlap"
    }

    override val supportedCategories: List<OmCategory> = listOf(HEIGHT, WIDTH, OCG, OCA, OCX)

    private val directories = mapOf(HEIGHT to whDir, WIDTH to whDir, OCG to overlapDir, OCA to overlapDir, OCX to overlapDir)
    private val tableHeaders = mapOf(HEIGHT to "Height", WIDTH to "Width", OCG to "Cycles/Cost", OCA to "Cycles/Area", OCX to "Cycles/Cost*Area")


    @PostConstruct
    fun onStartUp() {
        access {
            for (dirName in directories.values.distinct()) {
                val dir = File(repo, dirName)
                File(dir, scoreFileName).takeIf { it.exists() }?.let { file ->
                    generatePage(dir, directories.filter { it.value == dirName }.keys.toList(), Json.decodeFromString(file.readText()))
                    if (status().changed.isNotEmpty()) {
                        commitAndPush("Update page formatting")
                    }
                }
            }
        }
    }

    override fun update(puzzle: OmPuzzle, record: OmRecord): UpdateResult<OmCategory, OmScore> {
        return access {
            val betterExists = mutableMapOf<OmCategory, OmScore>()
            val success = mutableMapOf<OmCategory, OmScore?>()
            directories.asIterable().groupBy { it.value }.mapValues { entry -> entry.value.map { it.key } }.forEach { (dirName, dirCategories) ->
                val dir = File(repo, dirName)
                val scoreFile = File(dir, scoreFileName)
                val recordList: RecordList = Json.decodeFromString(scoreFile.readText())
                val categories = dirCategories.filter { it.supportsPuzzle(puzzle) && it.supportsScore(record.score) }
                var changed = false
                for (category in categories) {
                    val oldRecord = recordList[puzzle]?.get(category)
                    if (oldRecord == null || category.isBetterOrEqual(record.score, oldRecord.score) && oldRecord.link != record.link) {
                        recordList[puzzle] = (recordList[puzzle] ?: mutableMapOf()).also { records ->
                            records[category] = OmRecord(category.normalizeScore(record.score), record.link)
                            changed = true
                        }
                        success[category] = oldRecord?.score
                    } else {
                        betterExists[category] = oldRecord.score
                    }
                }
                if (changed) {
                    scoreFile.writeText(Json { prettyPrint = true }.encodeToString(recordList))
                    add(scoreFile)
                    generatePage(dir, dirCategories, recordList)
                }
            }
            return@access if (success.isNotEmpty()) {
                commitAndPush(record.author, puzzle, record.score, success.keys.map { it.displayName })
                UpdateResult.Success(success)
            } else {
                UpdateResult.BetterExists(betterExists)
            }
        }
    }

    private fun AccessScope.generatePage(dir: File, categories: List<OmCategory>, recordList: RecordList) {
        val folder = File(repo, "templates")
        val mainTemplate = File(folder, "main.html").readText()
        val groupTemplate = File(folder, "group.html").readText()
        val puzzleTemplate = File(folder, "puzzle.html").readText()
        val blockTemplate = File(folder, "block.html").readText()
        val scoreTemplate = File(folder, "score.html").readText()
        val gifTemplate = File(folder, "gif.html").readText()
        val videoTemplate = File(folder, "video.html").readText()
        val headerTemplate = File(folder, "columnheader.html").readText()
        val cellTemplate = File(folder, "cell.html").readText()
        val explanation = File(dir, "explanation.html").readText()
        fun generateRecord(category: OmCategory, record: OmRecord): String {
            return scoreTemplate.format(record.link,
                if(category == HEIGHT || category == WIDTH) {
                    record.score.toDisplayString({ category.sortScoreParts(this) }) { _, value -> format(value) }
                } else {
                    record.score.toShortDisplayString()
                },
                if (record.link.endsWith("mp4") || record.link.endsWith("webm")) {
                    videoTemplate.format(record.link)
                } else {
                    gifTemplate.format(record.link)
                })
        }

        val text = mainTemplate.format(explanation, OffsetDateTime.now(ZoneOffset.UTC), OmGroup.values().joinToString("\n") { group ->
            val puzzles = OmPuzzle.values().filter { it.group == group && it.type != OmType.PRODUCTION }
            if (puzzles.isNotEmpty()) {
                groupTemplate.format(group.displayName, categories.joinToString("\n") { category ->
                    headerTemplate.format(tableHeaders[category])
                }, puzzles.joinToString("\n") { puzzle ->
                    puzzleTemplate.format(puzzle.displayName, categories.joinToString("\n") { category ->
                        cellTemplate.format(if (category.supportsPuzzle(puzzle)) {
                            recordList[puzzle]?.get(category)?.let { generateRecord(category, it) } ?: ""
                        } else {
                            blockTemplate
                        })
                    })
                })
            } else {
                ""
            }
        })
        val file = File(dir, "index.html")
        val old = file.readText()
        if (old.lines().filter { !it.contains("last updated on") } != text.lines().filter { !it.contains("last updated on") }) {
            file.writeText(text)
            add(file)
        }
    }

    override fun get(puzzle: OmPuzzle, category: OmCategory): OmRecord? {
        return access {
            val scoreFile = File(File(repo, directories[category] ?: return@access null), scoreFileName)
            val recordList: RecordList = Json.decodeFromString(scoreFile.readText())
            recordList[puzzle]?.get(category)?.let { OmRecord(it.score, it.link) }
        }
    }
}

typealias RecordList = MutableMap<OmPuzzle, MutableMap<OmCategory, OmRecord>>