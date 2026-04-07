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
import com.faendir.zachtronics.bot.utils.*
import com.faendir.zachtronics.bot.utils.InfinInt.Companion.toInfinInt
import java.text.NumberFormat
import java.text.ParsePosition
import java.util.*
import kotlin.math.pow


/**
 * A query language for filtering and selecting Opus Magnum records.
 *
 * Syntax:
 * - `X`: Select records with the minimum value of metric `X`.
 * - `(XYZ)`: Select records on the Pareto frontier of metrics `X`, `Y`, and `Z`.
 * - `{X=val}`: Filter records where metric `X` equals `val`. Supports `==`, `!=`, `<`, `>`, `<=`, `>=`, `&&`.
 * - `[A*B]`: Define a custom metric. Can be used inside other constructs, e.g., `{[A+B]<100}` or `[A*B]` (to minimize the product).
 * - `true`/`false`: Boolean constants.
 * - `123`: Numeric constants.
 *
 * Metrics are identified by their display names (e.g., `C`, `G`, `S`, `A`, `L`, `I`).
 * If no overlap-related constraint or minimization is specified, `{O=false}` is added automatically.
 *
 * Examples:
 * - `CG{A<8}`: Among records with minimum Cost then minimum Cycles, filter for Area < 8.
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
                if (metric.displayName.length <= 1) metric.displayName else "[${metric.displayName}]"
        }

        class Pareto(override val metrics: List<OmMetric<*>>) : QueryElement {
            override fun filter(records: Collection<OmMemoryRecord>): List<OmMemoryRecord> {
                return records.paretoFrontierWith(metrics.map { m -> compareBy(m.comparator) { it.record.score } })
            }

            override fun toString() =
                metrics.joinToString("", "(", ")") { it.displayName.runIf(it is OmMetric.Computed<*>) { "[$this]" } }
        }

        class Constraint(val metric: OmMetric<Boolean>) : QueryElement {
            override val metrics: Collection<OmMetric<*>>
                get() = listOf(metric)

            override fun filter(records: Collection<OmMemoryRecord>): List<OmMemoryRecord> {
                return records.filter { r -> metric.getValueFrom(r.record.score) ?: false }
            }

            override fun toString() = if (metric.description.isNotEmpty()) "{${metric.description}}" else ""
        }
    }

    internal fun parseQuery(query: String, possibleMetrics: List<OmMetric<*>>): List<QueryElement> {
        val elements = mutableListOf<QueryElement>()
        var idx = 0
        var inPareto = false

        val currMetrics = mutableListOf<OmMetric<*>>()
        fun unloadMetrics() {
            currMetrics.forEach { elements.add(QueryElement.Min(it)) }
            currMetrics.clear()
        }

        while (idx < query.length) {
            when (query[idx]) {
                '{' -> { // {X=val} or {X <= val}
                    unloadMetrics()
                    val (foundMetric, size) = parseCustomMetric(query, idx + 1, '}', possibleMetrics)
                    elements.add(QueryElement.Constraint(foundMetric.asComputed<Boolean>(context = "Constraint")))
                    idx += size + 2
                }

                '(' -> { // (AB) as pareto
                    unloadMetrics()
                    inPareto = true
                    idx++
                }

                ')' -> {
                    if (!inPareto)
                        throw IllegalArgumentException("Missing opening ) in query: $query")
                    inPareto = false
                    elements.add(QueryElement.Pareto(currMetrics.toList()))
                    currMetrics.clear()
                    idx++
                }

                else -> { // XYZ or [A*B] as computed metric
                    val (foundMetric, size) = parseMetric(query, idx, possibleMetrics)
                    currMetrics.add(foundMetric)
                    idx += size
                }
            }
        }
        if (inPareto)
            throw IllegalArgumentException("Missing closing ) in query: $query")
        unloadMetrics()

        if (elements.none { qe -> OmMetric.OVERLAP in qe.metrics.flatMap { it.scoreParts } }) {
            elements.addFirst(QueryElement.Constraint(OmMetric.NOVERLAP))
        }
        return elements
    }

    private fun parseMetric(
        query: String, idx: Int, possibleMetrics: List<OmMetric<*>>
    ): Pair<OmMetric<*>, Int> {
        if (query[idx] == '[') {
            return parseCustomMetric(query, idx + 1, ']', possibleMetrics).let { (m, s) -> m to s + 2 }
        }
        return parseSingleMetric(query.substring(idx), possibleMetrics)
    }

    private val TRUE = OmMetric.Constant("true", true)
    private val FALSE = OmMetric.Constant("false", false)
    private val NUMBER_FORMAT = NumberFormat.getInstance(Locale.ENGLISH)

    private fun parseSingleMetric(
        query: String, possibleMetrics: List<OmMetric<*>>
    ): Pair<OmMetric<*>, Int> {
        if (query.startsWith("false", ignoreCase = true)) {
            return FALSE to "false".length
        }
        if (query.startsWith("true", ignoreCase = true)) {
            return TRUE to "true".length
        }
        val metric = possibleMetrics.firstOrNull { query.startsWith(it.displayName, ignoreCase = true) }
        if (metric != null) {
            return metric to metric.displayName.length
        }
        val pp = ParsePosition(0)
        val number = NUMBER_FORMAT.parse(query, pp)
        if (number != null) {
            return OmMetric.Constant(number.toString(), number.toDouble()) to pp.index
        }
        throw IllegalArgumentException("Unknown metric(s): $query")
    }

    private sealed interface Operator {
        sealed interface Unary<T : Comparable<T>> : Operator {
            val func: (T) -> T

            sealed class UnaryBoolean(override val func: (Boolean) -> Boolean) : Unary<Boolean> {
                data object NOT : UnaryBoolean(Boolean::not)
            }

            sealed class UnaryDouble(override val func: (Double) -> Double) : Unary<Double> {
                data object PLUS : UnaryDouble(Double::unaryPlus)
                data object MINUS : UnaryDouble(Double::unaryMinus)
            }
        }

        sealed interface Binary<I> : Operator {
            val func: (I, I) -> Comparable<*>

            sealed class BinaryAny(override val func: (Any, Any) -> Boolean) : Binary<Any> {
                data object EQ : BinaryAny(Any::equals)
                data object NE : BinaryAny({ a, b -> a != b })
            }

            sealed class BinaryBoolean(override val func: (Boolean, Boolean) -> Boolean) : Binary<Boolean> {
                data object AND : BinaryBoolean(Boolean::and)
                data object OR : BinaryBoolean(Boolean::or)
            }

            sealed class BinaryDouble(override val func: (Double, Double) -> Comparable<*>) : Binary<Double> {
                data object LT : BinaryDouble({ a, b -> a < b })
                data object GT : BinaryDouble({ a, b -> a > b })
                data object LE : BinaryDouble({ a, b -> a <= b })
                data object GE : BinaryDouble({ a, b -> a >= b })

                data object PLUS : BinaryDouble(Double::plus)
                data object MINUS : BinaryDouble(Double::minus)
                data object TIMES : BinaryDouble(Double::times)
                data object DIV : BinaryDouble(Double::div)
                data object REM : BinaryDouble(Double::rem)
                data object POW : BinaryDouble(Double::pow)
            }
        }
    }

    private enum class Token {
        METRIC, UNARY_OP, BINARY_OP,
    }

    private fun parseCustomMetric(
        query: String, startIdx: Int, boundary: Char, possibleMetrics: List<OmMetric<*>>
    ): Pair<OmMetric.Computed<*>, Int> {
        var idx = startIdx
        val operators = mutableListOf<Operator>()
        val metrics = mutableListOf<OmMetric<*>>()

        fun tryReadUnary(): Boolean {
            val op = when (query[idx]) {
                '+' -> Operator.Unary.UnaryDouble.PLUS
                '-' -> Operator.Unary.UnaryDouble.MINUS
                '!' -> Operator.Unary.UnaryBoolean.NOT
                else -> return false
            }
            idx += 1
            operators.add(op)
            return true
        }

        fun tryReadBinary(): Boolean {
            val op = when {
                query.startsWith("==", idx) -> { idx += 2; Operator.Binary.BinaryAny.EQ }
                query.startsWith("!=", idx) -> { idx += 2; Operator.Binary.BinaryAny.NE }
                query.startsWith("<=", idx) -> { idx += 2; Operator.Binary.BinaryDouble.LE }
                query.startsWith(">=", idx) -> { idx += 2; Operator.Binary.BinaryDouble.GE }
                query.startsWith("**", idx) -> { idx += 2; Operator.Binary.BinaryDouble.POW }
                query.startsWith("&&", idx) -> { idx += 2; Operator.Binary.BinaryBoolean.AND }
                query.startsWith("||", idx) -> { idx += 2; Operator.Binary.BinaryBoolean.OR }
                query.startsWith("=", idx) -> { idx += 1; Operator.Binary.BinaryAny.EQ }
                query.startsWith("<", idx) -> { idx += 1; Operator.Binary.BinaryDouble.LT }
                query.startsWith(">", idx) -> { idx += 1; Operator.Binary.BinaryDouble.GT }
                query.startsWith("+", idx) -> { idx += 1; Operator.Binary.BinaryDouble.PLUS }
                query.startsWith("-", idx) -> { idx += 1; Operator.Binary.BinaryDouble.MINUS }
                query.startsWith("*", idx) -> { idx += 1; Operator.Binary.BinaryDouble.TIMES }
                query.startsWith("/", idx) -> { idx += 1; Operator.Binary.BinaryDouble.DIV }
                query.startsWith("%", idx) -> { idx += 1; Operator.Binary.BinaryDouble.REM }
                else -> return false
            }
            operators.add(op)
            return true
        }

        var allowedTokens = EnumSet.of(Token.METRIC, Token.UNARY_OP)

        while (true) {
            if (allowedTokens.contains(Token.UNARY_OP)) {
                if (tryReadUnary()) {
                    allowedTokens = EnumSet.of(Token.METRIC, Token.UNARY_OP)
                    continue
                }
            }
            if (allowedTokens.contains(Token.BINARY_OP)) {
                if (tryReadBinary()) {
                    allowedTokens = EnumSet.of(Token.METRIC, Token.UNARY_OP)
                    continue
                }
            }
            if (allowedTokens.contains(Token.METRIC)) {
                // either works or we throw
                val (metric, size) = parseMetric(query, idx, possibleMetrics)
                idx += size
                metrics.add(metric)
                allowedTokens = EnumSet.of(Token.BINARY_OP)
                if (idx >= query.length)
                    throw IllegalArgumentException("Missing closing $boundary in query: $query")
                if (query[idx] == boundary)
                    break
                continue
            }
            throw IllegalArgumentException("Unknown token in query: $query")
        }

        @Suppress("UNCHECKED_CAST")
        val expression: (OmScore) -> Comparable<Any>? = l@{ s ->
            val metricIt = metrics.iterator()
            var res = metricIt.next().getValueFrom(s) ?: return@l null
            for (op in operators) {
                val context = op::class.simpleName
                res = when (op) {
                    is Operator.Unary.UnaryBoolean -> op.func(res.booleanOrBust(context))
                    is Operator.Unary.UnaryDouble -> op.func(res.doubleOrBust(context))
                    is Operator.Binary<*> -> {
                        val next = metricIt.next().getValueFrom(s) ?: return@l null
                        when (op) {
                            is Operator.Binary.BinaryBoolean -> op.func(res.booleanOrBust(context), next.booleanOrBust(context))
                            is Operator.Binary.BinaryDouble -> op.func(res.doubleOrBust(context), next.doubleOrBust(context))
                            is Operator.Binary.BinaryAny -> op.func(res, next)
                        }
                    }
                }
            }
            res as Comparable<Any>
        }
        // if it throws it throws here
        expression(allOnes)

        val descr = query.substring(startIdx, idx)
        return object : OmMetric.Computed<Comparable<Any>> {
            override val subMetrics: Array<out OmMetric<*>> = metrics.toTypedArray()
            override val description = descr
            override val displayName = descr
            override val getValueFrom = expression
        } to idx - startIdx
    }

    private fun Any.booleanOrBust(context: String? = null): Boolean {
        return when (this) {
            is Boolean -> this
            else -> throw IllegalArgumentException("Invalid type: ${this::class.simpleName}" + (context?.let { " for $it" } ?: ""))
        }
    }

    private fun Any.doubleOrBust(context: String? = null): Double {
        return when (this) {
            is Number -> toDouble()
            is InfinInt -> toDouble()
            is LevelValue -> 10.0.pow(5 * level) + value
            else -> throw IllegalArgumentException("Invalid type: ${this::class.simpleName}" + (context?.let { " for $it" } ?: ""))
        }
    }

    private val allOnes = OmScore(
        1, 1, overlap = true, trackless = true,
        1, 1, 1, 1.0, 1,
        1.0, 1.toLevelValue(), 1.toInfinInt(), 1.0, 1.toInfinInt()
    )

    private inline fun <reified T : Comparable<T>> OmMetric.Computed<*>.asComputed(context: String? = null): OmMetric.Computed<T> {
        @Suppress("UNCHECKED_CAST")
        when (val v = getValueFrom(allOnes)!!) {
            is T -> return this as OmMetric.Computed<T>
            else -> throw IllegalArgumentException("Invalid type: ${v::class.simpleName}" + (context?.let { " for $it" } ?: ""))
        }
    }
}
