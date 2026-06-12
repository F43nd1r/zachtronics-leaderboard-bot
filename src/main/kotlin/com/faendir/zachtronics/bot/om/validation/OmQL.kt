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
import com.faendir.zachtronics.bot.utils.InfinInt
import com.faendir.zachtronics.bot.utils.InfinInt.Companion.toInfinInt
import com.faendir.zachtronics.bot.utils.LevelValue
import com.faendir.zachtronics.bot.utils.Trie
import com.faendir.zachtronics.bot.utils.allMinsWith
import com.faendir.zachtronics.bot.utils.paretoFrontierWith
import com.faendir.zachtronics.bot.utils.toLevelValue
import java.text.NumberFormat
import java.text.ParsePosition
import java.util.*
import java.util.Collections.emptyMap
import kotlin.math.pow


/**
 * OmQL, the overengineered query language for filtering and selecting Opus Magnum records.
 *
 * **Elements syntax:**
 * - `X`: Select records with the minimum value of metric `X`.
 * - `(XYZ)`: Select records on the Pareto frontier of metrics `X`, `Y`, and `Z`.
 * - `{X=val}`: Select records where the inner expression is true, in this case if metric `X` equals `val`.
 *
 * If no Overlap-related constraint or minimization is specified, `{O=false}` is added automatically.
 *
 * **Metrics syntax:**
 * - `C`/`C@V`/`G@0`/`R@INF`/`B@∞`: Native metrics (see below).
 * - `[A*B]`: Define a custom metric. Can be used anywhere a native metric could, e.g. `{[A+B]<100}` or `[A*B]`.
 * - `"omsim expression"`: Call any valid [Omsim metric](https://events.critelli.technology/static/metrics.html),
 *                         with some restrictions to avoid excessive computation.
 * - `true`/`false`: Boolean constants.
 * - `123`: Numeric constants.
 * - `null`: Value of untracked metrics, like B in production.
 *
 * Metrics are identified by their display names (e.g., `C`, `G`, `A`, `R`, `L`, `I`), common aliases are supported.
 * The measure point can be appended to a metric name to disambiguate them or just because (e.g. `A@INF`, `C@V`)
 * Custom metrics and expressions support the following operators, precedence can be overrided by nesting `[]`:
 * - Logical: `&&`, `||`, `!`
 * - Comparison: `=`/`==`, `!=`, `<`, `>`, `<=`, `>=`
 * - Arithmetic: `+`, `-`, `*`, `/`, `%`, `**`
 * - Functions: `abs/log/exp(x)`, `min/max(a,b)`
 *
 * Examples:
 * - `CG{A<8}`: Among records with minimum Cost then minimum Cycles, filter for Area < 8.
 * - `(C Sum)`: Find the Pareto frontier of Cycles and Sum.
 * - `{[C+G]<500}A`: Among records where Cost + Cycles < 500, find the minimum Area.
 * - `[C*G]`: Find the record(s) with the minimum product of Cost and Cycles.
 */
internal class OmQL(possibleMetrics: List<OmMetric<*>>, measurePoint: MeasurePoint? = null) {
    companion object {
        private val TRUE = OmMetric.Constant("true", true)
        private val FALSE = OmMetric.Constant("false", false)
        private val NULL = OmMetric.Constant<Comparable<Any>?>("null", null)

        private val NUMBER_FORMAT = NumberFormat.getInstance(Locale.ENGLISH)

        private val binaryOperatorTrie = Trie<Callable.Binary<*>>().apply {
            put("=", Callable.Binary.BinaryAny.EQ)
            put("==", Callable.Binary.BinaryAny.EQ)
            put("!=", Callable.Binary.BinaryAny.NE)
            put("&&", Callable.Binary.BinaryBoolean.AND)
            put("||", Callable.Binary.BinaryBoolean.OR)
            put("<", Callable.Binary.BinaryDouble.LT)
            put(">", Callable.Binary.BinaryDouble.GT)
            put("<=", Callable.Binary.BinaryDouble.LE)
            put(">=", Callable.Binary.BinaryDouble.GE)
            put("+", Callable.Binary.BinaryDouble.PLUS)
            put("-", Callable.Binary.BinaryDouble.MINUS)
            put("*", Callable.Binary.BinaryDouble.TIMES)
            put("/", Callable.Binary.BinaryDouble.DIV)
            put("%", Callable.Binary.BinaryDouble.REM)
            put("**", Callable.Binary.BinaryDouble.POW)
        }

        private val allOnesMap = object : MutableMap<String, Double> by emptyMap() {
            override val size = Int.MAX_VALUE
            override fun get(key: String): Double = 1.0
        }

        val allOnes = OmScore(
            1, 1, overlap = true, trackless = true,
            1, 1, 1, 1.0, 1,
            1.0, 1.toLevelValue(), 1.toInfinInt(), 1.0, 1.toInfinInt(),
            extraKnownMetrics = allOnesMap
        )
    }

    private val metricTrie = Trie<OmMetric<*>>().apply {
        fun putAll(m: OmMetric<*>, name: String) {
            if (measurePoint == null || m.measurePoint == MeasurePoint.START || m.measurePoint == measurePoint)
                put(name, m)
            put(name + m.measurePoint.displayName, m)
            if (m.measurePoint == MeasurePoint.INFINITY)
                put("$name@INF", m)
        }

        for (m in possibleMetrics + listOf(TRUE, FALSE, NULL)) {
            putAll(m, m.displayName)
            if (m is OmMetric.Value<*> && m.alias != null) {
                putAll(m, m.alias)
            }
        }
    }

    val OmMetric<*>.unambiguousName: String
        get() {
            if (this is OmMetric.Custom<*> || this is OmMetric.Constant || this is OmMetric.Omsim)
                return displayName
            val metrics = metricTrie[displayName]
            return when (metrics.size) {
                0 -> throw IllegalArgumentException("Unknown metric: $displayName")
                1 -> displayName
                else -> displayName + measurePoint.displayName
            }
        }


    internal sealed interface QueryElement {
        val metrics: Collection<OmMetric<*>>
        val scoreParts: Set<OmMetric.ScorePart<*>>
            get() = metrics.flatMapTo(HashSet()) { it.scoreParts }

        fun filter(records: Collection<OmMemoryRecord>): Collection<OmMemoryRecord>
    }

    inner class Min(val metric: OmMetric<*>) : QueryElement {
        override val metrics: Collection<OmMetric<*>>
            get() = listOf(metric)

        override fun filter(records: Collection<OmMemoryRecord>): List<OmMemoryRecord> {
            return records.allMinsWith(compareBy(metric) { it.score })
        }

        override fun toString() =
            metric.unambiguousName.run { if (length > 1) "[$this]" else this }
    }

    inner class Pareto(override val metrics: Collection<OmMetric<*>>) : QueryElement {
        override fun filter(records: Collection<OmMemoryRecord>): List<OmMemoryRecord> {
            return records.paretoFrontierWith(metrics.map { m -> compareBy(m) { it.score } })
        }

        override fun toString() =
            metrics.joinToString("", "(", ")") { it.unambiguousName.run { if (length > 1) "[$this]" else this } }
    }

    inner class Constraint(val metric: OmMetric<Boolean?>) : QueryElement {
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
                    elements.add(Constraint(foundMetric.asCustom<Boolean>(context = "Constraint")))
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
                else -> { // XYZ or [A*B] as custom metric
                    val (foundMetric, end) = parseMetric(query, idx)
                    currMetrics.add(foundMetric)
                    idx = end
                }
            }
        }
        if (paretoContext != 0)
            throw IllegalArgumentException("Missing closing ) in query: $query")
        unloadMetrics()

        if (elements.none { qe -> OmMetric.OVERLAP in qe.scoreParts }) {
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
        if (query[idx] == '"') {
            val end = query.indexOf('"', idx + 1)
            if (end == -1)
                throw IllegalArgumentException("Missing closing \" in: ${query.substring(idx)}")
            val command = query.substring(idx + 1, end).replace(Regex("\\s+"), " ")
            if (Regex("^product +\\d{3,}").containsMatchIn(command)) {
                throw IllegalArgumentException("Omsim command \"$command\" is asking for products over 100, reconsider")
            }
            return OmMetric.Omsim(command) to end + 1
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
        throw IllegalArgumentException("Invalid metric(s): ${query.substring(idx)}")
    }

    /**
     * @property precedence higher is more precedence-y, models python's rules
     */
    private sealed interface Callable {
        val arity: Int
        val precedence: Int
        val rightAssociative: Boolean

        sealed interface Unary<T : Comparable<T>> : Callable {
            val func: (T) -> T
            override val arity get() = 1
            override val precedence: Int
            override val rightAssociative get() = true

            sealed class UnaryBoolean(override val func: (Boolean) -> Boolean) : Unary<Boolean> {
                override val precedence = 100
                data object NOT : UnaryBoolean(Boolean::not)
            }

            sealed class UnaryDouble(override val func: (Double) -> Double, override val precedence: Int) :
                Unary<Double> {
                // operators
                data object PLUS : UnaryDouble(Double::unaryPlus, 100)
                data object MINUS : UnaryDouble(Double::unaryMinus, 100)

                // functions
                data object ABS : UnaryDouble(Math::abs, Int.MIN_VALUE)
                data object EXP : UnaryDouble(Math::exp, Int.MIN_VALUE)
                data object LOG : UnaryDouble(Math::log, Int.MIN_VALUE)
            }
        }

        sealed interface Binary<I> : Callable {
            override val arity get() = 2
            val func: (I, I) -> Comparable<*>

            sealed class BinaryAny(
                override val func: (Any?, Any?) -> Boolean,
                override val precedence: Int,
                override val rightAssociative: Boolean = false
            ) : Binary<Any?> {
                data object EQ : BinaryAny({ a, b -> a == b }, 3)
                data object NE : BinaryAny({ a, b -> a != b }, 3)
            }

            sealed class BinaryBoolean(
                override val func: (Boolean, Boolean) -> Boolean,
                override val precedence: Int,
                override val rightAssociative: Boolean = false
            ) : Binary<Boolean> {
                data object AND : BinaryBoolean(Boolean::and, 2)
                data object OR : BinaryBoolean(Boolean::or, 1)
            }

            sealed class BinaryDouble(
                override val func: (Double, Double) -> Comparable<*>,
                override val precedence: Int,
                override val rightAssociative: Boolean = false
            ) : Binary<Double> {
                data object LT : BinaryDouble({ a, b -> a < b }, 4)
                data object GT : BinaryDouble({ a, b -> a > b }, 4)
                data object LE : BinaryDouble({ a, b -> a <= b }, 4)
                data object GE : BinaryDouble({ a, b -> a >= b }, 4)

                data object PLUS : BinaryDouble(Double::plus, 5)
                data object MINUS : BinaryDouble(Double::minus, 5)
                data object TIMES : BinaryDouble(Double::times, 6)
                data object DIV : BinaryDouble(Double::div, 6)
                data object REM : BinaryDouble(Double::rem, 6)
                data object POW : BinaryDouble(Double::pow, 101, true)

                // functions
                data object MIN : BinaryDouble(Math::min, Int.MIN_VALUE)
                data object MAX : BinaryDouble(Math::max, Int.MIN_VALUE)
            }
        }
    }

    private enum class Token {
        METRIC, UNARY_OP, BINARY_OP, FUNCTION_CALL,
    }

    private fun parseCustomMetric(query: String, startIdx: Int, boundary: Char): Pair<OmMetric.Custom<*>, Int> {
        var idx = startIdx
        val opStack = ArrayDeque<Callable>()
        val metrics = mutableListOf<OmMetric<*>>()
        val valueStack = ArrayDeque<(OmScore) -> Comparable<*>?>()

        fun applyOperator(op: Callable) {
            val context = op::class.simpleName

            when (op) {
                is Callable.Unary<*> -> {
                    val value = valueStack.removeLast()
                    valueStack.addLast {
                        val v = value(it) ?: return@addLast null
                        when (op) {
                            is Callable.Unary.UnaryBoolean -> op.func(v.booleanOrBust(context))
                            is Callable.Unary.UnaryDouble -> op.func(v.doubleOrBust(context))
                        }
                    }
                }

                is Callable.Binary<*> -> {
                    val right = valueStack.removeLast()
                    val left = valueStack.removeLast()

                    valueStack.addLast {
                        val l = left(it)
                        val r = right(it)

                        if (l == null || r == null) {
                            if (op is Callable.Binary.BinaryAny) { // we can try it
                                op.func(l, r)
                            } else null
                        } else when (op) {
                            is Callable.Binary.BinaryBoolean ->
                                op.func(l.booleanOrBust(context), r.booleanOrBust(context))
                            is Callable.Binary.BinaryDouble ->
                                op.func(l.doubleOrBust(context), r.doubleOrBust(context))
                            is Callable.Binary.BinaryAny -> {
                                if (l::class == r::class)
                                    op.func(l, r)
                                else // the only funnel conversion is to double, hope for the best
                                    op.func(l.doubleOrBust(context), r.doubleOrBust(context))
                            }
                        }
                    }
                }
            }
        }

        fun shouldReduce(incoming: Callable?): Boolean {
            if (opStack.isEmpty())
                return false
            // null is treated as minimum precedence -> reduce everything
            if (incoming == null)
                return true
            return if (incoming.rightAssociative)
                opStack.peekLast().precedence > incoming.precedence
            else
                opStack.peekLast().precedence >= incoming.precedence
        }

        fun pushOperator(op: Callable?) {
            while (shouldReduce(op))
                applyOperator(opStack.removeLast())
            if (op != null)
                opStack.addLast(op)
        }

        fun pushMetric(metric: OmMetric<*>) {
            metrics.add(metric)
            valueStack.addLast(metric.getValueFrom)
        }

        var allowedTokens = EnumSet.of(Token.METRIC, Token.UNARY_OP, Token.FUNCTION_CALL)
        while (true) {
            if (idx >= query.length)
                throw IllegalArgumentException("Missing closing $boundary in query: $query")
            if (query[idx] == boundary) {
                if (allowedTokens.contains(Token.METRIC))
                    throw IllegalArgumentException("Missing final metric in query: $query")
                else
                    break
            }

            if (query[idx] == ' ') {
                idx++
                continue
            }
            if (allowedTokens.contains(Token.UNARY_OP)) {
                val op = when (query[idx]) {
                    '+' -> Callable.Unary.UnaryDouble.PLUS
                    '-' -> Callable.Unary.UnaryDouble.MINUS
                    '!' -> Callable.Unary.UnaryBoolean.NOT
                    else -> null
                }
                if (op != null) {
                    idx += 1
                    pushOperator(op)
                    allowedTokens = EnumSet.of(Token.METRIC, Token.UNARY_OP, Token.FUNCTION_CALL)
                    continue
                }
            }
            if (allowedTokens.contains(Token.BINARY_OP)) {
                val (ops, end) = binaryOperatorTrie.findLongestPrefix(query, idx)
                when (ops.size) {
                    0 -> {}
                    1 -> {
                        idx = end
                        pushOperator(ops[0])
                        allowedTokens = EnumSet.of(Token.METRIC, Token.UNARY_OP, Token.FUNCTION_CALL)
                        continue
                    }
                    else -> throw IllegalArgumentException("Ambiguous operator: ${query.substring(idx, end)}")
                }
            }
            if (allowedTokens.contains(Token.FUNCTION_CALL)) {
                val op = when {
                    query.startsWith("abs(", idx, ignoreCase = true) -> Callable.Unary.UnaryDouble.ABS
                    query.startsWith("exp(", idx, ignoreCase = true) -> Callable.Unary.UnaryDouble.EXP
                    query.startsWith("log(", idx, ignoreCase = true) -> Callable.Unary.UnaryDouble.LOG
                    query.startsWith("min(", idx, ignoreCase = true) -> Callable.Binary.BinaryDouble.MIN
                    query.startsWith("max(", idx, ignoreCase = true) -> Callable.Binary.BinaryDouble.MAX
                    else -> null
                }
                if (op != null) {
                    idx += 4
                    pushOperator(op)
                    for (i in 0 until op.arity) {
                        val (metric, end) = parseCustomMetric(query, idx, if (i == op.arity - 1) ')' else ',')
                        pushMetric(metric)
                        idx = end
                    }
                    allowedTokens = EnumSet.of(Token.BINARY_OP)
                    continue
                }
            }
            if (allowedTokens.contains(Token.METRIC)) {
                // either works or we throw
                val (metric, end) = parseMetric(query, idx)
                idx = end
                pushMetric(metric)
                allowedTokens = EnumSet.of(Token.BINARY_OP)
                continue
            }
            throw IllegalArgumentException("Invalid token(s): ${query.substring(idx)}")
        }

        // final reduction
        pushOperator(null)

        @Suppress("UNCHECKED_CAST")
        val expression = valueStack.single() as (OmScore) -> Comparable<Any>?

        // if it throws it throws here
        expression(allOnes)

        val descr = query.substring(startIdx, idx)
        return OmMetric.Custom(
            displayName = descr,
            subMetrics = metrics,
            getValueFrom = expression,
        ) to idx + 1
    }

    private fun Comparable<*>.booleanOrBust(context: String? = null): Boolean {
        return when (this) {
            is Boolean -> this
            else -> throw IllegalArgumentException("Invalid type: ${this::class.simpleName}" + (context?.let { " for $it" }
                ?: ""))
        }
    }

    private fun Comparable<*>.doubleOrBust(context: String? = null): Double {
        return when (this) {
            is Number -> toDouble()
            is InfinInt -> toDouble()
            is LevelValue -> 100_000.0.pow(level) * value
            else -> throw IllegalArgumentException("Invalid type: ${this::class.simpleName}" + (context?.let { " for $it" }
                ?: ""))
        }
    }

    private inline fun <reified T : Comparable<T>> OmMetric.Custom<*>.asCustom(context: String? = null): OmMetric.Custom<T?> {
        @Suppress("UNCHECKED_CAST")
        when (val v = getValueFrom(allOnes)) {
            null -> throw IllegalArgumentException("Invalid type: <null>" + (context?.let { " for $it" } ?: ""))
            is T -> return this as OmMetric.Custom<T?>
            else -> throw IllegalArgumentException("Invalid type: ${v::class.simpleName}" + (context?.let { " for $it" }
                ?: ""))
        }
    }
}
