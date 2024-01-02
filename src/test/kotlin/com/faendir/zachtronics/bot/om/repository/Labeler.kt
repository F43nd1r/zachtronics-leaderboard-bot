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
                        val modifiers = manifold.scoreParts.filterIsInstance<OmMetric.Modifier>().toSet()
                        val values = manifold.scoreParts.filterIsInstance<OmMetric.Value<*>>().toSet()
                        for (modifierCombination in Sets.powerSet(modifiers)) {
                            val tree = OmLabellingTree(
                                records = records.filterTo(mutableSetOf()) { record ->
                                    (OmMetric.OVERLAP in modifierCombination || !record.record.score.overlap) &&
                                            (OmMetric.TRACKLESS !in modifierCombination || record.record.score.trackless) &&
                                            (OmMetric.LOOPING !in modifierCombination || record.record.score.looping)
                                },
                                metrics = values
                            )
                            for (record in records) {
                                labeled2.getOrPut(record) { mutableSetOf() }
                                    .addAll(tree.namesOf(record).map { modifierCombination.joinToString("") + it })
                            }
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
}

private fun <T : Comparable<T>> Set<OmMemoryRecord>.sortIntoBuckets(metric: OmMetric.ScorePart<T>): List<Set<OmMemoryRecord>> {
    return groupBy { metric.getValueFrom(it.record.score) }.values.sortedWith { a, b ->
        metric.comparator.compare(
            a.first().record.score,
            b.first().record.score
        )
    }.map { it.toSet() }
}

internal class OmLabellingTree(
    private val records: MutableSet<OmMemoryRecord>,
    private val metrics: Set<OmMetric.Value<*>>,
    sortedRecordsByMetric: Map<OmMetric.Value<*>, List<Set<OmMemoryRecord>>> = metrics.associateWith { records.sortIntoBuckets(it) },
) {
    private val children = mutableMapOf<OmParetoNamePart, OmLabellingTree>()

    init {
        if (records.size > 1 && metrics.isNotEmpty()) {

            for (key in Sets.powerSet(metrics).minusElement(emptySet()).sortedBy { it.size }) {
                val potentialRecords =
                    records - Sets.powerSet(key).minusElement(emptySet()).flatMapTo(mutableSetOf()) { children[OmParetoNamePart(it)]?.records ?: emptySet() }
                if (potentialRecords.isEmpty()) continue

                val namePart = OmParetoNamePart(key)
                val remainingMetrics = metrics - key
                when (key.size) {
                    1 -> {
                        sortedRecordsByMetric.getValue(key.first()).firstOrNull()
                            ?.let { best ->
                                children[namePart] = OmLabellingTree(
                                    records = best.toMutableSet(),
                                    metrics = remainingMetrics,
                                    sortedRecordsByMetric = sortedRecordsByMetric.filterKeys { it in remainingMetrics }
                                        .mapValues { (_, value) -> value.map { it.intersect(best) } }
                                )
                            }
                    }

                    2 -> {
                        val (part1, part2) = key.toList()
                        val sortedRecords = sortedRecordsByMetric.getValue(part1).flatMap { it.sortIntoBuckets(part2) }
                        var min2: OmScore = sortedRecords.first().first().record.score
                        val best = sortedRecords.first().intersect(potentialRecords).toMutableSet()
                        for (recordBucket in sortedRecords.drop(1)) {
                            val compare2 = part2.comparator.compare(recordBucket.first().record.score, min2)
                            if (compare2 < 0) {
                                min2 = recordBucket.first().record.score
                                best.addAll(recordBucket.intersect(potentialRecords))
                            }
                        }
                        if (best.isNotEmpty()) {
                            children[namePart] = OmLabellingTree(
                                records = best,
                                metrics = remainingMetrics,
                                sortedRecordsByMetric = sortedRecordsByMetric.filterKeys { it in remainingMetrics }
                                    .mapValues { (_, value) -> value.map { it.intersect(best) } }
                            )
                        }
                    }

                    else -> {
                        val comparators = key.map { it.comparator }
                        val best = potentialRecords.filter { potentialRecord ->
                            records.none { record ->
                                if (record == potentialRecord) return@none false
                                val compares = comparators.map { it.compare(record.record.score, potentialRecord.record.score) }
                                compares.all { it <= 0 } && compares.any { it < 0 }
                            }
                        }.toMutableSet()
                        if (best.isNotEmpty()) {
                            children[namePart] = OmLabellingTree(
                                records = best,
                                metrics = remainingMetrics,
                                sortedRecordsByMetric = sortedRecordsByMetric.filterKeys { it in remainingMetrics }
                                    .mapValues { (_, value) -> value.map { it.intersect(best) } }
                            )
                        }
                    }
                }
            }
        }
    }

    fun namesOf(record: OmMemoryRecord): Set<OmParetoName> {
        if (record !in records) return emptySet()
        if (children.isEmpty()) return setOf(OmParetoName(listOf()))
        val names = mutableSetOf<OmParetoName>()
        for ((namePart, child) in children) {
            val name = OmParetoName(listOf(namePart))
            child.namesOf(record).forEach { names.add(name + it) }
        }
        return names
    }
}