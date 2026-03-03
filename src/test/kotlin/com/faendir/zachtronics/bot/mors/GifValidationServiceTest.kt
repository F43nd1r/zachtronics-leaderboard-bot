/*
 * Copyright (c) 2025
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

import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource
import strikt.api.expectCatching
import strikt.api.expectThrows
import strikt.assertions.isSuccess
import strikt.assertions.message
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

internal class GifValidationServiceTest {

    private val gifValidationService = GifValidationService()

    @Test
    fun `valid game GIF passes validation`() {
        val gif = ClassPathResource("Face_Powder_Height_1.gif").inputStream.readAllBytes()
        expectCatching { gifValidationService.validate(gif) }.isSuccess()
    }

    @Test
    fun `GIF smaller than minimum size is rejected`() {
        val tinyGif = ByteArray(100)
        expectThrows<IllegalArgumentException> { gifValidationService.validate(tinyGif) }
            .message.equals("GIF is too small (100 bytes). Minimum size is 10240 bytes.")
    }

    @Test
    fun `GIF with wrong dimensions is rejected`() {
        val wrongDimensionsGif = createGif(100, 100)
        expectThrows<IllegalArgumentException> { gifValidationService.validate(wrongDimensionsGif) }
            .message.equals("GIF dimensions 100x100 do not match expected 826x647.")
    }

    @Test
    fun `GIF with wrong width is rejected`() {
        val wrongWidthGif = createGif(800, 647)
        expectThrows<IllegalArgumentException> { gifValidationService.validate(wrongWidthGif) }
            .message.equals("GIF dimensions 800x647 do not match expected 826x647.")
    }

    @Test
    fun `GIF with wrong height is rejected`() {
        val wrongHeightGif = createGif(826, 600)
        expectThrows<IllegalArgumentException> { gifValidationService.validate(wrongHeightGif) }
            .message.equals("GIF dimensions 826x600 do not match expected 826x647.")
    }

    /**
     * Creates a single-frame GIF with the given dimensions, large enough to pass the minimum size check.
     */
    private fun createGif(width: Int, height: Int): ByteArray {
        val baos = ByteArrayOutputStream()
        ImageIO.write(BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), "gif", baos)
        val raw = baos.toByteArray()
        return raw + ByteArray(10 * 1024)
    }
}
