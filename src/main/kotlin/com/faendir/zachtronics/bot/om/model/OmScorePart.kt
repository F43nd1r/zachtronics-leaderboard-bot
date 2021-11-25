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

import java.text.DecimalFormat

enum class OmScorePart(val key: Char, val displayName: String?, val getValue: (OmScore) -> Number?) {
    COST('g', "Cost", OmScore::cost),
    CYCLES('c', "Cycles", OmScore::cycles),
    AREA('a', "Area", OmScore::area),
    INSTRUCTIONS('i', "Instructions", OmScore::instructions),
    HEIGHT('h', "Height", OmScore::height),
    WIDTH('w', "Width", OmScore::width),
    RATE('r', "Rate", OmScore::rate),
    ;

    fun format(score: OmScore) = getValue(score)?.let { numberFormat.format(it) }?.plus(key)

    companion object {
        private val numberFormat = DecimalFormat("0.###")


        fun parse(string: String): Pair<OmScorePart, Double>? {
            if (string.isEmpty()) return null
            val part = string.last().let { key -> values().find { key.equals(it.key, ignoreCase = true) } } ?: return null
            val number = string.substring(0, string.length - 1).toDoubleOrNull() ?: return null
            return part to number
        }
    }
}