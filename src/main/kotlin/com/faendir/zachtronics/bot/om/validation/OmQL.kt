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
 * Metrics are identified by their display names (e.g., `C`, `G`, `S`, `A`, `L`, `I`), common aliases are supported.
 * The measure point can be appended to a metric name to disambiguate them or just because (e.g. `A@INF`, `C@V`)
 * If no overlap-related constraint or minimization is specified, `{O=false}` is added automatically.
 *
 * Examples:
 * - `CG{A<8}`: Among records with minimum Cost then minimum Cycles, filter for Area < 8.
 * - `(C Sum)`: Find the Pareto frontier of Cycles and Sum.
 * - `{[C+G]<500}A`: Among records where Cost + Cycles < 500, find the minimum Area.
 * - `[C*G]`: Find the record(s) with the minimum product of Cost and Cycles.
 */
internal class OmQL(possibleMetrics: List<OmMetric<*>>, private val measurePoint: MeasurePoint? = null) {
    companion object {
        private val TRUE = OmMetric.Constant("true", true)
        private val FALSE = OmMetric.Constant("false", false)

        private val NUMBER_FORMAT = NumberFormat.getInstance(Locale.ENGLISH)

        private val binaryOperatorTrie = Trie<Operator.Binary<*>>().apply {
            put("=", Operator.Binary.BinaryAny.EQ)
            put("==", Operator.Binary.BinaryAny.EQ)
            put("!=", Operator.Binary.BinaryAny.NE)
            put("&&", Operator.Binary.BinaryBoolean.AND)
            put("||", Operator.Binary.BinaryBoolean.OR)
            put("<", Operator.Binary.BinaryDouble.LT)
            put(">", Operator.Binary.BinaryDouble.GT)
            put("<=", Operator.Binary.BinaryDouble.LE)
            put(">=", Operator.Binary.BinaryDouble.GE)
            put("+", Operator.Binary.BinaryDouble.PLUS)
            put("-", Operator.Binary.BinaryDouble.MINUS)
            put("*", Operator.Binary.BinaryDouble.TIMES)
            put("/", Operator.Binary.BinaryDouble.DIV)
            put("%", Operator.Binary.BinaryDouble.REM)
            put("**", Operator.Binary.BinaryDouble.POW)
        }
    }

    private val metricTrie = Trie<OmMetric<*>>().apply {
        fun putAll(m: OmMetric<*>, name: String) {
            if (measurePoint == null || m.measurePoint == MeasurePoint.START || m.measurePoint == measurePoint)
                put(name, m)
            put(name + m.measurePoint.displayName, m)
            if (m.measurePoint == MeasurePoint.INFINITY)
                put("$name@INF", m)
        }

        for (m in possibleMetrics + TRUE + FALSE) {
            putAll(m, m.displayName)
            if (m is OmMetric.Value<*> && m.alias != null) {
                putAll(m, m.alias)
            }
        }
    }

    val OmMetric<*>.unambiguousName: String
        get() {
            val metrics = metricTrie.get(displayName)
            return when {
                metrics == null -> displayName
                metrics.size == 1 -> displayName
                metrics.size >= 2 -> displayName + measurePoint.displayName
                else -> throw IllegalArgumentException("Unknown metric: $displayName")
            }
        }


    internal sealed interface QueryElement {
        val metrics: Collection<OmMetric<*>>
        fun filter(records: Collection<OmMemoryRecord>): Collection<OmMemoryRecord>
    }

    inner class Min(val metric: OmMetric<*>) : QueryElement {
        override val metrics: Collection<OmMetric<*>>
            get() = listOf(metric)

        override fun filter(records: Collection<OmMemoryRecord>): List<OmMemoryRecord> {
            return records.allMinsWith(compareBy(metric.comparator) { it.score })
        }

        override fun toString() =
            metric.unambiguousName.run { if (length > 1) "[$this]" else this }
    }

    inner class Pareto(override val metrics: Collection<OmMetric<*>>) : QueryElement {
        override fun filter(records: Collection<OmMemoryRecord>): List<OmMemoryRecord> {
            return records.paretoFrontierWith(metrics.map { m -> compareBy(m.comparator) { it.score } })
        }

        override fun toString() =
            metrics.joinToString("", "(", ")") { it.unambiguousName.run { if (length > 1) "[$this]" else this } }
    }

    inner class Constraint(val metric: OmMetric<Boolean>) : QueryElement {
        override val metrics: Collection<OmMetric<*>>
            get() = listOf(metric)

        override fun filter(records: Collection<OmMemoryRecord>): List<OmMemoryRecord> {
            return records.filter { r -> metric.getValueFrom(r.score) ?: false }
        }

        override fun toString() = if (metric.displayName.isNotEmpty()) "{${metric.unambiguousName}}" else ""
    }

    internal fun parseQuery(query: String): List<QueryElement> {
        val elements = mutableListOf<QueryElement>()
        var idx = 0
        var paretoContext = 0

        val currMetrics = mutableListOf<OmMetric<*>>()
        fun unloadMetrics() {
            currMetrics.forEach { elements.add(Min(it)) }
            currMetrics.clear()
        }

        while (idx < query.length) {
            when (query[idx]) {
                ' ' -> {
                    idx++
                }

                '{' -> { // {X=val} or {[bool expr]}
                    unloadMetrics()
                    val (foundMetric, end) = parseCustomMetric(query, idx + 1, '}')
                    elements.add(Constraint(foundMetric.asComputed<Boolean>(context = "Constraint")))
                    idx = end
                }

                '(' -> { // (AB) as pareto
                    unloadMetrics()
                    paretoContext++
                    idx++
                }

                ')' -> {
                    if (paretoContext != 1)
                        throw IllegalArgumentException("Missing/extra opening ( in query: $query")
                    paretoContext--
                    elements.add(Pareto(currMetrics.toList()))
                    currMetrics.clear()
                    idx++
                }

                else -> { // XYZ or [A*B] as computed metric
                    val (foundMetric, end) = parseMetric(query, idx)
                    currMetrics.add(foundMetric)
                    idx = end
                }
            }
        }
        if (paretoContext != 0)
            throw IllegalArgumentException("Missing closing ) in query: $query")
        unloadMetrics()

        if (elements.none { qe -> OmMetric.OVERLAP in qe.metrics.flatMap { it.scoreParts } }) {
            elements.addFirst(Constraint(OmMetric.NOVERLAP))
        }
        return elements
    }

    private fun parseMetric(query: String, idx: Int): Pair<OmMetric<*>, Int> {
        var idx = idx
        while (query[idx] == ' ')
            idx++
        if (query[idx] == '[') {
            return parseCustomMetric(query, idx + 1, ']')
        }
        return parseSingleMetric(query, idx)
    }

    private fun parseSingleMetric(query: String, idx: Int): Pair<OmMetric<*>, Int> {
        var idx = idx
        while (query[idx] == ' ')
            idx++
        val (metrics, end) = metricTrie.findLongestPrefix(query, idx)
        when (metrics.size) {
            0 -> {}
            1 -> return metrics[0] to end
            else -> throw IllegalArgumentException("Ambiguous metric: ${query.substring(idx, end)}")
        }
        val pp = ParsePosition(idx)
        val number = NUMBER_FORMAT.parse(query, pp)
        if (number != null) {
            return OmMetric.Constant(number.toString(), number.toDouble()) to pp.index
        }
        throw IllegalArgumentException("Unknown metric(s): ${query.substring(idx)}")
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
        query: String, startIdx: Int, boundary: Char
    ): Pair<OmMetric.Computed<*>, Int> {
        var idx = startIdx
        val operators = mutableListOf<Operator>()
        val metrics = mutableListOf<OmMetric<*>>()

        var allowedTokens = EnumSet.of(Token.METRIC, Token.UNARY_OP)

        while (true) {
            if (query[idx] == ' ') {
                idx++
                continue
            }
            if (allowedTokens.contains(Token.UNARY_OP)) {
                val op = when (query[idx]) {
                    '+' -> Operator.Unary.UnaryDouble.PLUS
                    '-' -> Operator.Unary.UnaryDouble.MINUS
                    '!' -> Operator.Unary.UnaryBoolean.NOT
                    else -> null
                }
                if (op != null) {
                    idx += 1
                    operators.add(op)
                    allowedTokens = EnumSet.of(Token.METRIC, Token.UNARY_OP)
                    continue
                }
            }
            if (allowedTokens.contains(Token.BINARY_OP)) {
                val (ops, end) = binaryOperatorTrie.findLongestPrefix(query, idx)
                when (ops.size) {
                    0 -> {}
                    1 -> {
                        idx = end
                        operators.add(ops[0])
                        allowedTokens = EnumSet.of(Token.METRIC, Token.UNARY_OP)
                        continue
                    }
                    else -> throw IllegalArgumentException("Ambiguous operator: ${query.substring(idx, end)}")
                }
            }
            if (allowedTokens.contains(Token.METRIC)) {
                // either works or we throw
                val (metric, end) = parseMetric(query, idx)
                idx = end
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
                            is Operator.Binary.BinaryAny -> {
                                if (res::class == next::class)
                                    op.func(res, next)
                                else // the only funnel conversion is to double, hope for the best
                                    op.func(res.doubleOrBust(context), next.doubleOrBust(context))
                            }
                        }
                    }
                }
            }
            res as Comparable<Any>
        }
        // if it throws it throws here
        expression(allOnes)

        val descr = query.substring(startIdx, idx)
        return OmMetric.Custom(
            subMetrics = metrics.toTypedArray(),
            description = descr,
            displayName = descr,
            getValueFrom = expression,
        ) to idx + 1
    }

    private fun Comparable<*>.booleanOrBust(context: String? = null): Boolean {
        return when (this) {
            is Boolean -> this
            else -> throw IllegalArgumentException("Invalid type: ${this::class.simpleName}" + (context?.let { " for $it" } ?: ""))
        }
    }

    private fun Comparable<*>.doubleOrBust(context: String? = null): Double {
        return when (this) {
            is Number -> toDouble()
            is InfinInt -> toDouble()
            is LevelValue -> 100_000.0.pow(level) * value
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
