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


import com.faendir.zachtronics.bot.rest.dto.RecordDTO
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

interface GameRestController<GroupDTO, PuzzleDTO, CategoryDTO, RecDTO: RecordDTO<*>> {
    @get:GetMapping(path = ["/groups"], produces = [MediaType.APPLICATION_JSON_VALUE])
    val groups: List<GroupDTO>

    @get:GetMapping(path = ["/puzzles"], produces = [MediaType.APPLICATION_JSON_VALUE])
    val puzzles: List<PuzzleDTO>

    @get:GetMapping(path = ["/categories"], produces = [MediaType.APPLICATION_JSON_VALUE])
    val categories: List<CategoryDTO>

    @GetMapping(path = ["/puzzle/{puzzleId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getPuzzle(@PathVariable puzzleId: String): PuzzleDTO

    @GetMapping(path = ["/group/{groupId}/puzzles"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun listPuzzlesByGroup(@PathVariable groupId: String): List<PuzzleDTO>

    @GetMapping(path = ["/category/{categoryId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getCategory(@PathVariable categoryId: String): CategoryDTO

    @GetMapping(path = ["/puzzle/{puzzleId}/records"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun listRecords(@PathVariable puzzleId: String, @RequestParam(required = false) includeFrontier: Boolean?): List<RecDTO>

    @GetMapping(path = ["/puzzle/{puzzleId}/category/{categoryId}/record"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getRecord(@PathVariable puzzleId: String, @PathVariable categoryId: String): RecDTO?
}