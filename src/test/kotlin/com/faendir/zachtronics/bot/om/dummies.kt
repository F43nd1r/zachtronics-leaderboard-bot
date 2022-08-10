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

package com.faendir.zachtronics.bot.om

import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmScore
import com.faendir.zachtronics.bot.om.model.OmSubmission

fun dummyOmSubmission(
    puzzle: OmPuzzle = OmPuzzle.STABILIZED_WATER,
    score: OmScore = OmScore(),
    author: String? = null,
    displayLink: String = "https://no.link",
    displayData: ByteArray? = null,
    wantedGifCycles: Pair<Int, Int> = 0 to 1,
    data: ByteArray = ByteArray(0)
) = OmSubmission(puzzle, score, author, displayLink, displayData, wantedGifCycles, data)