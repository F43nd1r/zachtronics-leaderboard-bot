package com.faendir.om.discord.leaderboards.git

import com.faendir.om.discord.config.GitProperties
import com.faendir.om.discord.git.GitRepository
import com.faendir.om.discord.leaderboards.Leaderboard
import com.faendir.om.discord.leaderboards.UpdateResult
import com.faendir.om.discord.model.Category
import com.faendir.om.discord.model.Record
import com.faendir.om.discord.model.Score
import com.faendir.om.discord.model.ScorePart.*
import com.faendir.om.discord.puzzle.Puzzle
import com.roxstudio.utils.CUrl
import org.springframework.stereotype.Component
import java.io.File
import java.net.URLEncoder

@Component
class GitLeaderboard(gitProperties: GitProperties) : GitRepository(gitProperties, "om-leaderboard", "https://github.com/F43nd1r/om-leaderboard.git"), Leaderboard {

    override val supportedCategories = listOf(Category.WIDTH, Category.HEIGHT)

    private fun Puzzle.findPuzzleDir(repo: File): File {
        val puzzleDirs = findPuzzleDirMatches(repo, displayName.replace(Regex("\\s"), "_"))
        check(puzzleDirs.size == 1)
        return puzzleDirs.first()
    }

    override fun update(user: String, puzzle: Puzzle, categories: List<Category>, score: Score, link: String): UpdateResult {
        return access {
            val linkContent = try {
                CUrl(link).exec()
            } catch (e: Exception) {
                return@access UpdateResult.BrokenLink
            }
            val betterExists = mutableMapOf<Category, Score>()
            val success = mutableMapOf<Category, Score?>()
            for (category in categories) {
                val prefix = category.displayName
                val puzzleDir = puzzle.findPuzzleDir(repo)
                val existingFile = puzzleDir.listFiles()?.find { it.name.startsWith(prefix) }
                val existingScore = existingFile?.getScore()
                if (existingScore == null || score.isBetterOrEqualTo(category, existingScore) && !linkContent.contentEquals(existingFile.readBytes())) {
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

    override fun get(puzzle: Puzzle, category: Category): Record? {
        return access {
            val prefix = category.displayName
            val puzzleDir = puzzle.findPuzzleDir(repo)
            puzzleDir.listFiles()?.find { it.name.startsWith(prefix) }?.let {
                Record(category, it.getScore(), "https://f43nd1r.github.io/om-leaderboard/${
                    puzzleDir.relativeTo(repo).path
                }/${URLEncoder.encode(it.name, Charsets.US_ASCII).replace("+", "%20")}")
            }
        }
    }

    private fun findPuzzleDirMatches(repo: File, name: String): List<File> =
        File(repo, "gif").listFiles()?.sorted()?.flatMap { it.listFiles()?.asIterable()?.sorted() ?: emptyList() }?.filter { it.isDirectory }
            ?.filter { it.name.contains(name, ignoreCase = true) }?.toList() ?: emptyList()

    private fun File.getPuzzleName() = name.replace(Regex("[\\d_]+"), " ").trim()

    private fun File.getScore(): Score {
        return Regex("(?<category>[HW])_(?<hw>[\\d.]+)_(?<cycles>\\d+)_(?<cost>\\d+).*").matchEntire(name)!!.let {
            Score(linkedMapOf((if (it.groups["category"]!!.value == "H") HEIGHT else WIDTH) to it.groups["hw"]!!.value.toDouble(),
                CYCLES to it.groups["cycles"]!!.value.toDouble(),
                COST to it.groups["cost"]!!.value.toDouble()))
        }
    }
}