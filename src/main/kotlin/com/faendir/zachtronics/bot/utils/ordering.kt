/*
 * Copyright (c) 2026
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

package com.faendir.zachtronics.bot.utils

/** used as the comparison result of a partial order */
enum class CompareResult {
    SMALLER, EQUAL, BIGGER, UNCOMPARABLE
}

fun <T> Iterable<T>.allMinsWith(comparator: Comparator<in T>): List<T> {
    val iterator = iterator()
    if (!iterator.hasNext()) return emptyList()

    val result = mutableListOf(iterator.next())
    while (iterator.hasNext()) {
        val next = iterator.next()
        val comparison = comparator.compare(next, result[0])
        if (comparison < 0) {
            result.clear()
            result.add(next)
        } else if (comparison == 0) {
            result.add(next)
        }
    }
    return result
}

fun <T> Iterable<Comparator<in T>>.partialCompare(x1: T, x2: T): CompareResult {
    var smaller = false
    var bigger = false
    for (comparator in this) {
        val comparison = comparator.compare(x1, x2)
        when {
            comparison < 0 -> smaller = true
            comparison > 0 -> bigger = true
        }
    }
    return when {
        smaller && bigger -> CompareResult.UNCOMPARABLE
        smaller -> CompareResult.SMALLER
        bigger -> CompareResult.BIGGER
        else -> CompareResult.EQUAL
    }
}

fun <T> Iterable<T>.paretoFrontierWith(comparators: Collection<Comparator<in T>>): List<T> {
    val frontier = mutableListOf<T>()
    candidates@ for (candidate in this) {
        val iter = frontier.listIterator()
        while (iter.hasNext()) {
            val point = iter.next()
            when (comparators.partialCompare(candidate, point)) {
                CompareResult.SMALLER -> iter.remove()
                CompareResult.EQUAL -> break
                CompareResult.BIGGER -> continue@candidates
                CompareResult.UNCOMPARABLE -> continue
            }
        }
        frontier.add(candidate)
    }
    return frontier
}
