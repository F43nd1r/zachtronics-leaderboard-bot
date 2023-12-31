/*
 * Copyright (c) 2023
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
import com.faendir.zachtronics.bot.utils.InfinInt
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

@Suppress("ClassName")
sealed interface OmMetric : Metric {
    val comparator: Comparator<OmScore>
    val description: String
    val scoreParts: Collection<ScorePart<*>>

    /** `34c` or `O` or `g+c+a=215`, no spaces/separators */
    fun describe(score: OmScore): String?

    companion object {
        private val numberFormat = DecimalFormat("0.###", DecimalFormatSymbols(Locale.ENGLISH))
    }

    /** Values and modifiers */
    sealed interface ScorePart<T: Comparable<T>> : OmMetric {
        val measurePoint: MeasurePoint
        val getValueFrom: (OmScore) -> T?

        override val scoreParts: Collection<ScorePart<*>>
            get() = listOf(this)
    }

    sealed class Value<T>(
        override val displayName: String,
        internal val scoreId: Char,
        override val measurePoint: MeasurePoint,
        final override val getValueFrom: (OmScore) -> T?
    ) : ScorePart<T> where T : Number, T : Comparable<T> {
        override val comparator: Comparator<OmScore> =
            Comparator.comparing(getValueFrom, Comparator.nullsLast<T>(Comparator.naturalOrder()))
        override val description: String = scoreId.toString()

        override fun describe(score: OmScore): String? =
            getValueFrom(score)?.let { numberFormat.format(it) }?.plus(scoreId)

        override fun toString(): String = displayName
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

        override fun describe(score: OmScore): String? = if (getValueFrom(score)) displayName else null

        override fun toString() = displayName
    }

    sealed interface Computed : OmMetric {
        val partVararg: Array<out ScorePart<*>>

        override val scoreParts: Collection<ScorePart<*>>
            get() = partVararg.toList()
    }

    sealed class Sum(override val displayName: String, final override vararg val partVararg: Value<Int>) : Computed {
        private fun extract(score: OmScore): Int? {
            return partVararg.sumOf { it.getValueFrom(score) ?: return null }
        }
        override val comparator: Comparator<OmScore> =
            Comparator.comparing(::extract, Comparator.nullsLast<Int>(Comparator.naturalOrder()))
        override val description: String = partVararg.joinToString("+") { it.scoreId.toString() }

        override fun describe(score: OmScore): String? =
            extract(score)?.let { "$description=$it" }
    }

    sealed class Product(final override vararg val partVararg: Value<*>) : Computed {
        private fun extract(score: OmScore): Double? {
            return partVararg.fold(1.0) { acc, part -> acc * (part.getValueFrom(score)?.toDouble() ?: return null) }
        }
        override val displayName = "X"
        override val comparator: Comparator<OmScore> =
            Comparator.comparing(::extract, Comparator.nullsLast<Double>(Comparator.naturalOrder()))
        override val description: String = partVararg.joinToString("·") { it.scoreId.toString() }

        override fun describe(score: OmScore): String? =
            extract(score)?.let { "$description=${numberFormat.format(it)}" }
    }

    sealed class Concatenation(final override vararg val partVararg: ScorePart<*>) : Computed {
        final override val displayName: String = partVararg.joinToString("") { it.displayName }
        override val comparator: Comparator<OmScore> = partVararg.map { it.comparator }.reduce(Comparator<OmScore>::thenComparing)
        override val description: String = displayName

        /** will be described as the underlying score metrics */
        override fun describe(score: OmScore): String? = null
    }

    object COST : Value<Int>("G", 'g', MeasurePoint.START, OmScore::cost)
    object INSTRUCTIONS : Value<Int>("I", 'i', MeasurePoint.START, OmScore::instructions)

    object CYCLES : Value<Int>("C", 'c', MeasurePoint.VICTORY, OmScore::cycles)
    object AREA : Value<Int>("A", 'a', MeasurePoint.VICTORY, OmScore::area)
    object HEIGHT : Value<Int>("H", 'h', MeasurePoint.VICTORY, OmScore::height)
    object WIDTH : Value<Double>("W", 'w', MeasurePoint.VICTORY, OmScore::width)

    object RATE : Value<Double>("R", 'r', MeasurePoint.INFINITY, OmScore::rate)
    object AREA_INF : Value<InfinInt>("A@∞", 'a', MeasurePoint.INFINITY, OmScore::areaINF)
    object HEIGHT_INF : Value<InfinInt>("H@∞", 'h', MeasurePoint.INFINITY, OmScore::heightINF)
    object WIDTH_INF : Value<Double>("W@∞", 'w', MeasurePoint.INFINITY, OmScore::widthINF)

    object OVERLAP : Modifier("O", MeasurePoint.START, OmScore::overlap)
    object TRACKLESS : Modifier("T", MeasurePoint.START, OmScore::trackless, reverseOrder = true)
    object LOOPING : Modifier("L", MeasurePoint.INFINITY, OmScore::looping, reverseOrder = true)

    object SUM3A : Sum("Sum", COST, CYCLES, AREA)
    object SUM3I : Sum("Sum", COST, CYCLES, INSTRUCTIONS)
    object SUM4 : Sum("Sum4", COST, CYCLES, AREA, INSTRUCTIONS)

    object PRODUCT_GC : Product(COST, CYCLES)
    object PRODUCT_GA : Product(COST, AREA)
    object PRODUCT_GI : Product(COST, INSTRUCTIONS)
    object PRODUCT_CA : Product(CYCLES, AREA)
    object PRODUCT_CI : Product(CYCLES, INSTRUCTIONS)

    object PRODUCT_GA_INF : Product(COST, AREA_INF)
    object PRODUCT_AI_INF : Product(AREA_INF, INSTRUCTIONS)

    // hack: don't want to show just "T", so need two-in-one
    object TRACKLESS_INSTRUCTION : Concatenation(TRACKLESS, INSTRUCTIONS)

    /**
     * @property displayName @X
     */
    enum class MeasurePoint(val displayName: String) {
        START("@0"),
        VICTORY("@V"),
        INFINITY("@∞")
    }
}

// https://youtrack.jetbrains.com/issue/KT-8970 stops us from doing some nice reflectivy thingy
object OmMetrics {
    val VALUE = listOf(
        OmMetric.COST,
        OmMetric.INSTRUCTIONS,
        OmMetric.CYCLES,
        OmMetric.AREA,
        OmMetric.HEIGHT,
        OmMetric.WIDTH,
        OmMetric.RATE,
        OmMetric.AREA_INF,
        OmMetric.HEIGHT_INF,
        OmMetric.WIDTH_INF
    )
    val MODIFIER = listOf(OmMetric.OVERLAP, OmMetric.TRACKLESS, OmMetric.LOOPING)
    val FULL_SCORE = VALUE + MODIFIER
}