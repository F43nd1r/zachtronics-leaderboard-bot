package com.faendir.om.discord.leaderboards.git

import com.faendir.om.discord.categories.Category
import com.faendir.om.discord.categories.ScorePart.*
import com.faendir.om.discord.leaderboards.GetResult
import com.faendir.om.discord.leaderboards.Leaderboard
import com.faendir.om.discord.leaderboards.PuzzleResult
import com.faendir.om.discord.leaderboards.UpdateResult
import com.faendir.om.discord.utils.Score
import com.faendir.om.discord.utils.toScoreString
import com.roxstudio.utils.CUrl
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.springframework.stereotype.Component
import java.io.File
import java.net.URLEncoder
import java.nio.file.Files
import javax.annotation.PostConstruct

@Component
class GitLeaderboard(private val gitProperties: GitProperties) : Leaderboard<File> {
    private val repo = Files.createTempDirectory("om-leaderboard").toFile()

    @PostConstruct
    fun prepare() {
        synchronized(repo) {
            Git.cloneRepository()
                .setURI("https://github.com/F43nd1r/om-leaderboard.git")
                .setDirectory(repo)
                .call()
        }
    }

    private val widthCategory = Category.create("W", WIDTH, CYCLES, COST)
    private val heightCategory = Category.create("H", HEIGHT, CYCLES, COST)

    override val supportedCategories = listOf(heightCategory, widthCategory)

    override fun findPuzzle(puzzle: String): PuzzleResult<File> {
        val puzzleDirs = findPuzzleDirMatches(repo, puzzle.replace(Regex("\\s"), "_"))
        return when {
            puzzleDirs.isEmpty() -> PuzzleResult.NotFound()
            puzzleDirs.size > 1 -> PuzzleResult.Ambiguous(*puzzleDirs.map { it.getPuzzleName() }.toTypedArray())
            else -> PuzzleResult.Success(puzzleDirs.first())
        }
    }

    override fun update(user: String, puzzle: File, category: Category, score: Score, link: String): UpdateResult {
        synchronized(repo) {
            Git.open(repo).use { git ->
                git.pull().call()
                val prefix = category.name
                val existingFile = puzzle.listFiles()?.find { it.name.startsWith(prefix) }
                val existingScore = existingFile?.getScore()
                if (existingScore != null && !score.isBetterScoreThan(existingScore)) {
                    return UpdateResult.BetterExists(puzzle.getPuzzleName(), existingScore)
                }
                val file = File(puzzle, "${prefix}_${score.toScoreString("_", false)}.${link.substringAfterLast('.')}")
                try {
                    file.writeBytes(CUrl(link).exec())
                } catch (e: Exception) {
                    return UpdateResult.BrokenLink
                }
                if (file != existingFile) {
                    existingFile?.let { git.rm().addFilepattern(it.name).call() }
                }
                Runtime.getRuntime().exec("/bin/bash ./generate.sh", null, repo).waitFor()
                git.add().addFilepattern(".").call()
                if (git.status().call().run { added.isNotEmpty() }) {
                    git.commit()
                        .setAuthor("om-leaderboard-discord-bot", "om-leaderboard-discord-bot@faendir.com")
                        .setCommitter(
                            "om-leaderboard-discord-bot",
                            "om-leaderboard-discord-bot@faendir.com"
                        )
                        .setMessage("Automated update with solution for ${puzzle.getPuzzleName()} by $user")
                        .call()
                    git.push().setCredentialsProvider(
                        UsernamePasswordCredentialsProvider(
                            gitProperties.username,
                            gitProperties.accessToken
                        )
                    ).call()
                }
                return UpdateResult.Success(puzzle.getPuzzleName(), existingScore)
            }
        }
    }

    override fun get(puzzleDir: File, category: Category): GetResult {
        synchronized(repo) {
            Git.open(repo).use { git ->
                git.pull().call()
                val prefix = category.name
                val file = puzzleDir.listFiles()?.find { it.name.startsWith(prefix) }
                return when {
                    file == null -> GetResult.NoScore(puzzleDir.getPuzzleName())
                    file.name.endsWith(".block") -> GetResult.ScoreNotRecorded(
                        puzzleDir.getPuzzleName(),
                        "width scores for infinites are not recorded"
                    )
                    else -> GetResult.Success(
                        puzzleDir.getPuzzleName(),
                        file.getScore(), "https://f43nd1r.github.io/om-leaderboard/${
                            puzzleDir.relativeTo(repo).path
                        }/${URLEncoder.encode(file.name, Charsets.US_ASCII).replace("+", "%20")}"
                    )
                }
            }
        }
    }

    private fun findPuzzleDirMatches(repo: File, name: String): List<File> =
        File(repo, "gif").listFiles()?.sorted()?.flatMap { it.listFiles()?.asIterable()?.sorted() ?: emptyList() }
            ?.filter { it.isDirectory }?.filter { it.name.contains(name, ignoreCase = true) }?.toList() ?: emptyList()

    private fun File.getPuzzleName() = name.replace(Regex("[\\d_]+"), " ").trim()

    private fun File.getScore(): Score {
        return Regex("(?<category>[HW])_(?<hw>[\\d.]+)_(?<cycles>\\d+)_(?<cost>\\d+).*").matchEntire(name)!!.let {
            listOf(
                (if(it.groups["category"]!!.value == "H") HEIGHT else WIDTH) to it.groups["hw"]!!.value.toDouble(),
                CYCLES to it.groups["cycles"]!!.value.toDouble(),
                COST to it.groups["cost"]!!.value.toDouble()
            )
        }
    }

    private fun Score.isBetterScoreThan(other: Score): Boolean {
        forEach { part ->
            val otherValue = other.first { part.first == it.first }.second
            //better score
            if(part.second < otherValue) return true
            //worse score
            if(part.second > otherValue) return false
            //same score, check next component
        }
        //everything is the same
        return true
    }
}