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

import com.faendir.zachtronics.bot.om.model.MeasurePoint
import com.faendir.zachtronics.bot.om.model.OmMetric.*
import com.faendir.zachtronics.bot.om.model.OmMetrics
import com.faendir.zachtronics.bot.om.model.OmScore
import com.faendir.zachtronics.bot.utils.InfinInt.Companion.toInfinInt
import com.faendir.zachtronics.bot.utils.toLevelValue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import strikt.api.expectDoesNotThrow
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.contains
import strikt.assertions.containsExactly
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo

class OmQLTest {

    private val parser = OmQL(OmMetrics.userFacing(), MeasurePoint.VICTORY)

    @Test
    fun `valid C`() {
        val elements = parser.parseQuery("C")
        expectThat(elements).hasSize(2) // NOVERLAP + C
        expectThat(elements[0]).isA<OmQL.Constraint>()
            .get { metric }.isEqualTo(NOVERLAP)
        expectThat(elements[1]).isA<OmQL.Min>()
            .get { metric }.isEqualTo(CYCLES)
    }

    @Test
    fun `valid O`() {
        val elements = parser.parseQuery("O")
        expectThat(elements).hasSize(1) // OVERLAP
        expectThat(elements[0]).isA<OmQL.Min>()
            .get { metric }.isEqualTo(OVERLAP)
    }

    @Test
    fun `valid CA`() {
        val elements = parser.parseQuery("CA")
        expectThat(elements).hasSize(3) // NOVERLAP + C + A
        expectThat(elements[1]).isA<OmQL.Min>()
            .get { metric }.isEqualTo(CYCLES)
        expectThat(elements[2]).isA<OmQL.Min>()
            .get { metric }.isEqualTo(AREA)
    }

    @Test
    fun `valid {C=3}`() {
        val elements = parser.parseQuery("{C=3}")
        expectThat(elements).hasSize(2) // NOVERLAP + {C=3}
        expectThat(elements[1]).isA<OmQL.Constraint>()
            .get { metric.scoreParts }.contains(CYCLES)
    }

    @Test
    fun `valid (CA)`() {
        val elements = parser.parseQuery("(CA)")
        expectThat(elements).hasSize(2) // NOVERLAP + (CA)
        expectThat(elements[1]).isA<OmQL.Pareto>()
            .get { metrics }.containsExactly(CYCLES, AREA)
    }

    @Test
    @DisplayName("valid {W<=3.5}")
    fun `non integer constraint`() {
        val elements = parser.parseQuery("{W<=3.5}")
        expectThat(elements).hasSize(2) // NOVERLAP + {W<=3.5}
        expectThat(elements[1]).isA<OmQL.Constraint>()
            .get { metric }.isA<Computed<*>>()
            .get { displayName }.isEqualTo("W<=3.5")
    }

    @Test
    @DisplayName("valid [C*A]")
    fun `valid square 1`() {
        val elements = parser.parseQuery("[C*A]")
        expectThat(elements).hasSize(2) // NOVERLAP + [C*A]
        expectThat(elements[1]).isA<OmQL.Min>()
            .get { metric }.isA<Computed<*>>()
            .get { displayName }.isEqualTo("C*A")
    }

    @Test
    @DisplayName("valid [C+[A*B]]")
    fun `valid square 2`() {
        val elements = parser.parseQuery("[C+[A*B]]")
        expectThat(elements).hasSize(2) // NOVERLAP + [C+A]
        expectThat(elements[1]).isA<OmQL.Min>()
            .get { metric }.isA<Computed<*>>()
            .get { displayName }.isEqualTo("C+[A*B]")
    }

    @Test
    @DisplayName("valid {[A*B] <= 30}")
    fun `nested custom metric in constraint`() {
        val elements = parser.parseQuery("{[A*B] <= 30}")
        expectThat(elements).hasSize(2) // NOVERLAP + {[A*B]<=30}
        expectThat(elements[1]).isA<OmQL.Constraint>()
            .get { metric }.isA<Computed<*>>()
            .get { displayName }.isEqualTo("[A*B] <= 30")
    }

    @Test
    @DisplayName("valid ([[A*B]+C]HW)")
    fun `nested custom metric in pareto`() {
        val elements = parser.parseQuery("([[A*B]+C]HW)")
        expectThat(elements).hasSize(2) // NOVERLAP + ([C+[A*B]]HW)
        expectThat(elements[1]).isA<OmQL.Pareto>()
            .get { metrics }.and {
                get { elementAt(0) }.isA<Computed<*>>()
                    .get { displayName }.isEqualTo("[A*B]+C")
                get { elementAt(1) }.isEqualTo(HEIGHT)
                get { elementAt(2) }.isEqualTo(WIDTH)
            }
    }

    @Test
    fun `valid C{A=3}(B@V H@INF)`() {
        val elements = parser.parseQuery("C{A=3}(B@V H@INF)")
        expectThat(elements).hasSize(4) // NOVERLAP + C + {A=3} + (BH)
        expectThat(elements[1]).isA<OmQL.Min>()
            .get { metric }.isEqualTo(CYCLES)
        expectThat(elements[2]).isA<OmQL.Constraint>()
            .get { metric.scoreParts }.contains(AREA)
        expectThat(elements[3]).isA<OmQL.Pareto>()
            .get { metrics }.containsExactlyInAnyOrder(BOUNDING_HEX, HEIGHT_INF)
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = ["[0-C]", "(CGAI)", "[C]", "[!T]", "[ ! ! T]"])
    fun `valid stuff`(query: String) {
        expectDoesNotThrow {
            parser.parseQuery(query)
        }
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = ["[C*A", "(CA", "((CA)", "U", "{CG}", "}", "{C}"])
    fun `invalid stuff`(query: String) {
        expectThrows<IllegalArgumentException> {
            parser.parseQuery(query)
        }.subject.printStackTrace()
    }

    @ParameterizedTest(name = "[{0}]")
    @ValueSource(strings = ["", "*", "+", "+T", "!C", "C+"])
    fun `invalid expressions`(query: String) {
        expectThrows<IllegalArgumentException> {
            parser.parseQuery("[$query]")
        }.subject.printStackTrace()
    }

    @ParameterizedTest(name = "[{index}] input={0}, expected={1}")
    @CsvSource(
        // + - (higher: *, lower: <)
        "'2*3+5*7', '41'",
        "'2+3<5+7', 'true'",
        "'2*3-5*7', '-29'",
        "'2-3<5-7', 'false'",
        // * / % (higher: **, lower: +)
        "'2**3*5**2', '200'",
        "'2*3+5*7', '41'",
        "'10**2/5**1', '20'",
        "'10/2+5/1', '10'",
        "'10**2%6**1', '4'",
        "'10%3+5%2', '2'",
        // unary - and **
        "'-2**2', '-4'",
        // ** (lower: *)
        "'2**3*5**2', '200'",
        // ** right associativity
        "'2**3**2', '512'",
        // < > <= >= (higher: +, lower: =)
        "'2+3<5+7', 'true'",
        "'2<3=5<7', 'true'",
        "'2+3>5+7', 'false'",
        "'2>3=5>7', 'true'",
        "'2+3<=5+7', 'true'",
        "'2<=3=5<=7', 'true'",
        "'2+3>=5+7', 'false'",
        "'2>=3=5>=7', 'true'",
        // = != (higher: <, lower: &&)
        "'2<3=5<7', 'true'",
        "'true&&false=true&&true', 'false'",
        "'2<3!=5<7', 'false'",
        "'true&&false!=true&&true', 'true'",
        // left associativity
        "'2=3=false', 'true'",
        // && || (higher: =)
        "'true=true&&false=false', 'true'",
        "'false&&false||true', 'true'",
    )
    fun `custom metric operator precedence`(query: String, expected: String) {
        val allOnes = OmScore(
            1, 1, overlap = true, trackless = true,
            1, 1, 1, 1.0, 1,
            1.0, 1.toLevelValue(), 1.toInfinInt(), 1.0, 1.toInfinInt()
        )
        val elements = parser.parseQuery("[$query]")
        val customMetric = elements[1].metrics.first() as Custom<*>
        val result = customMetric.getValueFrom(allOnes)
        if (result is Double)
            expectThat(result).isEqualTo(expected.toDouble())
        else
            expectThat(result).isEqualTo(expected.toBoolean())
    }
}
