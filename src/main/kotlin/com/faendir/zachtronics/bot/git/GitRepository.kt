/*
 * Copyright (c) 2023
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.faendir.zachtronics.bot.git

import com.faendir.zachtronics.bot.config.GitProperties
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Score
import com.google.common.util.concurrent.CycleDetectingLockFactory
import jakarta.annotation.PreDestroy
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand
import org.eclipse.jgit.api.Status
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.lib.Config
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectLoader
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevSort
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.revwalk.filter.CommitTimeRevFilter
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.eclipse.jgit.treewalk.TreeWalk
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.io.File
import java.nio.file.Files
import java.util.*
import java.util.concurrent.locks.Lock

open class GitRepository(private val gitProperties: GitProperties, val name: String, val url: String) {
    companion object {
        private val logger = LoggerFactory.getLogger(GitRepository::class.java)
    }

    val rawFilesUrl = Regex("github.com/([^/]+)/([^/.]+)(?:.git)?").replaceFirst(url, "raw.githubusercontent.com/$1/$2/master")
    private val repo = Files.createTempDirectory(name).toFile()
    private val git: Git
    private val lock = CycleDetectingLockFactory.newInstance(CycleDetectingLockFactory.Policies.WARN).newReentrantReadWriteLock(name)
    private val writeLock = lock.writeLock()
    private val readLock = lock.readLock()

    @Volatile
    private var remoteHash: String

    init {
        writeLock.lock()
        git = Git.cloneRepository().setURI(url).setDirectory(repo).call()
        remoteHash = git.repository.resolve("HEAD").name()
        writeLock.unlock()
    }

    private fun ensureUpToDate(): ReadAccess {
        var read: ReadAccess? = null
        val hash = try {
            read = ReadAccess(readLock, repo)
            read.currentHash()
        } catch (t: Throwable) {
            read?.close()
            throw t
        }
        if (hash == remoteHash) {
            logger.debug("$name is up to date, not pulling")
            return read
        }
        read.close()
        var write: ReadWriteAccess? = null
        try {
            write = ReadWriteAccess(writeLock, repo)
            git.pull().setTimeout(120).call()
        } catch (t: Throwable) {
            write?.close()
            throw t
        }
        logger.debug("pulled $name")
        return write
    }

    /**
     * needs to be closed to release repository access
     */
    fun acquireReadAccess() = ensureUpToDate()

    /**
     * needs to be closed to release repository access
     */
    fun acquireWriteAccess() = ensureUpToDate().let {
        if (it is ReadWriteAccess) {
            it
        } else {
            it.close()
            ReadWriteAccess(writeLock, repo)
        }
    }

    fun updateRemoteHash(remoteHash: String) {
        this.remoteHash = remoteHash
    }

    open inner class ReadAccess(private val lock: Lock, val repo: File) : Closeable {
        init {
            lock.lock()
        }

        fun status(): Status = git.status().call()

        fun currentHash(): String = git.repository.resolve("HEAD").name()

        fun shortCurrentHash(): String = git.repository.resolve("HEAD").abbreviate(7).name()

        override fun close() {
            lock.unlock()
        }

        fun changesSince(instant: Instant): List<Change> {
            return RevWalk(git.repository).use { walk ->
                val latestCommit = walk.parseCommit(git.repository.resolve(Constants.HEAD))
                walk.markStart(latestCommit)
                walk.sort(RevSort.COMMIT_TIME_DESC)
                walk.revFilter = CommitTimeRevFilter.after(Date.from(instant.toJavaInstant()))
                val firstCommit = walk.lastOrNull() ?: return@use emptyList()
                val beforeCommit: RevCommit? = git.repository.resolve(firstCommit.name + "^")?.let { walk.parseCommit(it) }
                DiffFormatter(null).use { diffFormatter ->
                    diffFormatter.setReader(walk.objectReader, Config())
                    diffFormatter.scan(beforeCommit?.tree, latestCommit.tree).map { diff ->
                        Change(
                            diff.changeType,
                            diff.oldPath,
                            diff.oldPath?.let { oldPath ->
                                beforeCommit?.tree?.let { oldTree ->
                                    TreeWalk.forPath(walk.objectReader, oldPath, oldTree)?.let {
                                        it.objectReader.open(it.getObjectId(0))
                                    }
                                }
                            },
                            diff.newPath,
                            diff.newPath?.let { newPath ->
                                TreeWalk.forPath(walk.objectReader, newPath, latestCommit.tree)?.let {
                                    it.objectReader.open(it.getObjectId(0))
                                }
                            },
                        )
                    }
                }
            }
        }

    }

    inner class ReadWriteAccess(lock: Lock, repo: File) : ReadAccess(lock, repo) {
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

        fun commitAndPush(user: String?, puzzle: Puzzle<*>, score: Score<*>, updated: Collection<String>) {
            commit(user, puzzle, score, updated)
            push()
        }

        fun commitAndPush(message: String) {
            commit(message)
            push()
        }

        fun commit(user: String?, puzzle: Puzzle<*>, score: Score<*>, updated: Collection<String>) {
            commit("${puzzle.displayName} ${score.toDisplayString()} $updated by ${user ?: "unknown"}")
        }

        fun commit(message: String): RevCommit =
            git.commit()
                .setAuthor("zachtronics-leaderboard-bot", "zachtronics-leaderboard-bot@faendir.com")
                .setCommitter("zachtronics-leaderboard-bot", "zachtronics-leaderboard-bot@faendir.com")
                .setMessage("[BOT] $message")
                .call()

        fun push() {
            git.push().setCredentialsProvider(UsernamePasswordCredentialsProvider(gitProperties.username, gitProperties.accessToken))
                .setTimeout(120).call()
        }

        fun resetAndClean(file: File) {
            git.reset().setMode(ResetCommand.ResetType.HARD).call()
            git.clean().setForce(true).setPaths(setOf(file.relativeTo(repo).path)).call()
        }
    }

    @PreDestroy
    open fun cleanup() {
        git.close()
        repo.deleteRecursively()
    }
}

data class Change(val type: DiffEntry.ChangeType, val oldName: String?, val oldContent: ObjectLoader?, val newName: String?, val newContent: ObjectLoader?)