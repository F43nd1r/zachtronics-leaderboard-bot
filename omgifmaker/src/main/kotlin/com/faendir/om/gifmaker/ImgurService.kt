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

import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import java.io.File

@Service
class ImgurService(private val gifMakerProperties: GifMakerProperties) {

    private val restTemplate = RestTemplate()

    fun upload(file: File): String? {
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA
        headers["Authorization"] = "Client-ID ${gifMakerProperties.imgurClientId}"

        val body = LinkedMultiValueMap<String, Any>()
        body.add("image", FileSystemResource(file))

        val result = restTemplate.exchange("https://api.imgur.com/3/upload", HttpMethod.POST, HttpEntity(body, headers), Response::class.java)
        if (result.statusCode == HttpStatus.OK) {
            val link = result.body?.data?.link
            if (link != null) {
                return if (link.endsWith(".")) link + "mp4"
                else link
            } else {
                logger.warn("Imgur did not return a link: ${result.body}")
            }
        } else {
            logger.warn("Failed to upload video to imgur: ${result.body}")
        }
        return null
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ImgurService::class.java)
    }
}

@Serializable
private data class Response(val data: Data?)

@Serializable
private data class Data(val link: String)