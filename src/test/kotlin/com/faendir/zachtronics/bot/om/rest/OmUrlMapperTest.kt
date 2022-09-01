/*
 * Copyright (c) 2022
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

package com.faendir.zachtronics.bot.om.rest

import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmScore
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

internal class OmUrlMapperTest {
    private val urlMapper = OmUrlMapper()

    @Test
    fun `should map old short to long url correctly`() {
        expectThat(urlMapper.map("d5524ee/P103/2265g-47c-1694a-516i-59h-34.5w-1r_ELECTRUM_SEPARATION"))
            .isEqualTo("https://raw.githubusercontent.com/f43nd1r/om-leaderboard/d5524ee/JOURNAL_VIII/ELECTRUM_SEPARATION/2265g-47c-1694a-516i-59h-34.5w-1r_ELECTRUM_SEPARATION.solution")
    }

    @Test
    fun `should map new short to long url correctly`() {
        expectThat(urlMapper.map("d5524ee/P103/2265g-47c-1694a-516i-59h-34.5w-1r"))
            .isEqualTo("https://raw.githubusercontent.com/f43nd1r/om-leaderboard/d5524ee/JOURNAL_VIII/ELECTRUM_SEPARATION/2265g-47c-1694a-516i-59h-34.5w-1r_ELECTRUM_SEPARATION.solution")
    }

    @Test
    fun `should create short url`() {
        expectThat(urlMapper.createShortUrl("d5524ee", OmPuzzle.ELECTRUM_SEPARATION, OmScore(2265, 47, 1694, 516, 59,34.5, 1.0)))
            .isEqualTo("https://zlbb.faendir.com/l/om/d5524ee/P103/2265g-47c-1694a-516i-59h-34.5w-1r")
    }
}