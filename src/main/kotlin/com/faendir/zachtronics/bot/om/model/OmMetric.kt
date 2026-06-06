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

package com.faendir.zachtronics.bot.om.model

import com.faendir.zachtronics.bot.model.Metric
import com.faendir.zachtronics.bot.model.StringFormat
import com.faendir.zachtronics.bot.utils.InfinInt
import com.faendir.zachtronics.bot.utils.InfinInt.Companion.toInfinInt
import com.faendir.zachtronics.bot.utils.LevelValue
import com.faendir.zachtronics.bot.utils.newEnumSet
import com.faendir.zachtronics.bot.utils.runIf
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

/**
 * @property displayName `C` or `T` or `Sum`
 * @property description `c` or `T` or `g+c+a`
 */
@Suppress("ClassName")
sealed interface OmMetric<out T> : Metric, Comparator<OmScore> where T : Comparable<T & Any>? {
    val description: String
    val scoreParts: Collection<ScorePart<*>>
    val measurePoint: MeasurePoint
    val getValueFrom: (OmScore) -> T

    override fun compare(o1: OmScore, o2: OmScore) =
        compareValuesBy(o1, o2, nullsLast(), getValueFrom)

    /** `34c` or `O` or `g+c+a=215`, no spaces/separators */
    fun describe(score: OmScore, format: StringFormat): String?

    companion object {
        private val numberFormat = DecimalFormat("0.###", DecimalFormatSymbols(Locale.ENGLISH))
    }

    /** Values and modifiers */
    sealed interface ScorePart<T : Comparable<T & Any>?> : OmMetric<T> {
        override val scoreParts
            get() = listOf(this)
    }

    sealed class Value<T>(
        override val displayName: String, // X
        override val measurePoint: MeasurePoint,
        override val getValueFrom: (OmScore) -> T,
        val alias: String? = null
    ) : ScorePart<T> where T : Comparable<T & Any>? {
        override val description: String = displayName.lowercase()

        override fun describe(score: OmScore, format: StringFormat): String? =
            when (val v = getValueFrom(score)) {
                null -> null
                is Int -> "$v$description"
                is Double -> {
                    numberFormat.format(v).runIf(format == StringFormat.FILE_NAME) { replace("∞", "INF") } + description
                }
                is InfinInt -> when (format) {
                    StringFormat.FILE_NAME -> "${v.toLatinString()}$description"
                    else -> "$v$description"
                }
                is LevelValue -> when (format) { // `1.23a1` and `1.23a'`
                    StringFormat.FILE_NAME -> "${numberFormat.format(v.value)}$description${v.level}"
                    else -> "${numberFormat.format(v.value)}$description" + "'".repeat(v.level)
                }
                else -> throw IllegalArgumentException("Invalid type: ${v::class.simpleName}")
            }
    }

    sealed class Modifier(
        override val displayName: String,
        override val measurePoint: MeasurePoint,
        override val getValueFrom: (OmScore) -> Boolean,
        private val reverseOrder: Boolean = false
    ) : ScorePart<Boolean> {
        override val description: String = displayName
        override val collapsible: Boolean = false

        override fun compare(o1: OmScore, o2: OmScore) =
            compareValuesBy(o1, o2, if (reverseOrder) reverseOrder() else naturalOrder(), getValueFrom)

        override fun describe(score: OmScore, format: StringFormat): String? =
            if (getValueFrom(score)) displayName else null
    }

    /** must be a [ScorePart] (which it is) so that we can isolate it inside [Custom]s */
    data class Omsim(val command: String) : ScorePart<Double?> {
        override val displayName = "\"${command}\""
        override val description = displayName
        override val measurePoint = when { // this is just a basic attempt
            command.startsWith("parsed") -> MeasurePoint.START
            command.startsWith("steady state") -> MeasurePoint.INFINITY
            else -> MeasurePoint.VICTORY
        }
        override val getValueFrom: (OmScore) -> Double? = {
            when (val v = it.extraKnownMetrics[command]) {
                null -> throw IllegalArgumentException("Unknown metric: $displayName")
                Double.NaN -> null
                else -> v
            }
        }

        override fun describe(score: OmScore, format: StringFormat) = null
    }

    /** functions of other [OmMetric]s */
    sealed interface Computed<T : Comparable<T & Any>?> : OmMetric<T> {
        val subMetrics: List<OmMetric<*>>
        override val measurePoint
            get() = subMetrics.findMeasurePoint()
        override val scoreParts
            get() = subMetrics.flatMap { it.scoreParts }

        override fun describe(score: OmScore, format: StringFormat): String? =
            getValueFrom(score)?.let { "$description=${numberFormat.format(it)}" }
    }

    sealed class Sum(override val displayName: String, vararg metrics: OmMetric<Int?>) : Computed<Int?> {
        override val subMetrics = metrics.toList()
        override val getValueFrom = l@{ score: OmScore ->
            subMetrics.sumOf { it.getValueFrom(score) ?: return@l null }
        }

        override val description: String = metrics.joinToString("+") { it.description }
    }

    sealed class Product(vararg metrics: OmMetric<*>) : Computed<Double?> {
        override val subMetrics = metrics.toList()
        override val getValueFrom = l@{ score: OmScore ->
            subMetrics.fold(1.0) { acc, part ->
                val value = (part.getValueFrom(score) as? Number)?.toDouble() ?: return@l null
                acc * value
            }
        }
        override val displayName = "X"
        override val description = subMetrics.joinToString("·") { it.description }
    }

    sealed class Not(
        private val modifier: Modifier,
        override val displayName: String = "!${modifier.displayName}"
    ) : Computed<Boolean> {
        override val collapsible = modifier.collapsible
        override val measurePoint = modifier.measurePoint
        override val subMetrics = listOf(modifier)
        override val getValueFrom = { score: OmScore -> !modifier.getValueFrom(score) }
        override val description = displayName

        override fun compare(o1: OmScore, o2: OmScore) = modifier.compare(o1, o2)
        override fun describe(score: OmScore, format: StringFormat): String? = null
    }

    sealed class And(vararg metrics: OmMetric<Boolean>) : Computed<Boolean> {
        override val subMetrics = metrics.toList()
        override val collapsible: Boolean = subMetrics.all { it.collapsible }
        override val getValueFrom =
            { score: OmScore -> subMetrics.all { part -> part.getValueFrom(score) } }
        override val displayName = subMetrics.joinToString("") { it.displayName }
        override val description = subMetrics.joinToString("") { it.description }

        override fun describe(score: OmScore, format: StringFormat): String? = null
    }

    open class Custom<T : Comparable<T & Any>?>(
        override val subMetrics: List<OmMetric<*>>,
        override val displayName: String,
        override val getValueFrom: (OmScore) -> T
    ) : Computed<T> {
        override val description: String = displayName
    }

    /** true constants */
    open class Constant<T : Comparable<T & Any>?>(
        override val displayName: String,
        val value: T,
    ) : OmMetric<T> {
        override val collapsible = false
        override val measurePoint = MeasurePoint.START
        override val getValueFrom: (OmScore) -> T = { value }
        override val scoreParts = emptyList<ScorePart<*>>()
        override val description = displayName
        override fun describe(score: OmScore, format: StringFormat): String? = null
    }

    data object COST : Value<Int>("G", MeasurePoint.START, OmScore::cost)
    data object INSTRUCTIONS : Value<Int>("I", MeasurePoint.START, OmScore::instructions)

    data object CYCLES : Value<Int>("C", MeasurePoint.VICTORY, OmScore::cycles)
    data object AREA : Value<Int>("A", MeasurePoint.VICTORY, OmScore::area)
    data object HEIGHT : Value<Int?>("H", MeasurePoint.VICTORY, OmScore::height)
    data object WIDTH : Value<Double?>("W", MeasurePoint.VICTORY, OmScore::width)
    data object BOUNDING_HEX : Value<Int?>("B", MeasurePoint.VICTORY, OmScore::boundingHex)

    data object RATE : Value<Double?>("R", MeasurePoint.INFINITY, OmScore::rate, alias = "C'")
    data object AREA_INF : Value<LevelValue?>("A", MeasurePoint.INFINITY, OmScore::areaINF)
    data object HEIGHT_INF : Value<InfinInt?>("H", MeasurePoint.INFINITY, OmScore::heightINF) {
        override val collapsible: Boolean = false
    }
    data object WIDTH_INF : Value<Double?>("W", MeasurePoint.INFINITY, OmScore::widthINF) {
        override val collapsible: Boolean = false
    }
    data object BOUNDING_HEX_INF : Value<InfinInt?>("B", MeasurePoint.INFINITY, OmScore::boundingHexINF) {
        override val collapsible: Boolean = false
    }

    // convenience
    data object A0_INF : Value<InfinInt?>("A0", MeasurePoint.INFINITY, { it.areaINF?.get(0)?.toInfinInt() })
    data object A1_INF : Value<Double?>("A1", MeasurePoint.INFINITY, { it.areaINF?.get(1) }, alias = "A'")
    data object A2_INF : Value<Double?>("A2", MeasurePoint.INFINITY, { it.areaINF?.get(2) }, alias = "A''")

    data object OVERLAP : Modifier("O", MeasurePoint.START, OmScore::overlap)
    data object TRACKLESS : Modifier("T", MeasurePoint.START, OmScore::trackless, reverseOrder = true)
    /** we're massively cheating by making it a first class [Modifier]@V instead of a [Computed]@INF, what do I not do for pretty gifs */
    data object LOOPING : Modifier("L", MeasurePoint.VICTORY, OmScore::looping, reverseOrder = true)

    data object ANYTHING_GOES : Constant<Boolean>("O", true)
    data object NOVERLAP : Not(OVERLAP, displayName = "")
    data object NOVERLAP_TRACKLESS : And(NOVERLAP, TRACKLESS)

    data object SUM3A : Sum("Sum", COST, CYCLES, AREA)
    data object SUM3I : Sum("Sum", COST, CYCLES, INSTRUCTIONS)
    data object SUM4 : Sum("Sum4", COST, CYCLES, AREA, INSTRUCTIONS)

    data object PRODUCT_GC : Product(COST, CYCLES)
    data object PRODUCT_GA : Product(COST, AREA)
    data object PRODUCT_GI : Product(COST, INSTRUCTIONS)
    data object PRODUCT_CA : Product(CYCLES, AREA)
    data object PRODUCT_CI : Product(CYCLES, INSTRUCTIONS)

    data object PRODUCT_GCA : Product(COST, CYCLES, AREA)
    data object PRODUCT_GCI : Product(COST, CYCLES, INSTRUCTIONS)
    data object PRODUCT_INF : Product(COST, RATE, INSTRUCTIONS)
}

/**
 * @property displayName @X
 */
enum class MeasurePoint(val displayName: String) {
    START("@0"),
    VICTORY("@V"),
    INFINITY("@∞")
}

fun Iterable<OmMetric<*>>.findMeasurePoint(): MeasurePoint {
    val points = mapTo(newEnumSet()) { it.measurePoint }
    if (points.size == 1) {
        return points.first()
    }
    if (points.size == 2 && points.contains(MeasurePoint.START)) {
        return points.find { it != MeasurePoint.START }!!
    }
    throw IllegalStateException("Incoherent metric set")
}

operator fun <T : Comparable<T & Any>?> OmScore.get(metric: OmMetric<T>): T = metric.getValueFrom(this)

// https://youtrack.jetbrains.com/issue/KT-8970 stops us from doing some nice reflectivy thingy
object OmMetrics {
    /** Score printing order for machines */
    val VALUE = listOf(
        OmMetric.COST,
        OmMetric.INSTRUCTIONS,
        OmMetric.CYCLES,
        OmMetric.AREA,
        OmMetric.HEIGHT,
        OmMetric.WIDTH,
        OmMetric.BOUNDING_HEX,
        OmMetric.RATE,
        OmMetric.AREA_INF,
        OmMetric.HEIGHT_INF,
        OmMetric.WIDTH_INF,
        OmMetric.BOUNDING_HEX_INF,
    )
    val MODIFIER = listOf(OmMetric.OVERLAP, OmMetric.TRACKLESS, OmMetric.LOOPING)
    val FULL_SCORE = VALUE + MODIFIER

    private val ALL_SCORE_PARTS = FULL_SCORE + listOf(OmMetric.A0_INF, OmMetric.A1_INF, OmMetric.A2_INF)

    fun userFacing(type: OmType? = null) = ALL_SCORE_PARTS + when (type) {
        OmType.NORMAL, OmType.POLYMER_HEIGHT, OmType.POLYMER_WIDTH, OmType.POLYMER_SKEW -> listOf(
            OmMetric.SUM3A,
            OmMetric.SUM4,
            OmMetric.PRODUCT_GCA,
            OmMetric.PRODUCT_INF,
        )
        OmType.PRODUCTION -> listOf(
            OmMetric.SUM3I,
            OmMetric.SUM4,
            OmMetric.PRODUCT_GCI,
            OmMetric.PRODUCT_INF,
        )
        null -> listOf(
            OmMetric.SUM3A,
            OmMetric.SUM3I,
            OmMetric.SUM4,
            OmMetric.PRODUCT_GCA,
            OmMetric.PRODUCT_GCI,
            OmMetric.PRODUCT_INF,
        )
    }

    /** Score printing order for humans */
    val BY_MEASURE_POINT = mapOf(
        MeasurePoint.START to listOf(
            OmMetric.COST,
            OmMetric.INSTRUCTIONS,
            OmMetric.OVERLAP,
            OmMetric.TRACKLESS,
        ),
        MeasurePoint.VICTORY to listOf(
            OmMetric.COST,
            OmMetric.CYCLES,
            OmMetric.AREA,
            OmMetric.INSTRUCTIONS,
            OmMetric.HEIGHT,
            OmMetric.WIDTH,
            OmMetric.BOUNDING_HEX,
            OmMetric.OVERLAP,
            OmMetric.TRACKLESS,
            OmMetric.LOOPING,
        ),
        MeasurePoint.INFINITY to listOf(
            OmMetric.COST,
            OmMetric.RATE,
            OmMetric.AREA_INF,
            OmMetric.INSTRUCTIONS,
            OmMetric.HEIGHT_INF,
            OmMetric.WIDTH_INF,
            OmMetric.BOUNDING_HEX_INF,
            OmMetric.OVERLAP,
            OmMetric.TRACKLESS
        )
    )
}
