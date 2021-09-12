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

private const val NOT_FOUND_PLACEHOLDER = 1_000_000_000.0

@Suppress("ClassName")
sealed class OmMetric(val displayName: String, val scoreParts: List<OmScorePart>) {
    abstract val comparator: Comparator<OmScore>

    abstract class Basic(identifier: String, scorePart: OmScorePart) : OmMetric(identifier, listOf(scorePart)) {
        override val comparator: Comparator<OmScore> = Comparator.comparing { it.parts[scorePart] ?: NOT_FOUND_PLACEHOLDER }
    }

    abstract class Sum(identifier: String, vararg scoreParts: OmScorePart) : OmMetric(identifier, scoreParts.toList()) {
        override val comparator: Comparator<OmScore> = Comparator.comparing { score -> scoreParts.sumOf { score.parts[it] ?: NOT_FOUND_PLACEHOLDER } }
    }

    abstract class Product(vararg scoreParts: OmScorePart) : OmMetric("X", scoreParts.toList()) {
        override val comparator: Comparator<OmScore> =
            Comparator.comparing { score -> scoreParts.map { score.parts[it] ?: NOT_FOUND_PLACEHOLDER }.reduce { a, b -> a * b } }
    }

    object COST : Basic("G", OmScorePart.COST)
    object CYCLES : Basic("C", OmScorePart.CYCLES)
    object AREA : Basic("A", OmScorePart.AREA)
    object INSTRUCTIONS : Basic("I", OmScorePart.INSTRUCTIONS)
    object HEIGHT : Basic("Height", OmScorePart.HEIGHT)
    object WIDTH : Basic("Width", OmScorePart.WIDTH)

    object SUM3A : Sum("SUM-", OmScorePart.COST, OmScorePart.CYCLES, OmScorePart.AREA)
    object SUM3I : Sum("SUM-", OmScorePart.COST, OmScorePart.CYCLES, OmScorePart.INSTRUCTIONS)
    object SUM4 : Sum("SUM4-", OmScorePart.COST, OmScorePart.CYCLES, OmScorePart.AREA, OmScorePart.INSTRUCTIONS)

    object PRODUCT_GC : Product(OmScorePart.COST, OmScorePart.CYCLES)
    object PRODUCT_GA : Product(OmScorePart.COST, OmScorePart.AREA)
    object PRODUCT_GI : Product(OmScorePart.COST, OmScorePart.INSTRUCTIONS)
    object PRODUCT_CA : Product(OmScorePart.CYCLES, OmScorePart.AREA)
    object PRODUCT_CI : Product(OmScorePart.CYCLES, OmScorePart.INSTRUCTIONS)
    object PRODUCT_AI : Product(OmScorePart.AREA, OmScorePart.INSTRUCTIONS)
}