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

import org.springframework.stereotype.Service
import java.io.File
import java.util.concurrent.TimeUnit

@Service
class GifRecorderService(private val properties: GifMakerProperties) {

    fun createGif(solution: ByteArray, start: Int?, end: Int?): File? {
        val file = File.createTempFile("gifmaker-", ".solution")
        file.writeBytes(solution)
        val out = File(file.parent, file.nameWithoutExtension + ".gif")
        val process = ProcessBuilder(
            "${properties.omInstallationDir}/ModdedLightning.bin.x86_64",
            "out=${out.absolutePath}",
            *listOfNotNull(start?.let { "start=$it" }, end?.let { "end=$it" }).toTypedArray(),
            file.absolutePath
        )
            .inheritIO()
            .start()
        return if (try {
                process.waitFor(5, TimeUnit.MINUTES)
                process.exitValue() == 0
            } catch (e: InterruptedException) {
                process.destroyForcibly()
                false
            }
        ) {
            out
        } else {
            null
        }
    }
}