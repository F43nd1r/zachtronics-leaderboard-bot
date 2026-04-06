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

import com.faendir.zachtronics.bot.om.model.OmMetric
import com.faendir.zachtronics.bot.om.model.OmScore
import com.faendir.zachtronics.bot.om.repository.OmMemoryRecord
import com.faendir.zachtronics.bot.utils.InfinInt
import com.faendir.zachtronics.bot.utils.LevelValue
import com.faendir.zachtronics.bot.utils.allMinsWith
import com.faendir.zachtronics.bot.utils.paretoFrontierWith
import kotlin.math.pow
import kotlin.reflect.KType
import kotlin.reflect.jvm.ExperimentalReflectionOnLambdas
import kotlin.reflect.jvm.reflect


/**
 * A query language for filtering and selecting Opus Magnum records.
 *
 * Syntax:
 * - `X`: Select records with the minimum value of metric `X`.
 * - `(XYZ)`: Select records on the Pareto frontier of metrics `X`, `Y`, and `Z`.
 * - `{X=val}`: Filter records where metric `X` equals `val`. Supports `==`, `!=`, `<`, `>`, `<=`, `>=`, `&&`.
 * - `[A*B]`: Define a computed metric. Can be used inside other constructs, e.g., `{[A+B]<100}` or `[A*B]` (to minimize the product).
 * - `true`/`false`: Boolean constants.
 * - `123`: Numeric constants.
 *
 * Metrics are identified by their display names (e.g., `C`, `G`, `S`, `A`, `L`, `I`).
 * If no overlap-related constraint or minimization is specified, `{O=false}` is added automatically.
 *
 * Examples:
 * - `CG{A<3}`: Among records with Area < 3, find the minimum Cost, then minimum Cycles.
 * - `(CSum)`: Find the Pareto frontier of Cycles and Sum.
 * - `{[C+G]<500}A`: Among records where Cost + Cycles < 500, find the minimum Area.
 * - `[C*G]`: Find the record(s) with the minimum product of Cost and Cycles.
 */
internal object OmQL {

    internal sealed interface QueryElement {
        val metrics: Collection<OmMetric<*>>
        fun filter(records: Collection<OmMemoryRecord>): Collection<OmMemoryRecord>

        class Min(val metric: OmMetric<*>) : QueryElement {
            override val metrics: Collection<OmMetric<*>>
                get() = listOf(metric)

            override fun filter(records: Collection<OmMemoryRecord>): List<OmMemoryRecord> {
                return records.allMinsWith(compareBy(metric.comparator) { it.record.score })
            }

            override fun toString() =
                if (metric.displayName.length <= 1) metric.displayName else "[${metric.description}]"
        }

        class Pareto(override val metrics: List<OmMetric<*>>) : QueryElement {
            override fun filter(records: Collection<OmMemoryRecord>): List<OmMemoryRecord> {
                return records.paretoFrontierWith(metrics.map { m -> compareBy(m.comparator) { it.record.score } })
            }

            override fun toString() = metrics.joinToString("", "(", ")") { it.displayName }
        }

        class Constraint(val metric: OmMetric<*>) : QueryElement {
            override val metrics: Collection<OmMetric<*>>
                get() = listOf(metric)

            override fun filter(records: Collection<OmMemoryRecord>): List<OmMemoryRecord> {
                return records.filter { r -> metric.getValueAsDouble(r.record.score)?.let { it != 0.0 } ?: false }
            }

            override fun toString() = if (metric.description.isNotEmpty()) "{${metric.description}}" else ""
        }
    }

    internal fun parseQuery(query: String, possibleMetrics: List<OmMetric<*>>): List<QueryElement> {
        val elements = mutableListOf<QueryElement>()
        var idx = 0
        val stack = ArrayDeque<Char>() // contains close brackets

        val currMetrics = mutableListOf<OmMetric<*>>()
        fun unloadMetrics() {
            currMetrics.forEach { elements.add(QueryElement.Min(it)) }
            currMetrics.clear()
        }

        while (idx < query.length) {
            when (query[idx]) {
                '{' -> { // {X=val} or {X <= val}
                    unloadMetrics()
                    stack.addLast('}')
                    idx++
                }

                '}' -> {
                    if (stack.lastOrNull() != '}')
                        throw IllegalArgumentException("Missing opening } in query: $query")
                    stack.removeLast()
                    currMetrics.forEach { elements.add(QueryElement.Constraint(it)) }
                    currMetrics.clear()
                    idx++
                }

                '(' -> { // (AB) as pareto
                    unloadMetrics()
                    stack.addLast(')')
                    idx++
                }

                ')' -> {
                    if (stack.lastOrNull() != ')')
                        throw IllegalArgumentException("Missing opening ) in query: $query")
                    stack.removeLast()
                    elements.add(QueryElement.Pareto(currMetrics.toList()))
                    currMetrics.clear()
                    idx++
                }

                else -> { // XYZ or [A*B] as computed metric
                    val (foundMetric, end) = parseMetric(query.substring(idx), possibleMetrics, stack)
                    currMetrics.add(foundMetric)
                    idx += end
                }
            }
        }
        if (stack.isNotEmpty())
            throw IllegalArgumentException("Missing closing ${stack.joinToString("")} in query: $query")
        unloadMetrics()

        if (elements.none { qe -> OmMetric.OVERLAP in qe.metrics.flatMap { it.scoreParts } }) {
            elements.addFirst(QueryElement.Constraint(OmMetric.NOVERLAP))
        }
        return elements
    }

    private fun parseMetric(
        query: String, possibleMetrics: List<OmMetric<*>>, stack: ArrayDeque<Char>
    ): Pair<OmMetric<*>, Int> {
        val last = stack.lastOrNull()
        if (last == ']' || last == '}') {
            var (m, end) = parseCustomMetric(query, possibleMetrics, last)
            if (last == ']') {
                stack.removeLast()
                end++
            }
            return m to end
        }
        if (query[0] == '[') {
            stack.addLast(']')
            return parseMetric(query.substring(1), possibleMetrics, stack).let { (m, s) -> m to s + 1 }
        }
        return parseSingleMetric(query, possibleMetrics)
    }

    private fun parseSingleMetric(
        query: String, possibleMetrics: List<OmMetric<*>>
    ): Pair<OmMetric<*>, Int> {
        if (query.startsWith("false", ignoreCase = true)) {
            return OmMetric.Constant("false", false) to "false".length
        }
        if (query.startsWith("true", ignoreCase = true)) {
            return OmMetric.Constant("true", true) to "true".length
        }
        val metric = possibleMetrics.firstOrNull { query.startsWith(it.displayName, ignoreCase = true) }
        if (metric != null) {
            return metric to metric.displayName.length
        }
        val match = Regex("""^[-+\d.]+""").find(query)
        if (match != null) {
            return OmMetric.Constant(match.value, match.value.toDouble()) to match.range.last + 1
        }
        throw IllegalArgumentException("Unknown metric(s): $query")
    }

    private fun parseCustomMetric(
        query: String, possibleMetrics: List<OmMetric<*>>, boundary: Char
    ): Pair<OmMetric.Computed<Double>, Int> {
        var idx = 0
        val metrics = mutableListOf<OmMetric<*>>()
        val operators = mutableListOf<(Double, Double) -> Double>()
        while (idx < query.length) {
            val (metric, size) = parseMetric(query.substring(idx), possibleMetrics, ArrayDeque())
            idx += size
            metrics.add(metric)
            if (idx >= query.length) throw IllegalArgumentException("Missing closing $boundary in query: $query")
            if (query[idx] == boundary) break

            fun load(op: (Double, Double) -> Double, size: Int) {
                operators.add(op)
                idx += size
            }
            when {
                query.startsWith("==", idx) -> load({ a, b -> if (a == b) 1.0 else 0.0 }, 2)
                query.startsWith("!=", idx) -> load({ a, b -> if (a != b) 1.0 else 0.0 }, 2)
                query.startsWith("<=", idx) -> load({ a, b -> if (a <= b) 1.0 else 0.0 }, 2)
                query.startsWith(">=", idx) -> load({ a, b -> if (a >= b) 1.0 else 0.0 }, 2)
                query.startsWith("&&", idx) -> load({ a, b -> if (a != 0.0 && b != 0.0) 1.0 else 0.0 }, 2)
                query.startsWith("=", idx) -> load({ a, b -> if (a == b) 1.0 else 0.0 }, 1)
                query.startsWith("<", idx) -> load({ a, b -> if (a < b) 1.0 else 0.0 }, 1)
                query.startsWith(">", idx) -> load({ a, b -> if (a > b) 1.0 else 0.0 }, 1)
                query.startsWith("+", idx) -> load(Double::plus, 1)
                query.startsWith("-", idx) -> load(Double::minus, 1)
                query.startsWith("*", idx) -> load(Double::times, 1)
                query.startsWith("/", idx) -> load(Double::div, 1)
                else -> throw IllegalArgumentException("Invalid operator")
            }
        }

        val descr = query.substring(0, idx)
        return object : OmMetric.Computed<Double> {
            override val subMetrics: Array<out OmMetric<*>> = metrics.toTypedArray()
            override val description = descr
            override val getValueFrom: (OmScore) -> Double? = l@{ s ->
                var ret = metrics[0].getValueAsDouble(s) ?: return@l null
                for (i in operators.indices) {
                    val next = metrics[i + 1].getValueAsDouble(s) ?: return@l null
                    ret = operators[i](ret, next)
                }
                ret
            }
            override val displayName = descr
        } to idx
    }

    private fun OmMetric<*>.getValueAsDouble(score: OmScore): Double? {
        val value = getValueFrom(score) ?: return null
        return when (value) {
            is Boolean -> if (value) 1.0 else 0.0
            is Number -> value.toDouble()
            is InfinInt -> value.toDouble()
            is LevelValue -> value.level * 10.0.pow(5) + value.value
            else -> throw IllegalArgumentException("Wat is $value")
        }
    }

    @OptIn(ExperimentalReflectionOnLambdas::class)
    private val OmMetric<*>.type: KType
        get() = getValueFrom.reflect()!!.returnType
}
