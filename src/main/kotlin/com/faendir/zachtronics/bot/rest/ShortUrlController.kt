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

package com.faendir.zachtronics.bot.rest

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.HandlerMapping
import javax.servlet.http.HttpServletRequest

@CrossOrigin
@RestController
@RequestMapping("/l")
class ShortUrlController(urlMappers: List<UrlMapper>) {
    private val mappers = urlMappers.associateBy { it.pathId }

    @GetMapping(path = ["/{mapperId}/**"])
    fun proxyUrl(@PathVariable mapperId: String, request: HttpServletRequest): ResponseEntity<Unit> {
        val path = (request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE) as String).removePrefix("/l/$mapperId/")
        val mapper = mappers[mapperId] ?: return ResponseEntity.badRequest().build()
        val longUrl = mapper.map(path) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY).header("Location", longUrl).build()
    }
}

interface UrlMapper {
    val pathId: String
    fun map(shortUrl: String): String?

    fun buildUrl(path: String) = "https://zlbb.faendir.com/l/${pathId}/${path.removePrefix("/")}"
}