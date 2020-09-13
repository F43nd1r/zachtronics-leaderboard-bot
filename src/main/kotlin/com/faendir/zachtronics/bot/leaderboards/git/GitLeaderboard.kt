package com.faendir.zachtronics.bot.leaderboards.git

import com.faendir.zachtronics.bot.config.GitProperties
import com.faendir.zachtronics.bot.git.GitRepository
import com.faendir.zachtronics.bot.leaderboards.Leaderboard
import com.faendir.zachtronics.bot.leaderboards.UpdateResult
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.om.*
import com.faendir.zachtronics.bot.model.om.OmScorePart.*
import com.roxstudio.utils.CUrl
import org.springframework.stereotype.Component
import java.io.File
import java.net.URLEncoder

@Component
class GitLeaderboard(gitProperties: GitProperties) : GitRepository(gitProperties, "om-leaderboard", "https://github.com/F43nd1r/om-leaderboard.git"),
                                                     Leaderboard<OmCategory, OmScore, OmPuzzle> {
    override val game = OpusMagnum

    override val supportedCategories: Collection<OmCategory> = listOf(OmCategory.WIDTH, OmCategory.HEIGHT)

    private fun Puzzle.findPuzzleDir(repo: File): File {
        val puzzleDirs = findPuzzleDirMatches(repo, displayName.replace(Regex("\\s"), "_"))
        check(puzzleDirs.size == 1)
        return puzzleDirs.first()
    }

    override fun update(user: String, puzzle: OmPuzzle, categories: List<OmCategory>, score: OmScore, link: String): UpdateResult<OmCategory, OmScore> {
        return access {
            val linkContent = try {
                CUrl(link).exec()
            } catch (e: Exception) {
                return@access UpdateResult.BrokenLink()
            }
            val betterExists = mutableMapOf<OmCategory, OmScore>()
            val success = mutableMapOf<OmCategory, OmScore?>()
            for (category in categories) {
                val prefix = category.displayName
                val puzzleDir = puzzle.findPuzzleDir(repo)
                val existingFile = puzzleDir.listFiles()?.find { it.name.startsWith(prefix) }
                val existingScore = existingFile?.getScore()
                if (existingScore == null || category.isBetterOrEqual(score, existingScore) && !linkContent.contentEquals(existingFile.readBytes())) {
                    val file = File(puzzleDir, "${prefix}_${category.normalizeScore(score).toString("_", false)}.${link.substringAfterLast('.')}")
                    file.writeBytes(linkContent)
                    if (file != existingFile) {
                        existingFile?.let { rm(it) }
                    }
                    success[category] = existingScore
                } else {
                    betterExists[category] = existingScore
                }
            }
            return@access if (success.isNotEmpty()) {
                Runtime.getRuntime().exec("/bin/bash ./generate.sh", null, repo).waitFor()
                addAll()
                if (status().added.isNotEmpty()) {
                    commitAndPush(user, puzzle, score, success.keys.map { it.displayName })
                }
                UpdateResult.Success(success)
            } else {
                UpdateResult.BetterExists(betterExists)
            }
        }
    }

    override fun get(puzzle: OmPuzzle, category: OmCategory): OmRecord? {
        return access {
            val prefix = category.displayName
            val puzzleDir = puzzle.findPuzzleDir(repo)
            puzzleDir.listFiles()?.find { it.name.startsWith(prefix) }?.let {
                OmRecord(category, it.getScore(), "https://f43nd1r.github.io/om-leaderboard/${
                    puzzleDir.relativeTo(repo).path
                }/${URLEncoder.encode(it.name, Charsets.US_ASCII).replace("+", "%20")}")
            }
        }
    }

    private fun findPuzzleDirMatches(repo: File, name: String): List<File> =
        File(repo, "gif").listFiles()?.sorted()?.flatMap { it.listFiles()?.asIterable()?.sorted() ?: emptyList() }?.filter { it.isDirectory }
            ?.filter { it.name.contains(name, ignoreCase = true) }?.toList() ?: emptyList()

    private fun File.getScore(): OmScore {
        return Regex("(?<category>[HW])_(?<hw>[\\d.]+)_(?<cycles>\\d+)_(?<cost>\\d+).*").matchEntire(name)!!.let {
            OmScore(linkedMapOf((if (it.groups["category"]!!.value == "H") HEIGHT else WIDTH) to it.groups["hw"]!!.value.toDouble(),
                CYCLES to it.groups["cycles"]!!.value.toDouble(),
                COST to it.groups["cost"]!!.value.toDouble()))
        }
    }
}