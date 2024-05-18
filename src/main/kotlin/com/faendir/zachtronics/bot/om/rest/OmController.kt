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

import com.faendir.zachtronics.bot.mors.MorsService
import com.faendir.zachtronics.bot.om.model.OmCategory
import com.faendir.zachtronics.bot.om.model.OmCollection
import com.faendir.zachtronics.bot.om.model.OmGroup
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmScoreManifold
import com.faendir.zachtronics.bot.om.model.OmSubmission
import com.faendir.zachtronics.bot.om.notifyOf
import com.faendir.zachtronics.bot.om.repository.OmSolutionRepository
import com.faendir.zachtronics.bot.om.rest.dto.OmCategoryDTO
import com.faendir.zachtronics.bot.om.rest.dto.OmCollectionDTO
import com.faendir.zachtronics.bot.om.rest.dto.OmGroupDTO
import com.faendir.zachtronics.bot.om.rest.dto.OmPuzzleDTO
import com.faendir.zachtronics.bot.om.rest.dto.OmRecordChangeDTO
import com.faendir.zachtronics.bot.om.rest.dto.OmRecordDTO
import com.faendir.zachtronics.bot.om.rest.dto.OmScoreManifoldDTO
import com.faendir.zachtronics.bot.om.rest.dto.OmSubmissionDTO
import com.faendir.zachtronics.bot.om.rest.dto.emptyRecord
import com.faendir.zachtronics.bot.om.rest.dto.id
import com.faendir.zachtronics.bot.om.rest.dto.toDTO
import com.faendir.zachtronics.bot.om.validation.createSubmission
import com.faendir.zachtronics.bot.om.withCategory
import com.faendir.zachtronics.bot.repository.SubmitResult
import com.faendir.zachtronics.bot.rest.GameRestController
import com.faendir.zachtronics.bot.rest.dto.SubmitResultType
import com.faendir.zachtronics.bot.utils.isValidLink
import com.google.common.hash.Hashing
import discord4j.core.GatewayDiscordClient
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.toKotlinInstant
import org.slf4j.LoggerFactory
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.io.path.readBytes
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.minutes

@RestController
@RequestMapping("/om")
class OmController(
    private val repository: OmSolutionRepository,
    private val morsService: MorsService,
    private val discordClient: GatewayDiscordClient
) : GameRestController<OmGroupDTO, OmPuzzleDTO, OmCategoryDTO, OmRecordDTO> {
    companion object {
        private val logger = LoggerFactory.getLogger(OmController::class.java)
    }

    private val gameUploadScope = CoroutineScope(Dispatchers.IO)

    @PreDestroy
    fun shutdown() {
        gameUploadScope.cancel()
    }

    override val groups: List<OmGroupDTO> = OmGroup.entries.map { it.toDTO() }

    @get:GetMapping("/collections", produces = [MediaType.APPLICATION_JSON_VALUE])
    val collections: List<OmCollectionDTO> = OmCollection.entries.map { it.toDTO() }

    @GetMapping(path = ["/collection/{collectionId}/groups"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getGroupsByCollection(@PathVariable collectionId: String): List<OmGroupDTO> {
        val collection = findCollection(collectionId)
        return OmGroup.entries.filter { it.collection == collection }.map { it.toDTO() }
    }

    override val puzzles: List<OmPuzzleDTO> = OmPuzzle.entries.map { it.toDTO() }

    override fun getPuzzle(puzzleId: String): OmPuzzleDTO = findPuzzle(puzzleId).toDTO()

    @GetMapping("/puzzle/{puzzleId}/file")
    fun getPuzzleFile(@PathVariable puzzleId: String): ByteArray = findPuzzle(puzzleId).file.readBytes()

    override fun listPuzzlesByGroup(groupId: String): List<OmPuzzleDTO> {
        val group = findGroup(groupId)
        return OmPuzzle.entries.filter { it.group == group }.map { it.toDTO() }
    }

    override val categories: List<OmCategoryDTO> = OmCategory.entries.map { it.toDTO() }

    override fun getCategory(categoryId: String): OmCategoryDTO = findCategory(categoryId).toDTO()

    @get:GetMapping("/manifolds", produces = [MediaType.APPLICATION_JSON_VALUE])
    val manifolds: List<OmScoreManifoldDTO> = OmScoreManifold.entries.map { it.toDTO() }

    @GetMapping(path = ["/manifold/{manifoldId}/categories"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getCategoriesByManifold(@PathVariable manifoldId: String): List<OmCategoryDTO> {
        val manifold = findManifold(manifoldId)
        return OmCategory.entries.filter { it.manifold == manifold }.map { it.toDTO() }
    }

    override fun listRecords(puzzleId: String, includeFrontier: Boolean?): List<OmRecordDTO> {
        val puzzle = findPuzzle(puzzleId)
        return repository.findCategoryHolders(puzzle, includeFrontier ?: false).map { it.toDTO() }
    }

    @GetMapping(path = ["/category/{categoryId}/records"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun listRecords(@PathVariable categoryId: String): List<OmRecordDTO> {
        val category = findCategory(categoryId)
        return repository.findAll(category).entries.sortedBy { it.key }.map { it.value?.withCategory(category)?.toDTO() ?: emptyRecord(it.key) }
    }

    override fun getRecord(puzzleId: String, categoryId: String): OmRecordDTO? {
        val puzzle = findPuzzle(puzzleId)
        val category = findCategory(categoryId)
        return repository.find(puzzle, category)?.withCategory(category)?.toDTO()
    }

    @GetMapping("puzzle/{puzzleId}/record/{recordId}/file")
    fun getRecordFile(@PathVariable puzzleId: String, @PathVariable recordId: String): ByteArray =
        repository.findCategoryHolders(findPuzzle(puzzleId), includeFrontier = true)
            .find { recordId == it.record.id }
            ?.record?.dataPath?.readBytes()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Record $recordId not found.")

    @PostMapping(path = ["/submit"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun submit(@ModelAttribute submissionDTO: OmSubmissionDTO): SubmitResultType {
        if (submissionDTO.gif != null && !isValidLink(submissionDTO.gif)) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid gif")
        if (submissionDTO.gif == null && submissionDTO.gifData == null) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "no gif")
        val submission = createSubmission(submissionDTO.gif, submissionDTO.author, submissionDTO.solution.bytes)
        return runBlocking { doSubmit(submission, submissionDTO.gifData?.bytes, setOf(SubmitResult.Success::class, SubmitResult.Updated::class)) }
    }

    /** game calls this every time a level is solved */
    @PostMapping(path = ["/game-api/{user}/solved"], consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun solved(@PathVariable user: String, @RequestParam solution: ByteArray): String {
        return "This leaderboard only accepts submissions with gifs."
    }

    /** game calls this every time a gif is recorded */
    @OptIn(ExperimentalEncodingApi::class)
    @PostMapping(path = ["/game-api/{user}/recorded"], consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun recorded(@PathVariable user: String, @RequestParam solution: ByteArray, @RequestParam gif: ByteArray): String {
        gameUploadScope.launch {
            try {
                withTimeout(15.minutes) {
                    val submission = createSubmission(null, user, Base64.decode(solution))
                    val result = doSubmit(submission, Base64.decode(gif), setOf(SubmitResult.Success::class))
                    logger.info("Received game api submission for ${submission.puzzle.name} from $user. Result: $result")
                }
            } catch (e: Exception) {
                logger.warn("Bad game api submission from $user", e)
            }
        }
        return "Thank you for your submission. It is being processed asynchronously."
    }

    private suspend fun doSubmit(
        submission: OmSubmission, gifData: ByteArray?, allowedResults: Collection<KClass<out SubmitResult<*, *>>>
    ): SubmitResultType {

        val result = if (submission.displayLink == null) {
            val dryRunResult = repository.submitDryRun(submission)
            if (dryRunResult::class in allowedResults) {
                if (gifData == null) {
                    throw IllegalArgumentException("Failed to generate gif for your solution.")
                }
                val (gif, video) = morsService.uploadGif(
                    gifData,
                    submission.puzzle.name,
                    "${submission.score.toMorsString()}-${Hashing.sha256().hashBytes(submission.data).toString().substring(0, 8)}"
                )
                submission.displayLinkEmbed = gif
                submission.displayLink = video
                repository.submit(submission)
            } else {
                dryRunResult
            }
        } else repository.submit(submission)

        discordClient.notifyOf(result)

        return if (result::class in allowedResults) SubmitResultType.SUCCESS
        else when (result) {
            is SubmitResult.Failure -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, result.message)
            is SubmitResult.NothingBeaten -> SubmitResultType.NOTHING_BEATEN
            else -> SubmitResultType.ALREADY_PRESENT
        }
    }

    @GetMapping(path = ["/records/changes/{since}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getRecordChanges(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) since: java.time.Instant): List<OmRecordChangeDTO> {
        return repository.computeChangesSince(since.toKotlinInstant()).map { it.toDTO() }
    }


    @GetMapping(path = ["/records/new/{since}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getNewRecords(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) since: java.time.Instant): List<OmRecordDTO> {
        val kSince = since.toKotlinInstant()
        return repository.records.filter { it.record.lastModified != null && it.record.lastModified >= kSince }
            .sortedBy { it.record.lastModified }
            .map { it.toDTO() }
    }
}

private fun findPuzzle(puzzleId: String) =
    OmPuzzle.entries.find { it.id.equals(puzzleId, ignoreCase = true) } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Puzzle $puzzleId not found.")

private fun findCategory(categoryId: String) = OmCategory.entries.find { it.name.equals(categoryId, ignoreCase = true) } ?: throw ResponseStatusException(
    HttpStatus.NOT_FOUND,
    "Category $categoryId not found."
)

private fun findGroup(groupId: String) =
    OmGroup.entries.find { it.name.equals(groupId, ignoreCase = true) } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Group $groupId not found.")

private fun findCollection(collectionId: String) =
    OmCollection.entries.find { it.name.equals(collectionId, ignoreCase = true) } ?: throw ResponseStatusException(
        HttpStatus.NOT_FOUND,
        "Collection $collectionId not found."
    )

private fun findManifold(manifoldId: String) =
    OmScoreManifold.entries.find { it.name.equals(manifoldId, ignoreCase = true) } ?: throw ResponseStatusException(
        HttpStatus.NOT_FOUND,
        "Manifold $manifoldId not found."
    )
