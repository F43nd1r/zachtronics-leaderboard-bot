/*
 * Copyright (c) 2024
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

package com.faendir.zachtronics.bot.mors

import com.faendir.zachtronics.bot.config.MorsProperties
import com.google.common.hash.Hashing
import net.bramp.ffmpeg.FFmpeg
import net.bramp.ffmpeg.FFmpegExecutor
import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.builder.FFmpegBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.exchange
import java.io.File
import java.time.Duration

/**
 * Interface to Syx's gif storage CDN at https://files.mors.technology/$name
 */
@Service
class MorsService(private val morsProperties: MorsProperties, restTemplateBuilder: RestTemplateBuilder) {
    private val rootUrl = "https://${morsProperties.region}.storage.bunnycdn.com/${morsProperties.storageZone}"
    private val restTemplate = restTemplateBuilder
        .setReadTimeout(Duration.ofMinutes(5))
        .setConnectTimeout(Duration.ofMinutes(5))
        .build()

    private val ffmpeg = FFmpegExecutor(FFmpeg("/usr/bin/ffmpeg"), FFprobe("/usr/bin/ffprobe"))

    /**
     * The final url will be: https://files.mors.technology/$path/$name-$hash(0,8).mp4
     */
    fun uploadGif(gif: ByteArray, path: String, stem: String): String {
        upload(gif, path, stem, "gif")
        return upload(convertToVideo(gif), path, stem, "mp4")
    }

    fun upload(gif: ByteArray, path: String, stem: String, ext: String): String {
        val hash = Hashing.sha256().hashBytes(gif).toString()
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_OCTET_STREAM
        headers["AccessKey"] = morsProperties.apiKey
        headers["Checksum"] = hash
        val fullPath = "$path/$stem-${hash.substring(0, 8)}.$ext"
        logger.info("Uploading to Mors: $fullPath")
        val result = restTemplate.exchange<Void>("$rootUrl/$fullPath", HttpMethod.PUT, HttpEntity(gif, headers))
        when (result.statusCode) {
            HttpStatus.CREATED -> return "https://files.mors.technology/$fullPath"
            HttpStatus.BAD_REQUEST -> throw RuntimeException("The file was uploaded unsuccessfully.")
            HttpStatus.UNAUTHORIZED -> throw RuntimeException("Invalid AccessKey, region hostname, or file passed in a non raw binary format.")
            else -> throw RuntimeException("Unexpected return code: ${result.statusCode}")
        }
    }

    fun delete(fullPath: String): Boolean {
        val headers = HttpHeaders()
        headers["AccessKey"] = morsProperties.apiKey
        val result = restTemplate.exchange<Void>("$rootUrl/$fullPath", HttpMethod.DELETE, HttpEntity(null, headers))
        when (result.statusCode) {
            HttpStatus.OK -> return true
            HttpStatus.BAD_REQUEST -> throw RuntimeException("Object delete failed")
            else -> throw RuntimeException("Unexpected return code: ${result.statusCode}")
        }
    }

    private fun convertToVideo(gif: ByteArray): ByteArray {
        val input = File.createTempFile("input", ".gif")
        input.writeBytes(gif)
        val output = File.createTempFile("output", ".mp4")
        val builder = FFmpegBuilder()
            .setInput(input.canonicalPath)
            .overrideOutputFiles(true)
            .addOutput(output.canonicalPath)
            .setVideoMovFlags("faststart")
            .setVideoPixelFormat("yuv420p")
            .setVideoFilter("scale=trunc(iw/2)*2:trunc(ih/2)*2")
            .done()
        ffmpeg.createJob(builder).run()
        val result = output.readBytes()
        input.delete()
        output.delete()
        return result
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(MorsService::class.java)
    }
}