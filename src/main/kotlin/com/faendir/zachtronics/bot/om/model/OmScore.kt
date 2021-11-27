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

import com.faendir.zachtronics.bot.model.DisplayContext
import com.faendir.zachtronics.bot.model.Score
import com.faendir.zachtronics.bot.model.StringFormat
import kotlinx.serialization.Serializable
import kotlin.reflect.full.memberProperties

@Serializable
data class OmScore(
    val cost: Int? = null,
    val cycles: Int? = null,
    val area: Int? = null,
    val instructions: Int? = null,
    val height: Int? = null,
    val width: Double? = null,
    val rate: Double? = null,
    val trackless: Boolean = false,
    val overlap: Boolean = false,
) : Score<OmCategory> {

    override fun toDisplayString(context: DisplayContext<OmCategory>): String =
        (getMetricDescriptions(context) + getModifierDescriptions(context)).joinToString(context.separator) + getAdditionalMetricDescriptions(context)

    private fun getMetricDescriptions(context: DisplayContext<OmCategory>): List<String> =
        (context.categories?.takeIf { it.isNotEmpty() }?.flatMap { it.requiredParts }?.distinct() ?: OmScorePart.values().toList())
            .mapNotNull { part -> part.format(this)?.let { if (context.format == StringFormat.FILE_NAME) it.replace("∞", "INF") else it } }

    private fun getModifierDescriptions(context: DisplayContext<OmCategory>): List<String> = when (context.format) {
        StringFormat.DISCORD, StringFormat.PLAIN_TEXT, StringFormat.FILE_NAME ->
            listOfNotNull("O".takeIf { overlap }, "T".takeIf { trackless })
        else -> emptyList()
    }

    private fun getAdditionalMetricDescriptions(context: DisplayContext<OmCategory>): String = when (context.format) {
        StringFormat.DISCORD, StringFormat.PLAIN_TEXT ->
            context.categories
                ?.flatMap { it.metrics.toList() }
                ?.distinct()
                ?.mapNotNull { metric -> metric.describe(this) }
                ?.takeIf { it.isNotEmpty() }
                ?.joinToString(separator = ", ", prefix = " (", postfix = ")")
        else -> null
    }.orEmpty()

    fun isSupersetOf(other: OmScore): Boolean {
        var hasMoreData = false
        for (property in OmScore::class.memberProperties) {
            val thisValue = property(this)
            val otherValue = property(other)
            if (thisValue != otherValue) {
                if (otherValue == null) {
                    hasMoreData = true
                } else {
                    return false
                }
            }
        }
        return hasMoreData
    }

    fun isStrictlyBetterThan(other: OmScore): Boolean {
        if ((overlap && !other.overlap) || (!trackless && other.trackless)) return false
        val compares = OmScorePart.values().map { it.getValue }
            .filter { it(other) != null }
            .map { it(this)?.toDouble()?.compareTo(it(other)!!.toDouble()) }
        return compares.none { it == null } && compares.none { it!! > 0 } && compares.any { it!! < 0 }
    }
}