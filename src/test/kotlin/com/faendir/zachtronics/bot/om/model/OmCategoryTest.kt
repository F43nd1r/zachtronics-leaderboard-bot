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

import com.faendir.zachtronics.bot.om.dummyOmScore
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isTrue

class OmCategoryTest {

    @Test
    fun `ensure manifold consistency`() {
        for (c in OmCategory.entries) {
            for (m in c.metrics) {
                expectThat(c.associatedManifold.scoreParts).contains(m.scoreParts)
            }
        }
    }

    @Test
    fun `ensure admission consistency`() {
        val victoryOnly = dummyOmScore
        for (c in OmCategory.entries) {
            expectThat(c.associatedManifold == OmScoreManifold.VICTORY || !c.supportsScore(victoryOnly)).isTrue()
        }
    }
}