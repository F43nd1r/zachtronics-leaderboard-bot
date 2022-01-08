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

import com.faendir.zachtronics.bot.config.GitProperties
import com.faendir.zachtronics.bot.createGitRepositoryFrom
import com.faendir.zachtronics.bot.git.GitRepository
import com.faendir.zachtronics.bot.testutils.TestGitRepository
import com.faendir.zachtronics.bot.om.dummyOmSubmission
import com.faendir.zachtronics.bot.om.model.OmCategory
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmRecord
import com.faendir.zachtronics.bot.om.model.OmScore
import com.faendir.zachtronics.bot.repository.SubmitResult
import com.google.common.io.Files
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.any
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.first
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.none
import java.io.File

class OmSolutionRepositoryTest {
    private val gitProperties = GitProperties().apply {
        accessToken = ""
        username = "zachtronics-bot-test"
    }

    private lateinit var leaderboardDir: File
    private lateinit var archiveDir: File

    private lateinit var leaderboard: GitRepository

    private lateinit var repository: OmSolutionRepository

    @BeforeEach
    internal fun setUp() {
        leaderboardDir = Files.createTempDir()
        archiveDir = Files.createTempDir()

        leaderboard = createGitRepositoryFrom(leaderboardDir, gitProperties)
        repository = OmSolutionRepository(leaderboard, mockk(relaxed = true))
    }

    @AfterEach
    internal fun tearDown() {
        leaderboard.cleanup()
    }

    @Test
    fun `submit without previous record`() {
        expectThat(repository.findCategoryHolders(OmPuzzle.STABILIZED_WATER, true)).isEmpty()

        val score = OmScore(cost= 10, cycles = 20, area =30)
        val result = repository.submit(dummyOmSubmission(OmPuzzle.STABILIZED_WATER, score))

        expectThat(result).isA<SubmitResult.Success<OmRecord, OmCategory>>().get { beatenRecords }.hasSize(1)
        expectThat(repository.findCategoryHolders(OmPuzzle.STABILIZED_WATER, true)) {
            hasSize(1)
            first().get { record.score }.isEqualTo(score)
        }
        expectThat(repository.findCategoryHolders(OmPuzzle.STABILIZED_WATER, false)).hasSize(1)

    }

    @Test
    fun `submit beating previous record`() {
        repository.submit(dummyOmSubmission(OmPuzzle.STABILIZED_WATER, OmScore(cost = 10, cycles = 20, area =30)))

        val score = OmScore(cost = 5, cycles = 5, area =5)

        val result = repository.submit(dummyOmSubmission(OmPuzzle.STABILIZED_WATER, score))

        expectThat(result).isA<SubmitResult.Success<OmRecord, OmCategory>>()
        expectThat(repository.findCategoryHolders(OmPuzzle.STABILIZED_WATER, true)) {
            hasSize(1)
            first().get { record.score }.isEqualTo(score)
        }
    }

    @Test
    fun `submit overlap record`() {
        repository.submit(dummyOmSubmission(OmPuzzle.STABILIZED_WATER, OmScore(cost = 10, cycles = 20, area =30)))

        val score = OmScore(cost = 5, cycles = 5, area =5, overlap = true)

        val result = repository.submit(dummyOmSubmission(OmPuzzle.STABILIZED_WATER, score))

        expectThat(result).isA<SubmitResult.Success<OmRecord, OmCategory>>()
        expectThat(repository.findCategoryHolders(OmPuzzle.STABILIZED_WATER, true)) {
            hasSize(2)
            any { get { record.score }.isEqualTo(score) }
        }
    }

    @Test
    fun `submit worse`() {
        val score = OmScore(cost = 10, cycles = 20, area =30)
        repository.submit(dummyOmSubmission(OmPuzzle.STABILIZED_WATER, score))

        val result = repository.submit(dummyOmSubmission(OmPuzzle.STABILIZED_WATER, OmScore(cost = 50, cycles = 50, area =50)))

        expectThat(result).isA<SubmitResult.NothingBeaten<OmRecord, OmCategory>>()
        expectThat(repository.findCategoryHolders(OmPuzzle.STABILIZED_WATER, true)) {
            hasSize(1)
            first().get { record.score }.isEqualTo(score)
        }
    }

    @Test
    fun `submit same`() {
        val score = OmScore(cost = 10, cycles = 20, area =30)
        repository.submit(dummyOmSubmission(OmPuzzle.STABILIZED_WATER, score))

        val result = repository.submit(dummyOmSubmission(OmPuzzle.STABILIZED_WATER, score))

        expectThat(result).isA<SubmitResult.AlreadyPresent<OmRecord, OmCategory>>()
        expectThat(repository.findCategoryHolders(OmPuzzle.STABILIZED_WATER, true)) {
            hasSize(1)
            first().get { record.score }.isEqualTo(score)
        }
    }

    @Test
    fun `submit new gif`() {
        val score = OmScore(cost = 10, cycles = 20, area =30)
        repository.submit(dummyOmSubmission(OmPuzzle.STABILIZED_WATER, score, displayLink = "https://bad.gif"))

        val gif = "https://better.gif"
        val result = repository.submit(dummyOmSubmission(OmPuzzle.STABILIZED_WATER, score, displayLink = gif))

        expectThat(result).isA<SubmitResult.Success<OmRecord, OmCategory>>()
        expectThat(repository.findCategoryHolders(OmPuzzle.STABILIZED_WATER, true)) {
            hasSize(1)
            first().get { record.displayLink }.isEqualTo(gif)
        }
    }

    @Test
    fun `submit pareto record`() {
        repository.submit(dummyOmSubmission(OmPuzzle.STABILIZED_WATER, OmScore(cost = 10, cycles = 20, area =30)))
        repository.submit(dummyOmSubmission(OmPuzzle.STABILIZED_WATER, OmScore(cost = 30, cycles = 20, area =10)))

        val score = OmScore(cost = 25, cycles = 25, area =25)

        val result = repository.submit(dummyOmSubmission(OmPuzzle.STABILIZED_WATER, score))

        expectThat(result).isA<SubmitResult.Success<OmRecord, OmCategory>>()
        expectThat(repository.findCategoryHolders(OmPuzzle.STABILIZED_WATER, true)) {
            hasSize(3)
            any { get { record.score }.isEqualTo(score) }
        }
        expectThat(repository.findCategoryHolders(OmPuzzle.STABILIZED_WATER, false)) {
            hasSize(2)
            none { get { record.score }.isEqualTo(score) }
        }
    }

    @Test
    fun `data survives reload`() {
        repository.submit(dummyOmSubmission(OmPuzzle.STABILIZED_WATER, OmScore(cost = 10, cycles = 20, area =30), displayLink = "https://some.gif"))
        repository.submit(dummyOmSubmission(OmPuzzle.STABILIZED_WATER, OmScore(cost = 30, cycles = 22, area =10)))
        repository.submit(dummyOmSubmission(OmPuzzle.STABILIZED_WATER, OmScore(cost = 25, cycles = 25, area =25)))
        repository.submit(dummyOmSubmission(OmPuzzle.STABILIZED_WATER, OmScore(cost = 5, cycles = 5, area =5, overlap = true)))
        repository.submit(dummyOmSubmission(OmPuzzle.STABILIZED_WATER, OmScore(cost = 50, cycles = 50, area =15, trackless = true)))

        val data = repository.findCategoryHolders(OmPuzzle.STABILIZED_WATER, true)

        val newLeaderboard = TestGitRepository(gitProperties, leaderboardDir)
        val newRepository = OmSolutionRepository(newLeaderboard, mockk(relaxed = true))

        expectThat(newRepository.findCategoryHolders(OmPuzzle.STABILIZED_WATER, true)) {
            hasSize(data.size)
            data.forEach { old ->
                any {
                    get { categories }.containsExactlyInAnyOrder(old.categories)
                    get { record.score }.isEqualTo(old.record.score)
                    get { record.displayLink }.isEqualTo(old.record.displayLink)
                }
            }
        }

        newLeaderboard.cleanup()
    }
}