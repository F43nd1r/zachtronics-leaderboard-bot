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
import com.faendir.zachtronics.bot.om.model.OmScoreManifold.VICTORY
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isTrue

class OmCategoryTest {

    @Test
    fun `ensure manifold consistency`() {
        for (cat in OmCategory.entries) {
            for (m in cat.metrics) {
                expectThat(cat.associatedManifold.scoreParts).contains(m.scoreParts)
            }
        }
    }

    @Test
    fun `ensure admission consistency`() {
        val victoryOnly = dummyOmScore
        for (cat in OmCategory.entries) {
            expectThat(cat.associatedManifold == VICTORY || !cat.supportsScore(victoryOnly)).isTrue()
        }
    }

    @Test
    fun `ensure ingame metrics are shown for each category`() {
        for (cat in OmCategory.entries) {
            for (type in cat.supportedTypes) {
                expectThat(cat) {
                    get { requiredParts }.contains(OmMetric.OVERLAP)
                    get { requiredParts }.contains(INGAME_METRICS[cat.associatedManifold]!![type]!!)
                }
            }
        }
    }
}