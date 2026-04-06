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

import com.faendir.zachtronics.bot.om.model.OmMetric.*
import com.faendir.zachtronics.bot.om.model.OmMetrics
import com.faendir.zachtronics.bot.om.validation.OmQL.QueryElement
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.contains
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo

class OmQLTest {

    private val possibleMetrics = OmMetrics.FULL_SCORE + OmMetrics.COMPUTED_BY_TYPE.values.flatten()

    @Test
    fun `valid C`() {
        val query = "C"
        val elements = OmQL.parseQuery(query, possibleMetrics)
        expectThat(elements).hasSize(2) // NOVERLAP + C
        expectThat(elements[0]).isA<QueryElement.Constraint>()
        expectThat((elements[0] as QueryElement.Constraint).metric).isEqualTo(NOVERLAP)
        expectThat(elements[1]).isA<QueryElement.Min>()
        expectThat((elements[1] as QueryElement.Min).metric).isEqualTo(CYCLES)
    }

    @Test
    fun `valid O`() {
        val query = "O"
        val elements = OmQL.parseQuery(query, possibleMetrics)
        expectThat(elements).hasSize(1) // OVERLAP
        expectThat(elements[0]).isA<QueryElement.Min>()
        expectThat((elements[0] as QueryElement.Min).metric).isEqualTo(OVERLAP)
    }

    @Test
    fun `valid CA`() {
        val query = "CA"
        val elements = OmQL.parseQuery(query, possibleMetrics)
        expectThat(elements).hasSize(3) // NOVERLAP + C + A
        expectThat(elements[1]).isA<QueryElement.Min>()
        expectThat((elements[1] as QueryElement.Min).metric).isEqualTo(CYCLES)
        expectThat(elements[2]).isA<QueryElement.Min>()
        expectThat((elements[2] as QueryElement.Min).metric).isEqualTo(AREA)
    }

    @Test
    fun `valid {C=3}`() {
        val query = "{C=3}"
        val elements = OmQL.parseQuery(query, possibleMetrics)
        expectThat(elements).hasSize(2) // NOVERLAP + {C=3}
        expectThat(elements[1]).isA<QueryElement.Constraint>()
        expectThat((elements[1] as QueryElement.Constraint).metric.scoreParts).contains(CYCLES)
    }

    @Test
    fun `valid (CA)`() {
        val query = "(CA)"
        val elements = OmQL.parseQuery(query, possibleMetrics)
        expectThat(elements).hasSize(2) // NOVERLAP + (CA)
        expectThat(elements[1]).isA<QueryElement.Pareto>()
        expectThat((elements[1] as QueryElement.Pareto).metrics).isEqualTo(listOf(CYCLES, AREA))
    }

    @Test
    fun `valid C{A=3}(BH)`() {
        val query = "C{A=3}(BH)"
        val elements = OmQL.parseQuery(query, possibleMetrics)
        expectThat(elements).hasSize(4) // NOVERLAP + C + {A=3} + (BH)
        expectThat(elements[1]).isA<QueryElement.Min>()
        expectThat((elements[1] as QueryElement.Min).metric).isEqualTo(CYCLES)
        expectThat(elements[2]).isA<QueryElement.Constraint>()
        expectThat((elements[2] as QueryElement.Constraint).metric.scoreParts).contains(AREA)
        expectThat(elements[3]).isA<QueryElement.Pareto>()
        expectThat((elements[3] as QueryElement.Pareto).metrics).isEqualTo(listOf(BOUNDING_HEX, HEIGHT))
    }

    @Test
    @DisplayName("valid {W<=3.5}")
    fun `non integer constraint`() {
        val query = "{W<=3.5}"
        val elements = OmQL.parseQuery(query, possibleMetrics)
        expectThat(elements).hasSize(2) // NOVERLAP + {W<=3.5}
        expectThat(elements[1]).isA<QueryElement.Constraint>()
        expectThat((elements[1] as QueryElement.Constraint).metric).isA<Computed<*>>()
        expectThat((elements[1] as QueryElement.Constraint).metric.displayName).isEqualTo("W<=3.5")
    }

    @Test
    @DisplayName("valid [C*A]")
    fun `valid square 1`() {
        val query = "[C*A]"
        val elements = OmQL.parseQuery(query, possibleMetrics)
        expectThat(elements).hasSize(2) // NOVERLAP + [C*A]
        expectThat(elements[1]).isA<QueryElement.Min>()
        expectThat((elements[1] as QueryElement.Min).metric).isA<Computed<*>>()
        expectThat((elements[1] as QueryElement.Min).metric.displayName).isEqualTo("C*A")
    }

    @Test
    @DisplayName("valid [C+[A*B]]")
    fun `valid square 2`() {
        val query = "[C+[A*B]]"
        val elements = OmQL.parseQuery(query, possibleMetrics)
        expectThat(elements).hasSize(2) // NOVERLAP + [C+A]
        expectThat(elements[1]).isA<QueryElement.Min>()
        expectThat((elements[1] as QueryElement.Min).metric).isA<Computed<*>>()
        expectThat((elements[1] as QueryElement.Min).metric.displayName).isEqualTo("C+[A*B]")
    }

    @Test
    @DisplayName("valid {[A*B]<=30}")
    fun `nested custom metric in constraint`() {
        val query = "{[A*B]<=30}"
        val elements = OmQL.parseQuery(query, possibleMetrics)
        expectThat(elements).hasSize(2) // NOVERLAP + {[A*B]<=30}
        expectThat(elements[1]).isA<QueryElement.Constraint>()
        expectThat((elements[1] as QueryElement.Constraint).metric).isA<Computed<*>>()
        expectThat((elements[1] as QueryElement.Constraint).metric.displayName).isEqualTo("[A*B]<=30")
    }

    @Test
    @DisplayName("valid ([[A*B]+C]HW)")
    fun `nested custom metric in pareto`() {
        val query = "([[A*B]+C]HW)"
        val elements = OmQL.parseQuery(query, possibleMetrics)
        expectThat(elements).hasSize(2) // NOVERLAP + ([C+[A*B]]HW)
        expectThat(elements[1]).isA<QueryElement.Pareto>()
        expectThat((elements[1] as QueryElement.Pareto).metrics[0]).isA<Computed<*>>()
        expectThat((elements[1] as QueryElement.Pareto).metrics[0].displayName).isEqualTo("[A*B]+C")
        expectThat((elements[1] as QueryElement.Pareto).metrics[1]).isEqualTo(HEIGHT)
        expectThat((elements[1] as QueryElement.Pareto).metrics[2]).isEqualTo(WIDTH)
    }

    @Test
    @DisplayName("throws [C*A")
    fun `throws missing square bracket`() {
        val query = "[C*A"
        expectThrows<IllegalArgumentException> {
            OmQL.parseQuery(query, possibleMetrics)
        }
    }

    @Test
    fun `throws (CA`() {
        val query = "(CA"
        expectThrows<IllegalArgumentException> {
            OmQL.parseQuery(query, possibleMetrics)
        }
    }

    @Test
    fun `throws U`() {
        val query = "U"
        expectThrows<IllegalArgumentException> {
            OmQL.parseQuery(query, possibleMetrics)
        }
    }
}
