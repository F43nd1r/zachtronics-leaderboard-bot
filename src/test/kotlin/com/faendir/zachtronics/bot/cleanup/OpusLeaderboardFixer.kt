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

package com.faendir.zachtronics.bot.cleanup

import com.faendir.zachtronics.bot.config.GitProperties
import com.faendir.zachtronics.bot.createGitRepositoryFrom
import com.faendir.zachtronics.bot.model.DisplayContext
import com.faendir.zachtronics.bot.om.createSubmission
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmRecord
import com.faendir.zachtronics.bot.om.model.OmSubmission
import com.faendir.zachtronics.bot.om.repository.OmSolutionRepository
import com.faendir.zachtronics.bot.om.rest.OmUrlMapper
import com.faendir.zachtronics.bot.testutils.TestGitRepository
import io.mockk.mockk
import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.jsonPrimitive
import org.eclipse.jgit.api.Git
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.io.path.readBytes
import kotlin.streams.asStream

/**
 * these fixes assume the opus magnum leaderboard repository is a sibling to this repository
 */
@Disabled
@OptIn(ExperimentalSerializationApi::class)
class OpusLeaderboardFixer {
    private val json = Json {
        prettyPrint = true
        allowSpecialFloatingPointValues = true
    }

    private fun getAllRecordFiles(root: File, vararg puzzles: OmPuzzle = OmPuzzle.values()): Sequence<File> =
        puzzles.asSequence()
            .onEach { println("Doing puzzle ${it.name}") }
            .map { root.resolve(it.group.name).resolve(it.name) }
            .filter(File::exists)
            .flatMap { it.listFiles { f -> f.isFile && f.extension == "json" }!!.sorted() }

    @Test
    fun resubmitAll() {
        val repo = File("../opus_magnum/om-leaderboard")

        val gitRepo = TestGitRepository(GitProperties().apply {
            accessToken = ""
            username = "zachtronics-leaderboard-bot"
        }, repo)
        val repository = OmSolutionRepository(gitRepo, mockk(relaxed = true), OmUrlMapper(), mockk(relaxed = true))

        for (puzzle in OmPuzzle.values()) {
            println("Doing ${puzzle.name}")
            for ((record, _) in repository.findCategoryHolders(puzzle, true)) {
                println("${record.score.toDisplayString(DisplayContext.fileName())} is being processed")
                val solution = repo.toPath().resolve(record.dataPath).readBytes()
                val submission = OmSubmission(
                    record.puzzle, record.score, "SomeGuy", record.displayLink, null, 0 to 0, solution
                )

                val result = repository.submit(submission)
                println(result)
            }
        }
    }

    @Test
    fun `it's manifolding time`() {
        val repo = File("../opus_magnum/om-leaderboard")
        val newRepoFile = File("../opus_magnum/om-leaderboard-new")

        val newRepo = createGitRepositoryFrom(newRepoFile, GitProperties().apply {
            accessToken = ""
            username = "zachtronics-leaderboard-bot"
        })
        val repository = OmSolutionRepository(newRepo, mockk(relaxed = true), OmUrlMapper(), mockk(relaxed = true))

        for (file in getAllRecordFiles(repo, OmPuzzle.STABILIZED_WATER)) {
            println("${file.name} is being processed")
            val oldRecord = json.parseToJsonElement(file.readText()) as JsonObject
            val displayLink = oldRecord["displayLink"]!!.jsonPrimitive.content
            val solution: File = file.resolveSibling(file.name.replace(".json", ".solution"))
            val bytes = solution.readBytes()
            val submission = try {
                createSubmission(displayLink, null, "Manifoldius", bytes)
            }
            catch (e: Exception) {
                e.printStackTrace()
                continue
            }
            val result = repository.submit(submission)
            println(result)
        }
    }

    @Test
    fun `fix file names`() {
        val root = File("../om-leaderboard")
        getAllRecordFiles(root).asStream().parallel()
            .forEach { file ->
                println("${file.name} is being processed")
                try {
                    val record = file.inputStream().buffered().use { stream -> json.decodeFromStream<OmRecord>(stream) }
                    val wantedFileName = "${record.score.toDisplayString(DisplayContext.fileName())}_${record.puzzle.name}"
                    val wantedFile = File(file.parentFile, "$wantedFileName.json")
                    if (file.nameWithoutExtension != wantedFileName) {
                        if (wantedFile.exists()) {
                            file.delete()
                            println("Deleted ${file.name}")
                            return@forEach
                        } else {
                            file.renameTo(wantedFile)
                            println("Renamed ${file.name} to ${wantedFile.name}")
                        }
                    }
                    val solutionFile = root.toPath().resolve(record.dataPath).toFile()
                    if (solutionFile.nameWithoutExtension != wantedFileName) {
                        val wantedSolutionFile = File(file.parentFile, "$wantedFileName.solution")
                        if (wantedSolutionFile.exists()) {
                            solutionFile.delete()
                            println("Deleted ${solutionFile.name}")
                            return@forEach
                        } else {
                            solutionFile.renameTo(wantedSolutionFile)
                            wantedFile.writeText(json.encodeToString(record.copy(dataPath = root.toPath().relativize(wantedSolutionFile.toPath()))))
                            println("Renamed ${solutionFile.name} to ${wantedSolutionFile.name}")
                        }
                    }
                } catch (e: Exception) {
                    if ("MIRACULOUS" !in e.message!!) {
                        println("warn: Failed to process ${file.name}: ${e.message}")
                    }
                }
            }
    }

    @Test
    fun `fix data links`() {
        val root = File("../om-leaderboard")
        val mapper = OmUrlMapper()
        val exec = Runtime.getRuntime().exec("git rev-parse --short HEAD", null, root)
        exec.waitFor()
        val commitHash = exec.inputStream.bufferedReader().readText().trim()
        getAllRecordFiles(root).asStream().parallel()
            .forEach { file ->
                println("${file.name} is being processed")
                try {
                    val record = file.inputStream().buffered().use { stream -> json.decodeFromStream<OmRecord>(stream) }
                    file.writeText(json.encodeToString(record.copy(dataLink = mapper.createShortUrl(commitHash, record.puzzle, record.score))))
                } catch (e: Exception) {
                    if ("MIRACULOUS" !in e.message!!) {
                        println("warn: Failed to process ${file.name}: ${e.message}")
                    }
                }
            }
    }

    @Test
    fun `fix last modified`() {
        val root = File("../om-leaderboard")
        val git = Git.open(root)
        getAllRecordFiles(root).asStream().parallel()
            .forEach { file ->
                println("${file.name} is being processed")
                try {
                    val record = file.inputStream().buffered().use { stream -> json.decodeFromStream<OmRecord>(stream) }
                    file.writeText(
                        json.encodeToString(
                            record.copy(
                                lastModified = git.log().addPath(file.relativeTo(root).path).call()
                                    .firstOrNull()?.commitTime?.let { Instant.fromEpochSeconds(it.toLong()) })
                        )
                    )
                } catch (e: Exception) {
                    println("warn: Failed to process ${file.name}: ${e.message}")
                }
            }
    }
}