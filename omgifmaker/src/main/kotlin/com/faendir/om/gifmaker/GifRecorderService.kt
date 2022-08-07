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

package com.faendir.om.gifmaker

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File
import java.util.concurrent.TimeUnit

@Service
class GifRecorderService(private val properties: GifMakerProperties) {

    fun createGif(solution: ByteArray, start: Int?, end: Int?): File {
        val file = File.createTempFile("gifmaker-", ".solution")
        file.writeBytes(solution)
        val out = File(file.parent, file.nameWithoutExtension + ".gif")
        val process = ProcessBuilder(
            "${properties.omInstallationDir}/ModdedLightning.bin.x86_64",
            "out=${out.absolutePath}",
            *listOfNotNull(start?.let { "start=$it" }, end?.let { "end=$it" }).toTypedArray(),
            file.absolutePath
        ).start()
        if (process.waitFor(8, TimeUnit.MINUTES)) {
            if (process.exitValue() == 0) {
                if(out.exists() && out.length() != 0L) {
                    return out
                } else {
                    val output = String(process.inputStream.readAllBytes() + process.errorStream.readAllBytes())
                    throw RuntimeException("Game exited but gif was not generated: $output")
                }
            } else {
                val output = String(process.inputStream.readAllBytes() + process.errorStream.readAllBytes())
                logger.warn(output)
                throw RuntimeException("Game failed to generate gif: ${Regex("FATAL UNHANDLED EXCEPTION: ([^\n]*)").matchEntire(output)?.groupValues?.get(1) ?: output}")
            }
        } else {
            process.destroyForcibly()
            throw RuntimeException("Timed out waiting for game to generate a gif.")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GifRecorderService::class.java)
    }
}