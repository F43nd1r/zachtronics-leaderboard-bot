/*
 * Copyright (c) 2021
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

import com.faendir.zachtronics.bot.discord.Colors
import com.faendir.zachtronics.bot.model.DisplayContext
import com.faendir.zachtronics.bot.model.StringFormat
import com.faendir.zachtronics.bot.om.createSubmission
import com.faendir.zachtronics.bot.om.model.OmCategory
import com.faendir.zachtronics.bot.om.model.OmGroup
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmRecord
import com.faendir.zachtronics.bot.om.model.OmScore
import com.faendir.zachtronics.bot.om.repository.OmSolutionRepository
import com.faendir.zachtronics.bot.repository.CategoryRecord
import com.faendir.zachtronics.bot.repository.SubmitResult
import com.faendir.zachtronics.bot.utils.SafeEmbedMessageBuilder
import com.faendir.zachtronics.bot.utils.embedCategoryRecords
import com.faendir.zachtronics.bot.utils.filterIsInstance
import com.faendir.zachtronics.bot.utils.isValidLink
import com.faendir.zachtronics.bot.utils.orEmpty
import com.faendir.zachtronics.bot.utils.smartFormat
import com.faendir.zachtronics.bot.utils.toMetricsTree
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.entity.channel.MessageChannel
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/om")
class OmController(private val repository: OmSolutionRepository, private val discordClient: GatewayDiscordClient) {

    @GetMapping(path = ["/groups"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun listGroups(): List<OmGroupDTO> = OmGroup.values().map { it.toDTO() }

    @GetMapping(path = ["/puzzles"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun listPuzzles(): List<OmPuzzleDTO> = OmPuzzle.values().map { it.toDTO() }

    @GetMapping(path = ["/group/{groupId}/puzzles"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun listPuzzlesByGroup(@PathVariable groupId: String): List<OmPuzzleDTO> {
        val group = findGroup(groupId)
        return OmPuzzle.values().filter { it.group == group }.map { it.toDTO() }
    }

    @GetMapping(path = ["/categories"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun listCategories(): List<OmCategoryDTO> = OmCategory.values().map { it.toDTO() }

    @GetMapping(path = ["/puzzle/{puzzleId}/records"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun listRecords(@PathVariable puzzleId: String, @RequestParam(required = false) includeFrontier: Boolean?): List<OmRecordDTO> {
        val puzzle = findPuzzle(puzzleId)
        return repository.findCategoryHolders(puzzle, includeFrontier ?: false).map { it.toDTO() }
    }

    @GetMapping(path = ["/category/{categoryId}/records"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun listRecords(@PathVariable categoryId: String): List<OmRecordDTO> {
        val category = findCategory(categoryId)
        return repository.findAll(category).entries.sortedBy { it.key }.map { it.value?.withCategory(category)?.toDTO() ?: emptyRecord(it.key) }
    }

    @GetMapping(path = ["/puzzle/{puzzleId}/category/{categoryId}/record"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getRecord(@PathVariable puzzleId: String, @PathVariable categoryId: String): OmRecordDTO? {
        val puzzle = findPuzzle(puzzleId)
        val category = findCategory(categoryId)
        return repository.find(puzzle, category)?.withCategory(category)?.toDTO()
    }

    @PostMapping(path = ["/submit"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun submit(@ModelAttribute submissionDTO: OmSubmissionDTO): SubmitResultType {
        if (!isValidLink(submissionDTO.gif)) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid gif")
        val submission = createSubmission(submissionDTO.gif, submissionDTO.author, submissionDTO.solution.bytes)
        return when (val result = repository.submit(submission)) {
            is SubmitResult.Success -> {
                val beatenCategories: List<OmCategory> = result.beatenRecords.flatMap { it.categories }
                val categoryString = beatenCategories.takeIf { it.isNotEmpty() }?.joinToString { it.displayName } ?: "Pareto"
                val score = submission.score.toDisplayString(DisplayContext(StringFormat.DISCORD, beatenCategories))
                sendDiscordMessage(
                    SafeEmbedMessageBuilder()
                        .title("API submission: *${submission.puzzle.displayName}* $categoryString")
                        .color(Colors.SUCCESS)
                        .description("`$score`${submission.author.orEmpty(prefix = " by ")}${if (beatenCategories.isNotEmpty()) "\npreviously:" else " was included in the pareto frontier"}")
                        .embedCategoryRecords(result.beatenRecords, submission.puzzle.supportedCategories)
                        .link(submission.displayLink)
                )
                SubmitResultType.SUCCESS
            }
            is SubmitResult.Failure -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, result.message)
            is SubmitResult.NothingBeaten -> SubmitResultType.NOTHING_BEATEN
            is SubmitResult.AlreadyPresent -> SubmitResultType.ALREADY_PRESENT
        }
    }

    private fun sendDiscordMessage(message: SafeEmbedMessageBuilder) {
        discordClient.guilds
            .flatMap { it.channels }
            .filter { it.name == "opus-magnum" }
            .filterIsInstance<MessageChannel>()
            .flatMap { message.send(it) }
            .subscribe()
    }
}

private fun findPuzzle(puzzleId: String) = OmPuzzle.values().find { it.id.equals(puzzleId, ignoreCase = true) } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Puzzle $puzzleId not found.")

private fun findCategory(categoryId: String) = OmCategory.values().find { it.name.equals(categoryId, ignoreCase = true) } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Category $categoryId not found.")

private fun findGroup(groupId: String) = OmGroup.values().find { it.name.equals(groupId, ignoreCase = true) } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Group $groupId not found.")

data class OmGroupDTO(val id: String, val displayName: String)

fun OmGroup.toDTO() = OmGroupDTO(name, displayName)

data class OmPuzzleDTO(val id: String, val displayName: String, val group: OmGroupDTO)

fun OmPuzzle.toDTO() = OmPuzzleDTO(id, displayName, group.toDTO())

data class OmCategoryDTO(val id: String, val displayName: String, val metrics: List<String>)

fun OmCategory.toDTO() = OmCategoryDTO(name, displayName, metrics.map { metric -> metric.description })

data class OmScoreDTO(
    val cost: Int? = null,
    val cycles: Int? = null,
    val area: Int? = null,
    val instructions: Int? = null,
    val height: Int? = null,
    val width: Double? = null,
    val rate: Double? = null,
    val trackless: Boolean = false,
    val overlap: Boolean = false,
)

fun OmScore.toDTO() = OmScoreDTO(
    cost = cost,
    cycles = cycles,
    area = area,
    instructions = instructions,
    height = height,
    width = width,
    rate = rate,
    trackless = trackless,
    overlap = overlap
)

data class OmRecordDTO(
    val puzzle: OmPuzzleDTO,
    val score: OmScoreDTO?,
    val smartFormattedScore: String?,
    val fullFormattedScore: String?,
    val gif: String?,
    val solution: String?,
    val categoryIds: List<String>?,
    val smartFormattedCategories: String?
)

fun CategoryRecord<OmRecord, OmCategory>.toDTO() =
    OmRecordDTO(
        puzzle = record.puzzle.toDTO(),
        score = record.score.toDTO(),
        smartFormattedScore = record.score.toDisplayString(DisplayContext(StringFormat.PLAIN_TEXT, categories.toList())),
        fullFormattedScore = record.score.toDisplayString(DisplayContext.plainText()),
        gif = record.displayLink,
        solution = record.dataLink,
        categoryIds = categories.map { it.name },
        smartFormattedCategories = categories.smartFormat(record.puzzle.supportedCategories.toMetricsTree())
    )

fun emptyRecord(puzzle: OmPuzzle) = OmRecordDTO(
    puzzle = puzzle.toDTO(),
    score = null,
    smartFormattedScore = null,
    fullFormattedScore = null,
    gif = null,
    solution = null,
    categoryIds = null,
    smartFormattedCategories = null
)

fun OmRecord.withCategory(category: OmCategory) = CategoryRecord(this, setOf(category))

data class OmSubmissionDTO(val gif: String, val author: String, val solution: MultipartFile)

enum class SubmitResultType {
    ALREADY_PRESENT,
    NOTHING_BEATEN,
    SUCCESS
}