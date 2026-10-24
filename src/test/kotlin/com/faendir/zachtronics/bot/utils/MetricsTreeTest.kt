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

package com.faendir.zachtronics.bot.utils

import com.faendir.zachtronics.bot.om.model.OmCategory
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo


class MetricsTreeTest {

    @Test
    fun `should collapse metrics if fully present`() {
        val reference = setOf(OmCategory.CG, OmCategory.CA)
        val target = setOf(OmCategory.CG, OmCategory.CA)

        val result = target.smartFormat(reference)

        expectThat(result).isEqualTo("C")
    }

    @Test
    fun `should not collapse metrics if not fully present`() {
        val reference = setOf(OmCategory.CG, OmCategory.CA)
        val target = setOf(OmCategory.CG)

        val result = target.smartFormat(reference)

        expectThat(result).isEqualTo("CG")
    }

    @Test
    fun `should not collapse noCollapse metrics even if fully present`() {
        val reference = setOf(OmCategory.TIC)
        val target = setOf(OmCategory.TIC)

        val result = target.smartFormat(reference)
        expectThat(result).isEqualTo("TI")
    }
}
