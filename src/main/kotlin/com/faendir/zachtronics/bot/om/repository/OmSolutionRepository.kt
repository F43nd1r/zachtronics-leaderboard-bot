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

package com.faendir.zachtronics.bot.om.repository

import com.faendir.zachtronics.bot.git.GitRepository
import com.faendir.zachtronics.bot.model.DisplayContext
import com.faendir.zachtronics.bot.om.model.OmCategory
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmRecord
import com.faendir.zachtronics.bot.om.model.OmScore
import com.faendir.zachtronics.bot.om.model.OmSubmission
import com.faendir.zachtronics.bot.om.rest.OmUrlMapper
import com.faendir.zachtronics.bot.repository.CategoryRecord
import com.faendir.zachtronics.bot.repository.SolutionRepository
import com.faendir.zachtronics.bot.repository.SubmitResult
import com.faendir.zachtronics.bot.utils.add
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.eclipse.jgit.diff.DiffEntry
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.io.File
import java.time.Instant
import java.util.*
import javax.annotation.PostConstruct

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
    private val recordOrder = Comparator
        .comparing<OmRecord, Int> {
            when {
                it.score.overlap -> 1
                it.score.trackless -> 0
                else -> 0
            }
        }
        .thenComparingInt { it.score.cost ?: Int.MAX_VALUE }
        .thenComparingInt { it.score.cycles ?: Int.MAX_VALUE }
        .thenComparingInt { it.score.area ?: Int.MAX_VALUE }
        .thenComparingInt { it.score.instructions ?: Int.MAX_VALUE }
        .thenComparingInt { it.score.height ?: Int.MAX_VALUE }
        .thenComparingDouble { it.score.width ?: Double.MAX_VALUE }
    private lateinit var data: Map<OmPuzzle, SortedMap<OmRecord, MutableSet<OmCategory>>>
    private var hash: String? = null

    @PostConstruct
    fun init() {
        leaderboard.acquireReadAccess().use { leaderboardScope ->
            loadData(leaderboardScope)
            pageGenerator.update(leaderboardScope, OmCategory.values().toList(), data)
        }
    }

    private fun loadData(leaderboardScope: GitRepository.ReadAccess) {
        data = OmPuzzle.values().associateWith { sortedMapOf(recordOrder) }
        for (puzzle in OmPuzzle.values()) {
            val records = data.getValue(puzzle)
            leaderboardScope.getPuzzleDir(puzzle).takeIf { it.exists() }
                ?.listFiles()
                ?.filter { file -> file.extension == "json" }
                ?.map { file -> file.inputStream().buffered().use { json.decodeFromStream<OmRecord>(it) } }
                ?.forEach { record -> records.add(record.copy(dataPath = record.dataPath?.let { leaderboardScope.repo.toPath().resolve(it) }), mutableSetOf()) }
            if (records.isNotEmpty()) {
                for (category in OmCategory.values().filter { it.supportsPuzzle(puzzle) }.toMutableSet()) {
                    records.entries.filter { category.supportsScore(it.key.score) }
                        .reduceOrNull { a, b -> if (category.scoreComparator.compare(a.key.score, b.key.score) <= 0) a else b }
                        ?.value?.add(category)
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
        if (submission.displayLink == null) throw IllegalArgumentException("Submissions must have a gif")
        if (submission.displayLink.endsWith(".solution")) throw IllegalArgumentException("You cannot use solution files as gifs.")
        return leaderboard.acquireWriteAccess().use { leaderboardScope ->
            val records = data.getValue(submission.puzzle)
            val newRecord by lazy { submission.createRecord(leaderboardScope) }
            val result = submit(leaderboardScope, submission) { beatenRecord, beatenCategories, shouldRemain ->
                if (beatenRecord != null) {
                    if (shouldRemain) {
                        records.getValue(beatenRecord) -= beatenCategories
                    } else {
                        beatenRecord.remove(leaderboardScope)
                    }
                }
                records.add(newRecord, beatenCategories.toMutableSet())
            }
            if (result is SubmitResult.Success) {
                pageGenerator.update(leaderboardScope, result.beatenRecords.flatMap { it.categories }, data)
                leaderboardScope.commitAndPush(
                    submission.author,
                    submission.puzzle,
                    submission.score,
                    result.beatenRecords.flatMap { it.categories }.map { it.toString() })
                hash = leaderboardScope.currentHash()
            }
            result
        }
    }

    fun submitDryRun(submission: OmSubmission): SubmitResult<OmRecord, OmCategory> {
        return leaderboard.acquireReadAccess().use { leaderboardScope -> submit(leaderboardScope, submission) { _, _, _ -> } }
    }

    private fun submit(
        leaderboardScope: GitRepository.ReadAccess,
        submission: OmSubmission,
        handleBeatenRecord: (beatenRecord: OmRecord?, beatenCategories: Set<OmCategory>, shouldRemain: Boolean) -> Unit
    ): SubmitResult<OmRecord, OmCategory> {
        loadDataIfNecessary(leaderboardScope)
        val records = data.getValue(submission.puzzle)
        val unclaimedCategories = OmCategory.values().filter { it.supportsPuzzle(submission.puzzle) && it.supportsScore(submission.score) }.toMutableSet()
        val result = mutableListOf<CategoryRecord<OmRecord?, OmCategory>>()
        for ((record, categories) in records.toMap()) {
            if (submission.score == record.score || submission.score.isSupersetOf(record.score)) {
                @Suppress("LiftReturnOrAssignment")
                if (submission.score.isSupersetOf(record.score)) {
                    handleBeatenRecord(record, categories, false)
                    unclaimedCategories -= categories
                    result.add(CategoryRecord(record, categories.toSet()))
                    continue
                } else if (submission.displayLink != record.displayLink || record.dataLink == null) {
                    handleBeatenRecord(record, categories, false)
                    return SubmitResult.Updated(CategoryRecord(record, categories.toSet()))
                } else {
                    return SubmitResult.AlreadyPresent()
                }
            }
            if (record.score.isStrictlyBetterThan(submission.score)) {
                return SubmitResult.NothingBeaten(records.filter { it.key.score.isStrictlyBetterThan(submission.score) }
                    .map { CategoryRecord(it.key, it.value) })
            }
            if (categories.isEmpty()) {
                if (submission.score.isStrictlyBetterThan(record.score)) {
                    handleBeatenRecord(record, emptySet(), false)
                    result.add(CategoryRecord(record, emptySet()))
                }
            } else {
                unclaimedCategories -= categories
                val beatenCategories = categories.filter { category ->
                    category.supportsScore(submission.score) && category.scoreComparator.compare(submission.score, record.score).let {
                        it < 0 || it == 0 && submission.displayLink != record.displayLink
                    }
                }.toSet()
                if (beatenCategories.isNotEmpty()) {
                    handleBeatenRecord(
                        record,
                        beatenCategories,
                        categories.size == beatenCategories.size && submission.score.isStrictlyBetterThan(record.score)
                    )
                    result.add(CategoryRecord(record, beatenCategories))
                }
            }
        }
        handleBeatenRecord(null, unclaimedCategories, false)
        if (unclaimedCategories.isNotEmpty()) {
            result.add(CategoryRecord(null, unclaimedCategories))
        }
        return SubmitResult.Success(null, result)
    }

    fun overrideScores(overrides: List<Pair<OmRecord, OmScore>>) {
        leaderboard.acquireWriteAccess().use { leaderboardScope ->
            for ((record, newScore) in overrides) {
                val puzzle = record.puzzle
                val dir = leaderboardScope.getPuzzleDir(puzzle)
                val oldFile = record.dataPath?.toFile() ?: continue
                val newFile = File(dir, "${newScore.toFileString()}_${puzzle.name}.solution")
                oldFile.renameTo(newFile)
                leaderboardScope.rm(oldFile)
                leaderboardScope.add(newFile)
            }
            leaderboardScope.commit("Score overrides (solution)")
            for ((record, newScore) in overrides) {
                val puzzle = record.puzzle
                val dir = leaderboardScope.getPuzzleDir(puzzle)
                leaderboardScope.rm(File(dir, "${record.score.toFileString()}_${puzzle.name}.json"))
                val newName = "${newScore.toFileString()}_${puzzle.name}"
                val leaderboardFile = File(dir, "$newName.json")
                val newPath = File(dir, "$newName.solution").takeIf { it.exists() }?.relativeTo(leaderboardScope.repo)?.toPath()
                val newRecord = record.copy(
                    score = newScore,
                    dataPath = newPath,
                    dataLink = createLink(leaderboardScope, puzzle, newName)
                )
                leaderboardFile.outputStream().buffered().use { json.encodeToStream(newRecord, it) }
                leaderboardScope.add(leaderboardFile)
            }
            leaderboardScope.commitAndPush("Score overrides (metadata)")
            loadData(leaderboardScope)
            pageGenerator.update(leaderboardScope, OmCategory.values().toList(), data)
        }
    }

    fun delete(record: OmRecord) {
        leaderboard.acquireWriteAccess().use { leaderboardScope ->
            val dir = leaderboardScope.getPuzzleDir(record.puzzle)
            record.dataPath?.toFile()?.let { leaderboardScope.rm(it) }
            leaderboardScope.rm(File(dir, "${record.score.toFileString()}_${record.puzzle.name}.json"))
            leaderboardScope.commitAndPush(null, record.puzzle, record.score, listOf("DELETE"))
            loadData(leaderboardScope)
            pageGenerator.update(leaderboardScope, OmCategory.values().toList(), data)
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


    private fun OmSubmission.createRecord(leaderboardScope: GitRepository.ReadWriteAccess): OmRecord {
        val name = "${score.toFileString()}_${puzzle.name}"
        val dir = leaderboardScope.getPuzzleDir(puzzle)
        dir.mkdirs()
        val archiveFile = File(dir, "$name.solution")
        archiveFile.writeBytes(data)
        leaderboardScope.add(archiveFile)
        leaderboardScope.commit(author, puzzle, score, listOf("Solution"))
        val path = archiveFile.relativeTo(leaderboardScope.repo).toPath()

        val leaderboardFile = File(dir, "$name.json")
        val record = OmRecord(
            puzzle = puzzle,
            score = score,
            displayLink = displayLink,
            dataLink = createLink(leaderboardScope, puzzle, name),
            dataPath = path,
        )
        leaderboardFile.outputStream().buffered().use { json.encodeToStream(record, it) }
        leaderboardScope.add(leaderboardFile)

        return record.copy(dataPath = archiveFile.toPath())
    }

    private fun createLink(leaderboardScope: GitRepository.ReadAccess, puzzle: OmPuzzle, name: String) =
        omUrlMapper.createShortUrl(leaderboardScope.shortCurrentHash(), puzzle, name)

    private fun OmRecord.remove(leaderboardScope: GitRepository.ReadWriteAccess) {
        dataPath?.let { dataPath -> leaderboardScope.rm(dataPath.toFile()) }
        leaderboardScope.rm(File(leaderboardScope.getPuzzleDir(puzzle), "${score.toFileString()}_${puzzle.name}.json"))
        data[puzzle]?.remove(this)
    }

    private fun GitRepository.ReadAccess.getPuzzleDir(puzzle: OmPuzzle): File = File(repo, "${puzzle.group.name}/${puzzle.name}")

    private fun OmScore.toFileString() = toDisplayString(DisplayContext.fileName())

    override fun find(puzzle: OmPuzzle, category: OmCategory): OmRecord? {
        leaderboard.acquireReadAccess().use { l -> loadDataIfNecessary(l) }
        return data[puzzle]?.entries?.find { (_, categories) -> categories.contains(category) }?.key
    }

    override fun findCategoryHolders(puzzle: OmPuzzle, includeFrontier: Boolean): List<CategoryRecord<OmRecord, OmCategory>> {
        leaderboard.acquireReadAccess().use { l -> loadDataIfNecessary(l) }
        return data[puzzle]?.entries?.filter { (_, categories) -> includeFrontier || categories.isNotEmpty() }
            ?.map { (record, categories) -> CategoryRecord(record, categories) } ?: emptyList()
    }

    fun findAll(category: OmCategory): Map<OmPuzzle, OmRecord?> {
        leaderboard.acquireReadAccess().use { l -> loadDataIfNecessary(l) }
        return data.entries.filter { category.supportsPuzzle(it.key) }
            .associate { it.key to it.value.entries.find { (_, categories) -> categories.contains(category) }?.key }
    }
}

enum class OmRecordChangeType {
    ADD,
    REMOVE
}

data class OmRecordChange(val type: OmRecordChangeType, val record: OmRecord)