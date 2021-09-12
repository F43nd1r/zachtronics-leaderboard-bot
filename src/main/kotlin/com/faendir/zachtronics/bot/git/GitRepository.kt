/*
 * Copyright (c) 2021
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
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.Status
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import javax.annotation.PreDestroy

open class GitRepository(private val gitProperties: GitProperties, val name: String, val url: String, private val hasWebHook: Boolean = false) {
    companion object {
        private val logger = LoggerFactory.getLogger(GitRepository::class.java)
    }

    val rawFilesUrl = Regex("github.com/([^/]+)/([^/.]+)(?:.git)?").replaceFirst(url, "raw.githubusercontent.com/$1/$2/master/")
    private val repo = Files.createTempDirectory(name).toFile()
    private var remoteHash: String? = null

    init {
        synchronized(repo) {
            Git.cloneRepository().setURI(url).setDirectory(repo).call()
        }
    }

    fun <T> access(access: AccessScope.() -> T): T {
        return synchronized(repo) {
            Git.open(repo).use { git ->
                val accessScope = AccessScope(git, repo)
                if (!hasWebHook || accessScope.currentHash() != remoteHash) {
                    git.pull().setTimeout(120).call()
                    if (remoteHash == null) {
                        remoteHash = accessScope.currentHash()
                        logger.info("initial pull $name")
                    } else {
                        logger.info("pulled $name")
                    }
                } else {
                    logger.info("$name is up to date, not pulling")
                }
                accessScope.access()
            }
        }
    }

    fun updateRemoteHash(remoteHash: String) {
        synchronized(repo) {
            this.remoteHash = remoteHash
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

        fun currentHash(): String = git.repository.resolve("HEAD").name()

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