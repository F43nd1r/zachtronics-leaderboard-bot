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

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isNotNull
import java.io.File

@Disabled
internal class ImgurServiceTest {

    private val imgurService = ImgurService(GifMakerProperties().apply { imgurClientId = "" })

    @Test
    fun upload() {
        val link = imgurService.upload(File(javaClass.classLoader.getResource("Face_Powder_Height_1.gif").file))

        expectThat(link).isNotNull()
        println(link)
    }
}