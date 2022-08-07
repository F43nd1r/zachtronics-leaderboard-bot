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
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import java.io.File

@Service
class ImgurService(private val gifMakerProperties: GifMakerProperties) {

    private val restTemplate = RestTemplate()

    private fun login(): String {
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA
        headers["User-Agent"] = "Mozilla/5.0 (X11; Linux x86_64; rv:103.0) Gecko/20100101 Firefox/103.0"

        val body = LinkedMultiValueMap<String, Any>()
        body.add("username", gifMakerProperties.imgurUsername)
        body.add("password", gifMakerProperties.imgurPassword)
        body.add("remember", "remember")
        body.add("submit", "")
        val response = restTemplate.exchange("https://imgur.com/signin", HttpMethod.POST, HttpEntity(body, headers), String::class.java)

        return response.headers["set-cookie"]
            ?.find { it.startsWith("accesstoken") }
            ?.let {cookie ->
                Regex("accesstoken=([^;]*);.*").matchEntire(cookie)?.groupValues?.get(1)
            }
            ?: throw RuntimeException("Imgur login failed")
    }

    fun upload(file: File): String {
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA
        headers["User-Agent"] = "Mozilla/5.0 (X11; Linux x86_64; rv:103.0) Gecko/20100101 Firefox/103.0"
        headers["Authorization"] = "Bearer ${login()}"
        val body = LinkedMultiValueMap<String, Any>()
        body.add("image", FileSystemResource(file))

        try {
            val result = restTemplate.exchange("https://api.imgur.com/3/image", HttpMethod.POST, HttpEntity(body, headers), Response::class.java)
            if (result.statusCode == HttpStatus.OK) {
                val link = result.body?.data?.link
                if (link != null) {
                    return if (link.endsWith(".")) link + "mp4"
                    else link
                } else {
                    throw RuntimeException("Imgur did not return a link: ${result.body}")
                }
            } else {
                throw RuntimeException("Failed to upload gif to imgur: ${result.body}")
            }
        } catch (e: HttpStatusCodeException) {
            logger.warn("Imgur returned error.\nHeaders:\n${e.responseHeaders?.toList()?.joinToString { "${it.first}=${it.second.joinToString()}" }}\n", e)
            throw RuntimeException("Imgur returned error: ${e.message}")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ImgurService::class.java)
    }
}

@Serializable
private data class Response(val data: Data?)

@Serializable
private data class Data(val link: String)