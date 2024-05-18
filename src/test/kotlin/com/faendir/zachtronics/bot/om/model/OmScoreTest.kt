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

package com.faendir.zachtronics.bot.om.model

import com.faendir.zachtronics.bot.model.DisplayContext
import com.faendir.zachtronics.bot.model.StringFormat
import com.faendir.zachtronics.bot.utils.InfinInt.Companion.toInfinInt
import com.faendir.zachtronics.bot.utils.LevelValue
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class OmScoreTest {

    @Test
    fun toDisplayString() {
        val score = OmScore(
            0, 1, true, true,
            2, 3, 4, 5.0, 6,
            7.0, LevelValue(0, 8.0), 9.toInfinInt(), 10.0, 11.toInfinInt()
        )
        val human = "0g/2c/3a/1i/4h/5w/6b/O/T/L@V 0g/7r/8a/1i/9h/10w/11b/O/T@∞"
        val machine = "0g-1i-2c-3a-4h-5w-6b-7r-8a0-9h-10w-11b-O-T"
        expectThat(score.toDisplayString()).isEqualTo(human)
        expectThat(score.toDisplayString(DisplayContext(StringFormat.PLAIN_TEXT, emptySet()))).isEqualTo(human)
        expectThat(score.toDisplayString(DisplayContext.fileName())).isEqualTo(machine)
    }
}