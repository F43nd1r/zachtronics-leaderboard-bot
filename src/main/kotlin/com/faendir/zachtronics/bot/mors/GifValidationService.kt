/*
 * Copyright (c) 2026
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

import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.probe.FFmpegProbeResult
import net.bramp.ffmpeg.probe.FFmpegStream
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File

/**
 * Validates that uploaded GIFs match the Opus Magnum game recorder output.
 */
@Service
class GifValidationService(private val ffprobe: FFprobe = FFprobe("/usr/bin/ffprobe")) {

    companion object {
        private val logger = LoggerFactory.getLogger(GifValidationService::class.java)

        private const val MIN_GIF_SIZE = 10 * 1024   // 10 KB
        private const val MIN_DURATION_SECS = 0.5
        private const val EXPECTED_WIDTH = 826
        private const val EXPECTED_HEIGHT = 647
    }

    /**
     * Validates the GIF before upload:
     * - minimum file size (10 KB)
     * - minimum duration (0.5s)
     * - exact game recorder dimensions (826x647)
     */
    fun validate(gif: ByteArray) {
        if (gif.size < MIN_GIF_SIZE) {
            throw IllegalArgumentException("GIF is too small (${gif.size} bytes). Minimum size is $MIN_GIF_SIZE bytes.")
        }

        val probe = probe(gif)
        val format = probe.format ?: throw IllegalArgumentException("Could not read GIF metadata.")
        val stream = probe.streams?.firstOrNull { it.codec_type == FFmpegStream.CodecType.VIDEO } ?: throw IllegalArgumentException("GIF has no video stream.")

        if (format.duration < MIN_DURATION_SECS) {
            throw IllegalArgumentException("GIF duration is too short (${format.duration}s). Minimum is ${MIN_DURATION_SECS}s.")
        }
        if (stream.width != EXPECTED_WIDTH || stream.height != EXPECTED_HEIGHT) {
            throw IllegalArgumentException("GIF dimensions ${stream.width}x${stream.height} do not match expected ${EXPECTED_WIDTH}x${EXPECTED_HEIGHT}.")
        }
    }

    private fun probe(gif: ByteArray): FFmpegProbeResult {
        val tempFile = File.createTempFile("probe", ".gif")
        try {
            tempFile.writeBytes(gif)
            return ffprobe.probe(tempFile.absolutePath)
        } catch (e: Exception) {
            logger.warn("Failed to probe GIF", e)
            throw IllegalArgumentException("Could not read GIF metadata.", e)
        } finally {
            tempFile.delete()
        }
    }
}
