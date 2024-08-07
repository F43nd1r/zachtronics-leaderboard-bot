/*
 * Copyright (c) 2024
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
import com.faendir.zachtronics.bot.utils.LevelValue
import com.faendir.zachtronics.bot.utils.runIf
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

@Suppress("ClassName")
sealed interface OmMetric<T> : Metric where T : Comparable<T> {
    val description: String
    val scoreParts: Collection<ScorePart<*>>
    val getValueFrom: (OmScore) -> T?

    /** `34c` or `O` or `g+c+a=215`, no spaces/separators */
    fun describe(score: OmScore, format: StringFormat): String?

    val comparator: Comparator<OmScore>
        get() = Comparator.comparing(getValueFrom, Comparator.nullsLast<T>(Comparator.naturalOrder()))

    companion object {
        private val numberFormat = DecimalFormat("0.###", DecimalFormatSymbols(Locale.ENGLISH))
    }

    /** Values and modifiers */
    sealed interface ScorePart<T: Comparable<T>> : OmMetric<T> {
        val measurePoint: MeasurePoint

        override val scoreParts
            get() = listOf(this)
    }

    sealed class Value<T>(
        override val displayName: String,
        private val scoreId: Char,
        override val measurePoint: MeasurePoint,
        final override val getValueFrom: (OmScore) -> T?,
        private val describer: Value<T>.(T, StringFormat) -> String
    ) : ScorePart<T> where T : Comparable<T> {
        override val description: String = scoreId.toString()
        override fun describe(score: OmScore, format: StringFormat): String? =
            getValueFrom(score)?.let { describer(it, format) }

        internal fun describeInt(value: Int, format: StringFormat): String = "$value$scoreId"

        internal fun describeInfinInt(value: InfinInt, format: StringFormat): String =
            when (format) {
                StringFormat.FILE_NAME -> "${value.toLatinString()}$scoreId"
                else -> "$value$scoreId"
            }

        internal fun describeDouble(value: Double, format: StringFormat): String =
            numberFormat.format(value).runIf(format == StringFormat.FILE_NAME) { replace("∞", "INF") } + scoreId

        /** `1.23a1` and `1.23a'` */
        internal fun describeLevelValue(lv: LevelValue, format: StringFormat): String =
            when (format) {
                StringFormat.FILE_NAME -> "${numberFormat.format(lv.value)}$scoreId${lv.level}"
                else -> "${numberFormat.format(lv.value)}$scoreId" + "'".repeat(lv.level)
            }
    }

    sealed class Modifier(
        final override val displayName: String,
        override val measurePoint: MeasurePoint,
        final override val getValueFrom: (OmScore) -> Boolean,
        reverseOrder: Boolean = false
    ) : ScorePart<Boolean> {
        override val comparator: Comparator<OmScore> =
            Comparator.comparing(getValueFrom).let { if (reverseOrder) it.reversed() else it }
        override val description: String = displayName
        override val collapsible: Boolean = false

        override fun describe(score: OmScore, format: StringFormat): String? = if (getValueFrom(score)) displayName else null
    }

    /** functions of other [OmMetric]s */
    sealed interface Computed<T : Comparable<T>> : OmMetric<T> {
        val subMetrics: Array<out OmMetric<*>>
        override val scoreParts
            get() = subMetrics.flatMap { it.scoreParts }
    }

    sealed class Sum(override val displayName: String, final override vararg val subMetrics: OmMetric<Int>) : Computed<Int> {
        override val getValueFrom = l@{ score: OmScore ->
            subMetrics.sumOf { it.getValueFrom(score) ?: return@l null }
        }

        override val description: String = subMetrics.joinToString("+") { it.description }

        override fun describe(score: OmScore, format: StringFormat): String? =
            getValueFrom(score)?.let { "$description=$it" }
    }

    sealed class Product(final override vararg val subMetrics: OmMetric<Int>) : Computed<Double> {
        override val getValueFrom = l@{ score: OmScore ->
            subMetrics.fold(1.0) { acc, part -> acc * (part.getValueFrom(score) ?: return@l null) }
        }
        override val displayName = "X"
        override val description: String = subMetrics.joinToString("·") { it.description }

        override fun describe(score: OmScore, format: StringFormat): String? =
            getValueFrom(score)?.let { "$description=${numberFormat.format(it)}" }
    }

    sealed class Not(final override val displayName: String, private val modifier: Modifier) : Computed<Boolean> {
        override val collapsible: Boolean = modifier.collapsible
        override val subMetrics: Array<out ScorePart<*>> = arrayOf(modifier)
        override val getValueFrom = { score: OmScore -> !modifier.getValueFrom(score) }
        override val comparator: Comparator<OmScore> = modifier.comparator.reversed()
        override val description = displayName

        override fun describe(score: OmScore, format: StringFormat): String? = null
    }

    sealed class And(final override vararg val subMetrics: OmMetric<Boolean>) : Computed<Boolean> {
        override val collapsible: Boolean = subMetrics.all { it.collapsible }
        override val getValueFrom =
            { score: OmScore -> subMetrics.all { part -> (part.getValueFrom(score) == true) } }
        override val displayName = subMetrics.joinToString("") { it.displayName }
        override val description = subMetrics.joinToString("") { it.description }

        override fun describe(score: OmScore, format: StringFormat): String? = null
    }

    /** true constants */
    sealed class Constant<T : Comparable<T>>(
        final override val displayName: String,
        val value: T,
    ) : OmMetric<T> {
        override val collapsible: Boolean = false
        override val getValueFrom: (OmScore) -> T = { value }
        override val scoreParts: Collection<ScorePart<*>> = listOf()
        override val description: String = displayName
        override fun describe(score: OmScore, format: StringFormat): String? = null
    }

    data object COST : Value<Int>("G", 'g', MeasurePoint.START, OmScore::cost, Value<*>::describeInt)
    data object INSTRUCTIONS : Value<Int>("I", 'i', MeasurePoint.START, OmScore::instructions, Value<*>::describeInt)

    data object CYCLES : Value<Int>("C", 'c', MeasurePoint.VICTORY, OmScore::cycles, Value<*>::describeInt)
    data object AREA : Value<Int>("A", 'a', MeasurePoint.VICTORY, OmScore::area, Value<*>::describeInt)
    data object HEIGHT : Value<Int>("H", 'h', MeasurePoint.VICTORY, OmScore::height, Value<*>::describeInt)
    data object WIDTH : Value<Double>("W", 'w', MeasurePoint.VICTORY, OmScore::width, Value<*>::describeDouble)
    data object BOUNDING_HEX : Value<Int>("B", 'b', MeasurePoint.VICTORY, OmScore::boundingHex, Value<*>::describeInt)

    data object RATE : Value<Double>("R", 'r', MeasurePoint.INFINITY, OmScore::rate, Value<*>::describeDouble)
    data object AREA_INF : Value<LevelValue>("A", 'a', MeasurePoint.INFINITY, OmScore::areaINF, Value<*>::describeLevelValue)
    data object HEIGHT_INF : Value<InfinInt>("H", 'h', MeasurePoint.INFINITY, OmScore::heightINF, Value<*>::describeInfinInt) {
        override val collapsible: Boolean = false
    }
    data object WIDTH_INF : Value<Double>("W", 'w', MeasurePoint.INFINITY, OmScore::widthINF, Value<*>::describeDouble) {
        override val collapsible: Boolean = false
    }
    data object BOUNDING_HEX_INF : Value<InfinInt>("B", 'b', MeasurePoint.INFINITY, OmScore::boundingHexINF, Value<*>::describeInfinInt) {
        override val collapsible: Boolean = false
    }

    data object OVERLAP : Modifier("O", MeasurePoint.START, OmScore::overlap)
    data object TRACKLESS : Modifier("T", MeasurePoint.START, OmScore::trackless, reverseOrder = true)
    /** we're massively cheating by making it a first class [Modifier]@V instead of a [Computed]@INF, what do I not do for pretty gifs */
    data object LOOPING : Modifier("L", MeasurePoint.VICTORY, OmScore::looping, reverseOrder = true)

    data object ANYTHING_GOES : Constant<Boolean>("O", true)
    data object NOVERLAP : Not("", OVERLAP)
    data object NOVERLAP_TRACKLESS : And(NOVERLAP, TRACKLESS)

    data object SUM3A : Sum("Sum", COST, CYCLES, AREA)
    data object SUM3I : Sum("Sum", COST, CYCLES, INSTRUCTIONS)
    data object SUM4 : Sum("Sum4", COST, CYCLES, AREA, INSTRUCTIONS)

    data object PRODUCT_GC : Product(COST, CYCLES)
    data object PRODUCT_GA : Product(COST, AREA)
    data object PRODUCT_GI : Product(COST, INSTRUCTIONS)
    data object PRODUCT_CA : Product(CYCLES, AREA)
    data object PRODUCT_CI : Product(CYCLES, INSTRUCTIONS)
}

/**
 * @property displayName @X
 */
enum class MeasurePoint(val displayName: String) {
    START("@0"),
    VICTORY("@V"),
    INFINITY("@∞")
}

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

    /** Score printing order for humans */
    val BY_MEASURE_POINT = mapOf(
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