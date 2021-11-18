/*
 * Copyright (c) 2021
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
import com.faendir.zachtronics.bot.utils.productOf

private const val NOT_FOUND_PLACEHOLDER = 1_000_000_000.0

@Suppress("ClassName")
sealed class OmMetric(override val displayName: String, val scoreParts: List<OmScorePart>) : Metric {
    abstract val comparator: Comparator<OmScore>
    abstract val description: String

    abstract fun describe(score: OmScore): String?

    abstract class Basic(identifier: String, scorePart: OmScorePart) : OmMetric(identifier, listOf(scorePart)) {
        override val comparator: Comparator<OmScore> = Comparator.comparing { scorePart.getValue(it)?.toDouble() ?: NOT_FOUND_PLACEHOLDER }
        override val description: String = scorePart.key.toString()

        override fun describe(score: OmScore): String? = null
    }

    abstract class Sum(identifier: String, vararg scoreParts: OmScorePart) : OmMetric(identifier, scoreParts.toList()) {
        override val comparator: Comparator<OmScore> =
            Comparator.comparing { score -> scoreParts.sumOf { it.getValue(score)?.toDouble() ?: NOT_FOUND_PLACEHOLDER } }
        override val description: String = scoreParts.joinToString("+") { it.key.toString() }
        override fun describe(score: OmScore): String? =
            if (scoreParts.all { it.getValue(score) != null }) "$description=${scoreParts.sumOf { it.getValue(score)!!.toInt() }}" else null
    }

    abstract class Product(vararg scoreParts: OmScorePart) : OmMetric("X", scoreParts.toList()) {
        override val comparator: Comparator<OmScore> =
            Comparator.comparing { score -> scoreParts.map { it.getValue(score)?.toDouble() ?: NOT_FOUND_PLACEHOLDER }.reduce { a, b -> a * b } }
        override val description: String = scoreParts.joinToString("·") { it.key.toString() }
        override fun describe(score: OmScore): String? =
            if (scoreParts.all { it.getValue(score) != null }) "$description=${scoreParts.productOf { it.getValue(score)!!.toInt() }}" else null
    }

    abstract class Modifier(identifier: String): OmMetric(identifier, emptyList()) {
        override val comparator: Comparator<OmScore> = Comparator { _, _ -> 0 }
        override val description: String = identifier

        override fun describe(score: OmScore): String? = null
    }

    object COST : Basic("G", OmScorePart.COST)
    object CYCLES : Basic("C", OmScorePart.CYCLES)
    object AREA : Basic("A", OmScorePart.AREA)
    object INSTRUCTIONS : Basic("I", OmScorePart.INSTRUCTIONS)
    object HEIGHT : Basic("Height", OmScorePart.HEIGHT)
    object WIDTH : Basic("Width", OmScorePart.WIDTH)

    object SUM3A : Sum("Sum", OmScorePart.COST, OmScorePart.CYCLES, OmScorePart.AREA)
    object SUM3I : Sum("Sum", OmScorePart.COST, OmScorePart.CYCLES, OmScorePart.INSTRUCTIONS)
    object SUM4 : Sum("Sum4", OmScorePart.COST, OmScorePart.CYCLES, OmScorePart.AREA, OmScorePart.INSTRUCTIONS)

    object PRODUCT_GC : Product(OmScorePart.COST, OmScorePart.CYCLES)
    object PRODUCT_GA : Product(OmScorePart.COST, OmScorePart.AREA)
    object PRODUCT_GI : Product(OmScorePart.COST, OmScorePart.INSTRUCTIONS)
    object PRODUCT_CA : Product(OmScorePart.CYCLES, OmScorePart.AREA)
    object PRODUCT_CI : Product(OmScorePart.CYCLES, OmScorePart.INSTRUCTIONS)
    object PRODUCT_AI : Product(OmScorePart.AREA, OmScorePart.INSTRUCTIONS)

    // hack: don't want to show just "T", so need two-in-one
    object TRACKLESS_INSTRUCTION : Basic("TI", OmScorePart.INSTRUCTIONS)
    object OVERLAP : Modifier("O")
}