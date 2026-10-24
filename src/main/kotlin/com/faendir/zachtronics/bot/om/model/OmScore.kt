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

import com.faendir.zachtronics.bot.model.DisplayContext
import com.faendir.zachtronics.bot.model.Score
import com.faendir.zachtronics.bot.model.StringFormat
import com.faendir.zachtronics.bot.utils.LevelValue
import com.faendir.zachtronics.bot.utils.newEnumSet
import com.faendir.zachtronics.bot.utils.runIf
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

    // @V
    val cycles: Int,
    val area: Int,
    val height: Int?,      // tracked for NORMAL,POLYMER_HEIGHT
    val width: Double?,    // tracked for NORMAL,POLYMER_WIDTH
    val boundingHex: Int?, // tracked for NORMAL

    // somewhere between @1 and @INF, see https://docs.google.com/document/d/1ujPMowWoO2gAcd-wLo9lw7wyurcbYwhrvd0xBkGNlOA
    val looping: Boolean,

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
    /**
     * the possibilities are:
     * * H integer, H'=0
     * * H=INF, H' float
     *
     * represented as (level, value)
     */
    val heightINF: LevelValue?,      // tracked for NORMAL,POLYMER_HEIGHT
    val widthINF: LevelValue?,       // tracked for NORMAL,POLYMER_WIDTH
    val boundingHexINF: LevelValue?, // tracked for NORMAL

    /** more stuff we learn from omsim at runtime, uses [Double.NaN] as a stand-in for `null` */
    @Transient
    val extraKnownMetrics: MutableMap<String, Double> = hashMapOf()
) : Score<OmCategory> {
    @Transient
    val measurePoints: EnumSet<MeasurePoint> =
        // with just 2 non-start measure points we can cut corners, hopefully we don't need to track more
        EnumSet.of(MeasurePoint.START, MeasurePoint.VICTORY).apply { if (rate != null) add(MeasurePoint.INFINITY) }

    val finiteAreaINF get() = areaINF?.level?.equals(0) ?: false

    /**
     * humans: `15g/34c/56a/78i/3h/4w/8b/OTL@V 15g/34r/57a''/78i/6h'/7w/9b/OT@∞`
     *
     * machines: `15g-78i-34c-56a-3h-4w-8b[-34r-57a2-6h1-7w-9b]-OTL`
     */
    override fun toDisplayString(context: DisplayContext<OmCategory>): String {

        if (context.format == StringFormat.FILE_NAME) {
            // we write a standard machine-readable deduplicated score
            return subScoreDisplay(OmMetrics.VALUE, OmMetrics.MODIFIER, context.format)
        }

        val desiredMetrics = context.categories
            ?.ifEmpty { null }
            ?.flatMapTo(HashSet(), OmCategory::requiredParts)
            ?.apply { if (context.format != StringFormat.REDDIT) addAll(OmMetrics.MODIFIER) }
            ?: OmMetrics.FULL_SCORE.toSet()

        val desiredMeasurePoints = context.categories
            ?.ifEmpty { null }
            ?.mapTo(newEnumSet()) { it.manifold.measurePoint }
            ?: EnumSet.copyOf(measurePoints).apply { remove(MeasurePoint.START) }

        return desiredMeasurePoints.joinToString(" ") { measurePoint ->
            val metrics = OmMetrics.BY_MEASURE_POINT[measurePoint]!!.filter { it in desiredMetrics }
            subScoreDisplay(metrics.filterIsInstance<OmMetric.Value<*>>(),
                            metrics.filterIsInstance<OmMetric.Modifier>(), context.format)
                .runIf(desiredMeasurePoints.size > 1) { plus(measurePoint.displayName) }
        } + additionalMetricDescriptions(context)
    }

    private fun subScoreDisplay(
        values: List<OmMetric.Value<*>>,
        modifiers: List<OmMetric.Modifier>,
        format: StringFormat
    ): String {
        val parts = values.mapNotNullTo(mutableListOf()) { it.describe(this, format) }
        val modifierPart = modifiers.mapNotNull { it.describe(this) }
        if (modifierPart.isNotEmpty())
            parts += modifierPart.joinToString("")
        return parts.joinToString(format.separator)
    }

    /** @return extra descriptions of computed metrics, like ` (g+c+a=215)` */
    private fun additionalMetricDescriptions(context: DisplayContext<OmCategory>): String = when (context.format) {
        StringFormat.DISCORD, StringFormat.PLAIN_TEXT ->
            context.categories
                ?.flatMap { it.metrics }
                ?.filterIsInstance<OmMetric.Computed<*>>()
                ?.distinct()
                ?.mapNotNull { metric -> metric.describe(this, context.format) }
                ?.takeIf { it.isNotEmpty() }
                ?.joinToString(separator = ", ", prefix = " (", postfix = ")")
                .orEmpty()
        else -> ""
    }

    /** `12g-15c-12a-34i[-O][-T]` to comply with Syx's desires */
    fun toMorsString() =
        subScoreDisplay(listOf(OmMetric.COST, OmMetric.CYCLES, OmMetric.AREA, OmMetric.INSTRUCTIONS),
                        listOf(OmMetric.OVERLAP, OmMetric.TRACKLESS),
                        StringFormat.FILE_NAME)

    override fun toString() = toDisplayString()
}
