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

import com.faendir.zachtronics.bot.om.model.OmCategory
import com.faendir.zachtronics.bot.om.model.OmMetric
import com.faendir.zachtronics.bot.om.model.OmMetrics
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmRecord
import com.faendir.zachtronics.bot.om.model.OmScore
import com.faendir.zachtronics.bot.om.model.OmScoreManifold
import com.google.common.collect.Sets
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable
import java.io.File
import java.nio.file.Path
import java.util.*
import kotlin.system.measureTimeMillis

class Labeler {
    private fun <T> Set<T>.isSupersetOfAny(sets: Iterable<Set<T>>) = sets.any { this.containsAll(it) }

    /** returns a list of possible names for `score`.
     *  at this point the “names” are just ordered lists of sets of metrics, like {{cycles}, {gold, height}}.
     *  turning those into the C(GH) notation can be done later.
     */
    private fun names(score: OmScore, metrics: Set<OmMetric>, otherScores: Iterable<OmScore>): List<List<Set<OmMetric>>> {
        val goodNames = mutableListOf<List<Set<OmMetric>>>()
        val goodSets = mutableListOf<Set<OmMetric>>()

        powerSet@ for (candidateSet in Sets.powerSet(metrics)) {
            if (candidateSet.isEmpty() || candidateSet.isSupersetOfAny(goodSets)) continue

            val ties = mutableListOf<OmScore>()
            val comparators = candidateSet.flatMap { it.scoreParts }.map { it.comparator }

            for (otherScore in otherScores) {
                val compares = comparators.map { it.compare(score, otherScore) }
                if (compares.all { it >= 0 }) {
                    if (compares.all { it == 0 })
                        ties.add(otherScore)
                    else
                        continue@powerSet
                }
            }
            goodSets.add(candidateSet)

            if (ties.isEmpty())
                goodNames.add(listOf(candidateSet))
            else {
                for (suffix in names(score, metrics.minus(candidateSet), ties)) {
                    goodNames.add(listOf(candidateSet) + suffix)
                }
            }
        }
        return goodNames.sortedBy { it.size }
    }

    private fun realNames(score: OmScore, metrics: Set<OmMetric>, allScores: Collection<OmScore>): List<String> {
        return names(score, metrics, allScores.filter { it != score })
            .map { ls ->
                ls.joinToString("") { s ->
                    when (s.size) {
                        0 -> throw IllegalStateException()
                        1 -> s.first().displayName
                        else -> s.joinToString("", "(", ")") { it.displayName }
                    }
                }
            }
    }


    private val json = Json {
        prettyPrint = true
        allowSpecialFloatingPointValues = true
    }

    private val memoryRecordOrder = Comparator.comparing({ r: OmMemoryRecord -> r.record.score },
        (listOf(OmMetric.OVERLAP) + OmMetrics.VALUE) // overlap scores last, trackless in the mix
            .map { it.comparator }
            .reduce(Comparator<OmScore>::thenComparing))

    @OptIn(ExperimentalSerializationApi::class)
    private fun loadData(): Map<OmPuzzle, TreeSet<OmMemoryRecord>> {
        val repo = File("../om-leaderboard")
        val data = OmPuzzle.entries.associateWith { sortedSetOf(memoryRecordOrder) }
        for ((puzzle, memoryRecords) in data.entries) {
            // fill map
            File(repo, "${puzzle.group.name}/${puzzle.name}").takeIf { it.exists() }
                ?.listFiles { file -> file.extension == "json" }
                ?.map { file ->
                    file.inputStream().buffered().use { json.decodeFromStream<OmRecord>(it) }
                }
                ?.map { it.toMemoryRecord(repo.toPath()) }
                ?.forEach(memoryRecords::add)

            // fill valid manifolds
            for (mRecord in memoryRecords) {
                manifolds@ for (manifold in OmScoreManifold.values()) {
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
                for (category in OmCategory.values().filter { it.supportsPuzzle(puzzle) }) {
                    memoryRecords
                        .filter { category.supportsScore(it.record.score) }
                        .minWithOrNull(Comparator.comparing({ it.record.score }, category.scoreComparator))
                        ?.categories
                        ?.add(category)
                }
            }
        }
        return data
    }

    @Test
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Testing")
    fun loadMeBabyOneMoreTime() {
        val data = loadData()
        assertEquals(11293, data.values.sumOf { it.size })
        val puzzleData = data[OmPuzzle.STABILIZED_WATER]!!
        val labeled1: Map<OmMemoryRecord, List<String>>
        println(
            "Time to label IEEE: ${
                measureTimeMillis {
                    val scoresByManifold = puzzleData
                        .flatMap { mr -> mr.frontierManifolds.associateWith { mr.record.score }.entries }
                        .groupBy({ it.key }, { it.value })
                    labeled1 = puzzleData.associateWith { mr ->
                        scoresByManifold.entries.flatMap { (m, s) ->
                            realNames(mr.record.score, m.scoreParts.toSet(), s)
                        }
                    }
                }
            }"
        )
        val labeled2: MutableMap<OmMemoryRecord, MutableSet<String>>
        println(
            "Time to label F43nd1r: ${
                measureTimeMillis {
                    val recordsByManifold = puzzleData
                        .flatMap { mr -> mr.frontierManifolds.associateWith { mr }.entries }
                        .groupBy({ it.key }, { it.value })
                    labeled2 = mutableMapOf()
                    for ((manifold, records) in recordsByManifold) {
                        for ((record, names) in names2(manifold.scoreParts.toSet(), records.toSet())) {
                            labeled2.getOrPut(record) { mutableSetOf() }.addAll(names.map { it.toString() })
                        }
                    }
                }
            }"
        )
        puzzleData.take(10).forEach {
            println("------- Score: ${it.record.score.toDisplayString()}")
            println("-- Names IEEE: ${labeled1[it]?.toSet()?.sorted()}")
            println("Names F43nd1r: ${labeled2[it]?.sorted()}")
        }
    }

    private fun names2(metrics: Set<OmMetric>, records: Set<OmMemoryRecord>): Map<OmMemoryRecord, Set<OmParetoName>> {
        val recordsByNamePart = mutableMapOf<Set<OmMetric>, MutableSet<OmMemoryRecord>>()

        for (key in Sets.powerSet(metrics).minusElement(emptySet()).sortedBy { it.size }) {
            val potentialRecords = records - Sets.powerSet(key).minusElement(emptySet()).flatMapTo(mutableSetOf()) { recordsByNamePart[it] ?: emptySet() }
            val comparators = key.flatMap { it.scoreParts }.map { it.comparator }
            for (potentialRecord in potentialRecords) {
                if (records.none { record ->
                        if (record == potentialRecord) return@none false
                        val compares = comparators.map { it.compare(record.record.score, potentialRecord.record.score) }
                        compares.all { it <= 0 } && compares.any { it < 0 }
                    }) {
                    recordsByNamePart.getOrPut(key) { mutableSetOf() }.add(potentialRecord)
                }
            }
        }

        val result = mutableMapOf<OmMemoryRecord, MutableSet<OmParetoName>>()

        for ((key, recordsForKey) in recordsByNamePart) {
            val name = OmParetoName(listOf(OmParetoNamePart(key)))
            if (recordsForKey.size == 1 || metrics.size == key.size) {
                for (record in recordsForKey) {
                    result.getOrPut(record) { mutableSetOf() }.add(name)
                }
            } else {
                for ((record, names) in names2(metrics.minus(key), recordsForKey)) {
                    result.getOrPut(record) { mutableSetOf() }.addAll(names.map { name + it })
                }
            }
        }
        return result
    }

    @Test
    fun simple() {
        val records = listOf(
            OmMemoryRecord(
                OmRecord(
                    puzzle = OmPuzzle.STABILIZED_WATER,
                    score = OmScore(
                        cost = 1,
                        area = 5,
                        instructions = 0,
                        overlap = false,
                        trackless = false,
                        cycles = 0,
                        height = 0,
                        width = 0.0,
                        rate = 0.0,
                        areaINF = null,
                        heightINF = null,
                        widthINF = null,
                    ),
                    displayLink = null,
                    dataLink = "",
                    dataPath = Path.of(""),
                )
            ), OmMemoryRecord(
                OmRecord(
                    puzzle = OmPuzzle.STABILIZED_WATER,
                    score = OmScore(
                        cost = 5,
                        area = 1,
                        instructions = 0,
                        overlap = false,
                        trackless = false,
                        cycles = 0,
                        height = 0,
                        width = 0.0,
                        rate = 0.0,
                        areaINF = null,
                        heightINF = null,
                        widthINF = null,
                    ),
                    displayLink = null,
                    dataLink = "",
                    dataPath = Path.of(""),
                )
            ), OmMemoryRecord(
                OmRecord(
                    puzzle = OmPuzzle.STABILIZED_WATER,
                    score = OmScore(
                        cost = 2,
                        area = 3,
                        instructions = 0,
                        overlap = false,
                        trackless = false,
                        cycles = 0,
                        height = 0,
                        width = 0.0,
                        rate = 0.0,
                        areaINF = null,
                        heightINF = null,
                        widthINF = null,
                    ),
                    displayLink = null,
                    dataLink = "",
                    dataPath = Path.of(""),
                )
            ), OmMemoryRecord(
                OmRecord(
                    puzzle = OmPuzzle.STABILIZED_WATER,
                    score = OmScore(
                        cost = 3,
                        area = 2,
                        instructions = 0,
                        overlap = false,
                        trackless = false,
                        cycles = 0,
                        height = 0,
                        width = 0.0,
                        rate = 0.0,
                        areaINF = null,
                        heightINF = null,
                        widthINF = null,
                    ),
                    displayLink = null,
                    dataLink = "",
                    dataPath = Path.of(""),
                )
            )
        )

        val names = names2(setOf(OmMetric.COST, OmMetric.AREA, OmMetric.CYCLES), records.toSet())

        records.forEach {
            println("Score: ${it.record.score.cost}g/${it.record.score.area}a/${it.record.score.cycles}c")
            println("Names: ${names[it]}")
        }
    }
}