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

package com.faendir.zachtronics.bot.cleanup

import com.faendir.zachtronics.bot.model.DisplayContext
import com.faendir.zachtronics.bot.om.model.OmRecord
import com.faendir.zachtronics.bot.om.rest.OmUrlMapper
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File

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

    @Test
    fun `fix file names`() {
        val root = File("../om-leaderboard")
        root.listFiles()!!
            .filter { it.isDirectory && it.name.contains("_") }
            .flatMap {
                println("Enter Directory ${it.name}")
                it.listFiles()!!.toList()
            }
            .filter { it.isDirectory }
            .flatMap {
                println("Enter Directory ${it.name}")
                it.listFiles()!!.toList()
            }
            .parallelStream()
            .filter { !it.isDirectory && it.extension == "json" }
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
                    if (record.dataPath != null) {
                        val solutionFile = root.toPath().resolve(record.dataPath!!).toFile()
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
        root.listFiles()!!
            .filter { it.isDirectory && it.name.contains("_") }
            .flatMap {
                println("Enter Directory ${it.name}")
                it.listFiles()!!.toList()
            }
            .filter { it.isDirectory }
            .flatMap {
                println("Enter Directory ${it.name}")
                it.listFiles()!!.toList()
            }
            .parallelStream()
            .filter { !it.isDirectory && it.extension == "json" }
            .forEach { file ->
                println("${file.name} is being processed")
                try {
                    val record = file.inputStream().buffered().use { stream -> json.decodeFromStream<OmRecord>(stream) }
                    if(record.dataPath != null) {
                        val wantedFileName = "${record.score.toDisplayString(DisplayContext.fileName())}_${record.puzzle.name}"
                        file.writeText(json.encodeToString(record.copy(dataLink = mapper.createShortUrl(commitHash, record.puzzle, wantedFileName))))
                    }
                } catch (e: Exception) {
                    if ("MIRACULOUS" !in e.message!!) {
                        println("warn: Failed to process ${file.name}: ${e.message}")
                    }
                }
            }
    }
}