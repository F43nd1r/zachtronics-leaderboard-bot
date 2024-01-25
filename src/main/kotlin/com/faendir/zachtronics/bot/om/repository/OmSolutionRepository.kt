/*
 * Copyright (c) 2023
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

package com.faendir.zachtronics.bot.om.repository

import com.faendir.zachtronics.bot.git.GitRepository
import com.faendir.zachtronics.bot.model.DisplayContext
import com.faendir.zachtronics.bot.om.model.*
import com.faendir.zachtronics.bot.om.rest.OmUrlMapper
import com.faendir.zachtronics.bot.repository.CategoryRecord
import com.faendir.zachtronics.bot.repository.SolutionRepository
import com.faendir.zachtronics.bot.repository.SubmitResult
import jakarta.annotation.PostConstruct
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.eclipse.jgit.diff.DiffEntry
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.io.File
import java.util.*

@OptIn(ExperimentalSerializationApi::class)
@Component
class OmSolutionRepository(
    @Qualifier("omLeaderboardRepository") private val leaderboard: GitRepository,
    private val pageGenerator: OmRedditWikiGenerator,
    private val omUrlMapper: OmUrlMapper,
) : SolutionRepository<OmCategory, OmPuzzle, OmSubmission, OmRecord> {
    private val json = Json {
        prettyPrint = true
        allowSpecialFloatingPointValues = true
    }
    private val memoryRecordOrder = Comparator.comparing({ r: OmMemoryRecord -> r.record.score },
        (listOf(OmMetric.OVERLAP) + OmMetrics.VALUE) // overlap scores last, trackless in the mix
            .map { it.comparator }
            .reduce(Comparator<OmScore>::thenComparing))
    private lateinit var data: Map<OmPuzzle, SortedSet<OmMemoryRecord>>
    private var hash: String? = null

    @PostConstruct
    fun init() {
        leaderboard.acquireReadAccess().use { leaderboardScope ->
            loadData(leaderboardScope)
            pageGenerator.update(leaderboardScope, OmCategory.entries, data)
        }
    }

    private fun loadData(leaderboardScope: GitRepository.ReadAccess) {
        data = OmPuzzle.entries.associateWith { sortedSetOf(memoryRecordOrder) }
        for ((puzzle, memoryRecords) in data.entries) {
            // fill map
            leaderboardScope.getPuzzleDir(puzzle).takeIf { it.exists() }
                ?.listFiles { file -> file.extension == "json" }
                ?.map { file ->
                    file.inputStream().buffered().use { json.decodeFromStream<OmRecord>(it) }
                }
                ?.map { it.toMemoryRecord(leaderboardScope.repo.toPath()) }
                ?.forEach(memoryRecords::add)

            // fill valid manifolds
            for (mRecord in memoryRecords) {
                manifolds@ for (manifold in OmScoreManifold.entries) {
                    for (otherMRecord in memoryRecords) {
                        val compares = manifold.frontierCompare(mRecord.record.score, otherMRecord.record.score)
                        if (compares.all { it >= 0 } && compares.any { it > 0 })
                            continue@manifolds
                    }
                    mRecord.frontierManifolds.add(manifold)
                }
            }

            // fill cats
            if (memoryRecords.isNotEmpty()) {
                for (category in OmCategory.entries.filter { it.supportsPuzzle(puzzle) }) {
                    memoryRecords
                        .filter { category.supportsScore(it.record.score) }
                        .minWithOrNull(Comparator.comparing({ it.record.score }, category.scoreComparator))
                        ?.categories
                        ?.add(category)
                }
            }
        }
    }

    private fun loadDataIfNecessary(leaderboardScope: GitRepository.ReadAccess) {
        val currentHash = leaderboardScope.currentHash()
        if (hash != currentHash) {
            loadData(leaderboardScope)
            hash = currentHash
        }
    }

    override fun submit(submission: OmSubmission): SubmitResult<OmRecord, OmCategory> {
        if (submission.displayLink == null) {
            throw IllegalArgumentException("Missing gif link.")
        }
        if (submission.displayLink!!.endsWith(".solution")) {
            throw IllegalArgumentException("You cannot use solution files as gifs.")
        }
        return leaderboard.acquireWriteAccess().use { leaderboardScope ->
            val records by lazy { data.getValue(submission.puzzle) }
            val newMRecord by lazy { submission.createMRecord(leaderboardScope) }
            val result = submit(leaderboardScope, submission) { beatenMRecord, beatenCategories, lostManifolds ->
                if (beatenMRecord != null) {
                    beatenMRecord.frontierManifolds -= lostManifolds
                    if (beatenMRecord.frontierManifolds.isNotEmpty()) {
                        beatenMRecord.categories -= beatenCategories
                    } else {
                        beatenMRecord.remove(leaderboardScope)
                    }
                }
                newMRecord.frontierManifolds += lostManifolds
                newMRecord.categories += beatenCategories
                records.add(newMRecord)
            }
            val beatenRecords = when (result) {
                is SubmitResult.Success -> result.beatenRecords
                is SubmitResult.Updated -> listOf(result.oldRecord)
                else -> null
            }
            if (beatenRecords != null) {
                pageGenerator.update(leaderboardScope, beatenRecords.flatMap { it.categories }, data)
                leaderboardScope.commitAndPush(
                    submission.author,
                    submission.puzzle,
                    submission.score,
                    beatenRecords.flatMap { it.categories }.map { it.toString() })
                hash = leaderboardScope.currentHash()
            }
            fun patchedUpRecord() = // add transient fields from submission
                newMRecord.record.copy(author = submission.author, displayLinkEmbed = submission.displayLinkEmbed)
            when (result) {
                is SubmitResult.Success -> result.copy(record = patchedUpRecord())
                is SubmitResult.Updated -> result.copy(record = patchedUpRecord())
                else -> result
            }
        }
    }

    fun submitDryRun(submission: OmSubmission): SubmitResult<OmRecord, OmCategory> {
        return leaderboard.acquireReadAccess().use { leaderboardScope -> submit(leaderboardScope, submission) { _, _, _ -> } }
    }

    private fun submit(
        leaderboardScope: GitRepository.ReadAccess,
        submission: OmSubmission,
        handleBeatenRecord: (beatenMRecord: OmMemoryRecord?, beatenCategories: Set<OmCategory>, lostManifolds: Set<OmScoreManifold>) -> Unit
    ): SubmitResult<OmRecord, OmCategory> {
        loadDataIfNecessary(leaderboardScope)
        val mRecords = data.getValue(submission.puzzle)
        val unclaimedCategories = OmCategory.entries.filter { it.supportsPuzzle(submission.puzzle) && it.supportsScore(submission.score) }.toMutableSet()
        val possibleManifolds = submission.score.manifolds.toMutableSet()
        val beatingWitnesses = mutableMapOf<OmScoreManifold, OmMemoryRecord>()
        val beatenCR = mutableSetOf<CategoryRecord<OmRecord?, OmCategory>>()
        for (mRecord in mRecords.toSet()) {
            val record = mRecord.record
            if (submission.score == record.score) {
                @Suppress("LiftReturnOrAssignment")
                if (submission.displayLink != record.displayLink || record.displayLink == null) {
                    // copies are needed or they'll edit themselves in the handler
                    handleBeatenRecord(mRecord, mRecord.categories.toSet(), mRecord.frontierManifolds.toSet())
                    return SubmitResult.Updated(null, mRecord.toCategoryRecord())
                } else {
                    return SubmitResult.AlreadyPresent()
                }
            }

            unclaimedCategories -= mRecord.categories
            // in case the candidate beats the old record all around, use that and skip the details
            if (OmMetrics.FULL_SCORE.all { it.comparator.compare(submission.score, record.score) <= 0 }) {
                // copies are needed or they'll edit themselves in the handler
                handleBeatenRecord(mRecord, mRecord.categories.toSet(), mRecord.frontierManifolds.toSet())
                beatenCR.add(CategoryRecord(record, mRecord.categories))
                continue
            }

            for (manifold in possibleManifolds.intersect(mRecord.frontierManifolds)) {
                val compares: List<Int> = manifold.frontierCompare(submission.score, record.score)
                /* If we let the identical case just go below, we allow overlapping-domino edit wars
                 * where 2 solves that are both paretos in manifold 1 but identical in manifold 2
                 * can keep beating each other.
                 * Therefore we leave the manifold as valid, but we add a beating witness
                 * If a submission's possible manifolds all have a beating witness, the submission is rejected.
                 * This isn't symmetrical, as the incoming solution is at a disadvantage wrt
                 * the ones in the leaderboard, but finding a minimal graph covering is out of my abilities.
                 */
                val identical = compares.all { it == 0 } // subscores identical
                val strictlyWorse = !identical && compares.all { it >= 0 } // candidate loses

                if (strictlyWorse || identical) {
                    if (strictlyWorse) {
                        possibleManifolds.remove(manifold)
                    }
                    beatingWitnesses[manifold] = mRecord
                    if (beatingWitnesses.keys.containsAll(possibleManifolds))
                        return SubmitResult.NothingBeaten(
                            beatingWitnesses.values.distinctBy { it.record.score }.map { it.toCategoryRecord() })
                }

                if (!strictlyWorse) {
                    val beatenCategories = mRecord.categories.filter { category ->
                        category.associatedManifold == manifold && category.supportsScore(submission.score) &&
                                category.scoreComparator.compare(submission.score, record.score)
                                    .let {
                                        it < 0 || it == 0 && submission.displayLink != record.displayLink
                                    }
                    }.toSet()

                    val strictlyBetter = !identical && compares.all { it <= 0 } // exactly equal is taken by the branch above
                    if (strictlyBetter || beatenCategories.isNotEmpty()) {
                        val lostManifolds = if (strictlyBetter) setOf(manifold) else emptySet()
                        handleBeatenRecord(mRecord, beatenCategories, lostManifolds)
                        beatenCR.add(CategoryRecord(record, beatenCategories))
                    }
                }
            }
        }

        handleBeatenRecord(null, unclaimedCategories, possibleManifolds)
        if (unclaimedCategories.isNotEmpty()) {
            beatenCR.add(CategoryRecord(null, unclaimedCategories))
        }
        return SubmitResult.Success(null, null, beatenCR)
    }

    fun overrideScores(overrides: List<Pair<OmRecord, OmScore>>) {
        leaderboard.acquireWriteAccess().use { leaderboardScope ->
            for ((record, newScore) in overrides) {
                val puzzle = record.puzzle
                val dir = leaderboardScope.getPuzzleDir(puzzle)
                val oldFile = record.dataPath.toFile()
                val newFile = File(dir, "${fileStemOf(puzzle, newScore)}.solution")
                oldFile.renameTo(newFile)
                leaderboardScope.rm(oldFile)
                leaderboardScope.add(newFile)
            }
            leaderboardScope.commit("Score overrides (solution)")
            for ((record, newScore) in overrides) {
                val puzzle = record.puzzle
                val dir = leaderboardScope.getPuzzleDir(puzzle)
                leaderboardScope.rm(File(dir, "${record.toFileStem()}.json"))
                val newName = fileStemOf(puzzle, newScore)
                val leaderboardFile = File(dir, "$newName.json")
                val newPath = File(dir, "$newName.solution").relativeTo(leaderboardScope.repo).toPath()
                val newRecord = record.copy(
                    score = newScore,
                    dataPath = newPath,
                    dataLink = createLink(leaderboardScope, puzzle, newScore)
                )
                leaderboardFile.outputStream().buffered().use { json.encodeToStream(newRecord, it) }
                leaderboardScope.add(leaderboardFile)
            }
            leaderboardScope.commitAndPush("Score overrides (metadata)")
            loadData(leaderboardScope)
            pageGenerator.update(leaderboardScope, OmCategory.entries, data)
        }
    }

    fun delete(record: OmRecord) {
        leaderboard.acquireWriteAccess().use { leaderboardScope ->
            val dir = leaderboardScope.getPuzzleDir(record.puzzle)
            leaderboardScope.rm(record.dataPath.toFile())
            leaderboardScope.rm(File(dir, "${record.toFileStem()}.json"))
            leaderboardScope.commitAndPush(null, record.puzzle, record.score, listOf("DELETE"))
            loadData(leaderboardScope)
            pageGenerator.update(leaderboardScope, OmCategory.entries, data)
        }
    }

    fun computeChangesSince(instant: Instant): List<OmRecordChange> {
        return leaderboard.acquireReadAccess().use { leaderboardScope ->
            leaderboardScope.changesSince(instant).mapNotNull { change ->

                try {
                    when (change.type) {
                        DiffEntry.ChangeType.ADD -> if (change.newName!!.endsWith(".json")) {
                            OmRecordChange(OmRecordChangeType.ADD, change.newContent!!.openStream().use { json.decodeFromStream(it) })
                        } else {
                            null
                        }

                        DiffEntry.ChangeType.DELETE -> if (change.oldName!!.endsWith(".json")) {
                            OmRecordChange(OmRecordChangeType.REMOVE, change.oldContent!!.openStream().use { json.decodeFromStream(it) })
                        } else {
                            null
                        }

                        else -> null
                    }
                } catch (e: Exception) {
                    null
                }
            }
        }
    }


    private fun OmSubmission.createMRecord(leaderboardScope: GitRepository.ReadWriteAccess): OmMemoryRecord {
        val name = fileStemOf(puzzle, score)
        val dir = leaderboardScope.getPuzzleDir(puzzle)
        dir.mkdirs()
        val solutionFile = File(dir, "$name.solution")
        solutionFile.writeBytes(data)
        leaderboardScope.add(solutionFile)
        leaderboardScope.commit(author, puzzle, score, listOf("Solution"))
        val path = solutionFile.relativeTo(leaderboardScope.repo).toPath()

        val leaderboardFile = File(dir, "$name.json")
        val record = OmRecord(
            puzzle = puzzle,
            score = score,
            displayLink = displayLink,
            dataLink = createLink(leaderboardScope, puzzle, score),
            dataPath = path,
            lastModified = Clock.System.now(),
        )
        leaderboardFile.outputStream().buffered().use { json.encodeToStream(record, it) }
        leaderboardScope.add(leaderboardFile)

        return record.toMemoryRecord(leaderboardScope.repo.toPath())
    }

    private fun createLink(leaderboardScope: GitRepository.ReadAccess, puzzle: OmPuzzle, score: OmScore) =
        omUrlMapper.createShortUrl(leaderboardScope.shortCurrentHash(), puzzle, score)

    private fun OmMemoryRecord.remove(leaderboardScope: GitRepository.ReadWriteAccess) {
        leaderboardScope.rm(record.dataPath.toFile())
        leaderboardScope.rm(record.dataPath.resolveSibling("${record.toFileStem()}.json").toFile())
        data[record.puzzle]?.remove(this)
    }

    private fun GitRepository.ReadAccess.getPuzzleDir(puzzle: OmPuzzle): File = File(repo, "${puzzle.group.name}/${puzzle.name}")

    private fun fileStemOf(puzzle: OmPuzzle, score: OmScore) = "${score.toDisplayString(DisplayContext.fileName())}_${puzzle.name}"
    private fun OmRecord.toFileStem() = fileStemOf(puzzle, score)

    override fun find(puzzle: OmPuzzle, category: OmCategory): OmRecord? {
        leaderboard.acquireReadAccess().use { l -> loadDataIfNecessary(l) }
        return data[puzzle]?.find { category in it.categories }?.record
    }

    override fun findCategoryHolders(puzzle: OmPuzzle, includeFrontier: Boolean): List<CategoryRecord<OmRecord, OmCategory>> {
        leaderboard.acquireReadAccess().use { l -> loadDataIfNecessary(l) }
        return data[puzzle]
            ?.filter { includeFrontier || it.categories.isNotEmpty() }
            ?.map(OmMemoryRecord::toCategoryRecord)
            ?: emptyList()
    }

    fun findAll(category: OmCategory): Map<OmPuzzle, OmRecord?> {
        leaderboard.acquireReadAccess().use { l -> loadDataIfNecessary(l) }
        return data.entries.filter { category.supportsPuzzle(it.key) }
            .associate { it.key to it.value.find { mr -> category in mr.categories }?.record }
    }

    val records: List<CategoryRecord<OmRecord, OmCategory>>
        get() = data.values.flatten().map(OmMemoryRecord::toCategoryRecord)
}

enum class OmRecordChangeType {
    ADD,
    REMOVE
}

data class OmRecordChange(val type: OmRecordChangeType, val record: OmRecord)