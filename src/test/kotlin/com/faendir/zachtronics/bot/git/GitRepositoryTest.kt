/*
 * Copyright (c) 2022
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
import com.faendir.zachtronics.bot.createGitRepositoryFrom
import com.faendir.zachtronics.bot.testutils.JGitNoExternalConfigReader
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import org.eclipse.jgit.diff.DiffEntry
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.first
import strikt.assertions.hasSize
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue
import java.io.File
import java.nio.file.Files

class GitRepositoryTest {
    init {
        JGitNoExternalConfigReader.install()
    }

    private val gitProperties = GitProperties().apply {
        accessToken = ""
        username = "zachtronics-bot-test"
    }
    private lateinit var gitRepository: GitRepository

    @BeforeEach
    internal fun setUp() {
        gitRepository = createGitRepositoryFrom(Files.createTempDirectory("repository").toFile(), gitProperties)
    }

    @AfterEach
    internal fun tearDown() {
        gitRepository.cleanup()
    }

    @Test
    fun `should return only changes since`() {
        gitRepository.acquireWriteAccess().use { access ->
            runBlocking {
                val file1 = File(access.repo, "file1")
                file1.writeText("file1")
                access.add(file1)
                access.commitAndPush("file1")
                delay(1000)
                val timestamp = Clock.System.now()
                delay(1000)
                val file2 = File(access.repo, "file2")
                file2.writeText("file2")
                access.add(file2)
                access.commitAndPush("file2")

                expectThat(access.changesSince(timestamp)).hasSize(1).and {
                    first().get { type }.isEqualTo(DiffEntry.ChangeType.ADD)
                    first().get { newName }.isEqualTo("file2")
                }
            }
        }
    }

    @Test
    fun `should return new content for added`() {
        gitRepository.acquireWriteAccess().use { access ->
            runBlocking {
                val timestamp = Clock.System.now()
                delay(1000)
                val file = File(access.repo, "file")
                file.writeText("file")
                access.add(file)
                access.commitAndPush("file")

                expectThat(access.changesSince(timestamp)).hasSize(1).and {
                    first().get { type }.isEqualTo(DiffEntry.ChangeType.ADD)
                    first().get { newName }.isEqualTo("file")
                    first().get { newContent!!.openStream().bufferedReader().use { it.readText() } }.isEqualTo("file")
                }
            }
        }
    }

    @Test
    fun `should return old content for deleted`() {
        gitRepository.acquireWriteAccess().use { access ->
            runBlocking {
                val file = File(access.repo, "file")
                file.writeText("file")
                access.add(file)
                access.commitAndPush("file")
                delay(1000)
                val timestamp = Clock.System.now()
                delay(1000)
                access.rm(file)
                access.commitAndPush("remove file")

                expectThat(access.changesSince(timestamp)).hasSize(1).and {
                    first().get { type }.isEqualTo(DiffEntry.ChangeType.DELETE)
                    first().get { oldName }.isEqualTo("file")
                    first().get { oldContent!!.openStream().bufferedReader().use { it.readText() } }.isEqualTo("file")
                }
            }
        }
    }


    @Test
    fun `should return empty if repo has no commits`() {
        gitRepository.acquireReadAccess().use { access ->
            expectThat(access.changesSince(Clock.System.now())).isEmpty()
        }
    }

    @Test
    fun `should allow concurrent read locks`() {
        gitRepository.acquireReadAccess().use {
            val thread = Thread {
                gitRepository.acquireReadAccess().close()
            }
            thread.start()
            thread.join(1000)
            expectThat(thread.isAlive).isFalse()
        }
    }

    @Test
    fun `should not allow read lock during write`() {
        gitRepository.acquireWriteAccess().use {
            val thread = Thread {
                gitRepository.acquireReadAccess().close()
            }
            thread.start()
            thread.join(1000)
            expectThat(thread.isAlive).isTrue()
            thread.interrupt()
        }
    }

    @Test
    fun `should not allow additional write lock during write`() {
        gitRepository.acquireWriteAccess().use {
            val thread = Thread {
                gitRepository.acquireWriteAccess().close()
            }
            thread.start()
            thread.join(1000)
            expectThat(thread.isAlive).isTrue()
            thread.interrupt()
        }
    }
}