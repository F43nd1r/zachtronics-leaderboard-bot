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

package com.faendir.zachtronics.bot.om.rest

import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmScore
import com.faendir.zachtronics.bot.utils.LevelValue
import com.faendir.zachtronics.bot.utils.toLevelValue
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

internal class OmUrlMapperTest {
    private val urlMapper = OmUrlMapper()

    @Test
    fun `should map old short to long url correctly`() {
        expectThat(urlMapper.map("d5524ee3/P103/ELECTRUM_SEPARATION_2265g-47c-1694a-516i-59h-34.5w-1r"))
            .isEqualTo("https://raw.githubusercontent.com/zachtronics-leaderboards/om-leaderboard/d5524ee3/" +
                       "JOURNAL_VIII/ELECTRUM_SEPARATION/ELECTRUM_SEPARATION_2265g-47c-1694a-516i-59h-34.5w-1r.solution")
    }

    @Test
    fun `should map new short to long url correctly`() {
        expectThat(urlMapper.map("d5524ee3/P103/2265g-47c-1694a-516i-59h-34.5w-1r"))
            .isEqualTo("https://raw.githubusercontent.com/zachtronics-leaderboards/om-leaderboard/d5524ee3/" +
                       "JOURNAL_VIII/ELECTRUM_SEPARATION/ELECTRUM_SEPARATION_2265g-47c-1694a-516i-59h-34.5w-1r.solution")
    }

    @Test
    fun `should create short url`() {
        expectThat(urlMapper.createShortUrl("d5524ee3", OmPuzzle.ELECTRUM_SEPARATION,
            OmScore(
                2265, 516, overlap = false, trackless = false,
                47, 1694, 59, 34.5, 12, looping = true,
                1.0, LevelValue(0, 1800.0), 60.toLevelValue(), 35.toLevelValue(), 12.toLevelValue())
        )).isEqualTo("https://zlbb.faendir.com/l/om/d5524ee3/P103/2265g-516i-47c-1694a-59h-34.5w-12b-1r-1800a-60h-35w-12b-L")
    }
}
