package com.faendir.zachtronics.bot.git

import com.faendir.zachtronics.bot.config.GitProperties
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Score
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.Status
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import javax.annotation.PreDestroy

open class GitRepository(private val gitProperties: GitProperties, private val name: String, val url: String, private val hasWebHook: Boolean = false) {
    companion object {
        private val logger = LoggerFactory.getLogger(GitRepository::class.java)
    }

    val rawFilesUrl = Regex("github.com/([^/]+)/([^/.]+)(?:.git)?").replaceFirst(url, "raw.githubusercontent.com/$1/$2/master/")
    private val repo = Files.createTempDirectory(name).toFile()
    private var needsPull = true

    init {
        synchronized(repo) {
            Git.cloneRepository().setURI(url).setDirectory(repo).call()
        }
    }

    fun <T> access(access: AccessScope.() -> T): T  {
        return synchronized(repo) {
            Git.open(repo).use { git ->
                if (!hasWebHook || needsPull) {
                    git.pull().setTimeout(120).call()
                    needsPull = false
                    logger.info("pulled $name")
                } else {
                    logger.info("$name is up to date, not pulling")
                }
                AccessScope(git, repo).access()
            }
        }
    }

    fun invalidate() {
        synchronized(repo) {
            needsPull = true
        }
    }

    inner class AccessScope(private val git: Git, val repo: File) {
        fun add(file: File) {
            git.add().addFilepattern(file.relativeTo(repo).path).call()
        }

        /** git add -A $file */
        fun addAll(file: File) {
            val relPath = file.relativeTo(repo).path
            git.add().addFilepattern(relPath).call()
            git.add().setUpdate(true).addFilepattern(relPath).call()
        }

        fun rm(file: File) {
            git.rm().addFilepattern(file.relativeTo(repo).path).call()
        }

        fun status(): Status = git.status().call()

        fun currentHash() : String = git.repository.resolve("HEAD").name()

        fun commitAndPush(user: String?, puzzle: Puzzle, score: Score, updated: Collection<String>) {
            commitAndPush("${puzzle.displayName} ${score.toDisplayString()} $updated by ${user ?: "unknown"}")
        }

        fun commitAndPush(message: String) {
            commit(message)
            push()
        }

        fun commit(message: String) {
            git.commit()
                .setAuthor("zachtronics-leaderboard-bot", "zachtronics-leaderboard-bot@faendir.com")
                .setCommitter("zachtronics-leaderboard-bot", "zachtronics-leaderboard-bot@faendir.com")
                .setMessage("[BOT] $message")
                .call()
        }

        fun push() {
            git.push().setCredentialsProvider(UsernamePasswordCredentialsProvider(gitProperties.username, gitProperties.accessToken))
                .setTimeout(120).call()
        }

        fun resetAndClean(file: File) {
            git.reset().addPath(file.relativeTo(repo).path).call()
            git.clean().setForce(true).setPaths(setOf(file.relativeTo(repo).path)).call()
        }
    }

    @PreDestroy
    fun cleanup() {
        repo.deleteRecursively()
    }
}