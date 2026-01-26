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

package com.faendir.zachtronics.bot.om.validation

import com.faendir.zachtronics.bot.validation.ValidationException
import java.io.Closeable
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

class OmSolutionVerifier(puzzle: ByteArray, solution: ByteArray) : Closeable {
    private val arena: Arena = Arena.ofConfined()
    private val verifier: MemorySegment

    init {
        verifier = OmSimWrapper.verifier_create_from_bytes_without_copying(
            toMemorySegment(puzzle), puzzle.size, toMemorySegment(solution), solution.size
        )
    }

    private fun toMemorySegment(array: ByteArray): MemorySegment {
        return arena.allocateFrom(ValueLayout.JAVA_BYTE, *array)
    }

    private fun toMemorySegment(s: String): MemorySegment {
        return arena.allocateFrom(s)
    }

    private fun throwIfError(verifier: MemorySegment) {
        val error = OmSimWrapper.verifier_error(verifier)
        if (error != MemorySegment.NULL) {
            val errorString = error.getString(0)
            OmSimWrapper.verifier_error_clear(verifier)
            throw ValidationException(errorString)
        }
    }

    fun getMetric(metric: OmSimMetric<Int>): Int {
        val result = OmSimWrapper.verifier_evaluate_metric(verifier, toMemorySegment(metric.id))
        throwIfError(verifier)
        return result
    }

    fun getApproximateMetric(metric: OmSimMetric<*>): Double {
        val result = OmSimWrapper.verifier_evaluate_approximate_metric(verifier, toMemorySegment(metric.id))
        throwIfError(verifier)
        return result
    }

    fun getMetricSafe(metric: OmSimMetric<Int>): Int? {
        val result = OmSimWrapper.verifier_evaluate_metric(verifier, toMemorySegment(metric.id))
        val error = OmSimWrapper.verifier_error(verifier)
        if (error != MemorySegment.NULL) {
            OmSimWrapper.verifier_error_clear(verifier)
            return null
        } else {
            return result
        }
    }

    fun getApproximateMetricSafe(metric: OmSimMetric<*>): Double? {
        val result = OmSimWrapper.verifier_evaluate_approximate_metric(verifier, toMemorySegment(metric.id))
        val error = OmSimWrapper.verifier_error(verifier)
        if (error != MemorySegment.NULL) {
            OmSimWrapper.verifier_error_clear(verifier)
            return null
        } else {
            return result
        }
    }

    override fun close() {
        OmSimWrapper.verifier_destroy(verifier)
        arena.close()
    }
}
