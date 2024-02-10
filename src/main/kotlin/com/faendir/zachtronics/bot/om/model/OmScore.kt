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

import com.faendir.zachtronics.bot.model.DisplayContext
import com.faendir.zachtronics.bot.model.Score
import com.faendir.zachtronics.bot.model.StringFormat
import com.faendir.zachtronics.bot.utils.InfinInt
import com.faendir.zachtronics.bot.utils.LevelValue
import com.faendir.zachtronics.bot.utils.newEnumSet
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.*

@Serializable
data class OmScore(
    // @0
    val cost: Int,
    val instructions: Int,
    val overlap: Boolean,
    val trackless: Boolean,

    // @6
    val cycles: Int,
    val area: Int,
    val height: Int?,   // tracked for NORMAL,POLYMER
    val width: Double?, // tracked for NORMAL,POLYMER

    // @INF
    /**
     * * FINITE: normal looping solve
     * * INFINITE: can't happen right now, represents a sublinear solve
     * * null: solve doesn't get to arbitrary large output numbers due to stopping or crashing
     */
    val rate: Double?,
    /**
     * the possibilities are:
     * * A integer, A'=0, A''=0
     * * A=INF, A' float, A''=0
     * * A=INF, A'=INF, A'' float
     *
     * represented as (level, value)
     */
    val areaINF: LevelValue?,
    val heightINF: InfinInt?, // tracked for NORMAL,POLYMER
    val widthINF: Double?,    // tracked for NORMAL
) : Score<OmCategory> {
    // @INF
    @Transient
    val looping: Boolean = rate != null

    @Transient
    val manifolds: Set<OmScoreManifold> =
        // with just 2 manifolds we can cut corners, but this should use the nullity of their scoreParts
        EnumSet.of(OmScoreManifold.VICTORY).apply { if (looping) add(OmScoreManifold.INFINITY) }

    /**
     * humans: `12g/34c/56a/78i/3h/4w/OTL@6 12g/34r/57a/78i/6h/7w/OT@∞`
     *
     * machines: `12g-34i-15c-12a-3h-4w[-15r-12a0-3h-4w]-O-T`
     */
    override fun toDisplayString(context: DisplayContext<OmCategory>): String {

        if (context.format == StringFormat.FILE_NAME) {
            // we write a standard machine-readable deduplicated score
            val allMetrics = OmMetrics.VALUE + listOf(OmMetric.OVERLAP, OmMetric.TRACKLESS)
            return subScoreDisplay(allMetrics, context.format)
        }

        val desiredMetrics = context.categories
            ?.flatMapTo(HashSet(), OmCategory::requiredParts)
            ?.takeIf { it.isNotEmpty() }
            ?.apply { if (context.format != StringFormat.REDDIT) addAll(OmMetrics.MODIFIER) }
            ?: OmMetrics.FULL_SCORE.toSet()

        val desiredManifolds = context.categories
            ?.mapTo(newEnumSet(), OmCategory::associatedManifold)
            ?.takeIf { it.isNotEmpty() }
            ?: manifolds

        return desiredManifolds.joinToString(" ") { manifold ->
            subScoreDisplay(manifold.scoreParts.filter { it in desiredMetrics }, context.format)
                .run { if (desiredManifolds.size > 1) plus(manifold.displayName) else this }
        } + additionalMetricDescriptions(context)
    }

    private fun subScoreDisplay(
        subScoreParts: List<OmMetric.ScorePart<*>>,
        format: StringFormat
    ) = subScoreParts.mapNotNull { it.describe(this, format) }.joinToString(format.separator)

    /** @return extra descriptions of computed metrics, like ` (g+c+a=215)` */
    private fun additionalMetricDescriptions(context: DisplayContext<OmCategory>): String = when (context.format) {
        StringFormat.DISCORD, StringFormat.PLAIN_TEXT ->
            context.categories
                ?.flatMap { it.metrics }
                ?.filterIsInstance<OmMetric.Computed>()
                ?.distinct()
                ?.mapNotNull { metric -> metric.describe(this, context.format) }
                ?.takeIf { it.isNotEmpty() }
                ?.joinToString(separator = ", ", prefix = " (", postfix = ")")
                .orEmpty()
        else -> ""
    }

    /** `12g-15c-12a-34i[-O][-T]` to comply with Syx's desires */
    fun toMorsString(): String = subScoreDisplay(
        listOf(
            OmMetric.COST, OmMetric.CYCLES, OmMetric.AREA, OmMetric.INSTRUCTIONS, OmMetric.OVERLAP, OmMetric.TRACKLESS
        ), StringFormat.FILE_NAME
    )

    override fun toString() = toDisplayString()
}