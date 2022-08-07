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

package com.faendir.zachtronics.bot.om.rest

import com.faendir.zachtronics.bot.config.GifMakerProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Service
import java.net.SocketTimeoutException
import java.time.Duration

@Service
class GifMakerService(restTemplateBuilder: RestTemplateBuilder, private val gifMakerProperties: GifMakerProperties) {

    private val restTemplate = restTemplateBuilder
        .setReadTimeout(Duration.ofMinutes(10))
        .build()

    fun createGif(solution: ByteArray, start: Int, end: Int): String? {
        try {
            return restTemplate.postForObject(gifMakerProperties.serverUrl + "/creategif?start=$start&end=$end", solution, String::class.java)
        } catch (e : SocketTimeoutException) {
            throw RuntimeException("Timed out creating a gif for your solution. Please create and upload your own gif.", e)
        }
    }
}