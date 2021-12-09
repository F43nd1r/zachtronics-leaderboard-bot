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

package com.faendir.zachtronics.bot.om.rest.dto

import com.faendir.zachtronics.bot.om.model.OmScore

data class OmScoreDTO(
    val cost: Int? = null,
    val cycles: Int? = null,
    val area: Int? = null,
    val instructions: Int? = null,
    val height: Int? = null,
    val width: Double? = null,
    val rate: Double? = null,
    val trackless: Boolean = false,
    val overlap: Boolean = false,
)

fun OmScore.toDTO() = OmScoreDTO(
    cost = cost,
    cycles = cycles,
    area = area,
    instructions = instructions,
    height = height,
    width = width,
    rate = rate,
    trackless = trackless,
    overlap = overlap
)