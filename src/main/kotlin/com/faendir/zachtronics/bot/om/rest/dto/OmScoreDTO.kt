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

package com.faendir.zachtronics.bot.om.rest.dto

import com.faendir.zachtronics.bot.om.model.OmScore

data class OmScoreDTO(
    // @0
    val cost: Int,
    val instructions: Int,
    val overlap: Boolean,
    val trackless: Boolean,

    // @V
    val cycles: Int,
    val area: Int,
    val height: Int?,
    val width: Double?,
    val boundingHex: Int?,

    // @INF
    val rate: Double?,
    val areaINFLevel: Int?,
    val areaINFValue: Double?,
    val heightINF: Double?,
    val widthINF: Double?,
    val boundingHexINF: Double?
)

fun OmScore.toDTO() = OmScoreDTO(
    cost = cost,
    instructions = instructions,
    trackless = trackless,
    overlap = overlap,

    cycles = cycles,
    area = area,
    height = height,
    width = width,
    boundingHex = boundingHex,

    rate = rate,
    areaINFLevel = areaINF?.level,
    areaINFValue = areaINF?.value,
    heightINF = heightINF?.toDouble(),
    widthINF = widthINF,
    boundingHexINF = boundingHexINF?.toDouble(),
)