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

package com.faendir.zachtronics.bot.om.rest

import com.faendir.zachtronics.bot.om.rest.dto.*
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/** Integration with Opus Magnum proper */
@RestController
@RequestMapping("/om/game-api")
class OmGameApiController {
    /** game calls this every time a level is solved, we read the author from the basic auth header username */
    @PostMapping(path = ["/solved"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun solved(@RequestHeader(HttpHeaders.AUTHORIZATION) auth: String, @ModelAttribute solvedDTO: OmGameApiSolvedDTO) {
        userFromBasicAuth(auth)
        solvedDTO.solution.bytes
        // TODO completely ignore request, as we want gifs
    }

    /** game calls this every time a gif is recorded, we read the author from the basic auth header username */
    @PostMapping(path = ["/recorded"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun recorded(@RequestHeader(HttpHeaders.AUTHORIZATION) auth: String, @ModelAttribute recordedDTO: OmGameApiRecordedDTO) {
        userFromBasicAuth(auth)
        recordedDTO.solution.bytes
        recordedDTO.gif.bytes
        // TODO hook into submit
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun userFromBasicAuth(auth: String) =
        String(Base64.decode(auth.removePrefix("Basic "))).substringBefore(":")
            .ifEmpty { throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Empty author") }
}