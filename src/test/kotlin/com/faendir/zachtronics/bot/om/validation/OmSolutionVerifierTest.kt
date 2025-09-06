/*
 * Copyright (c) 2025
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
package com.faendir.zachtronics.bot.om.validation

import com.faendir.zachtronics.bot.om.validation.OmSimMetric.INSTRUCTIONS_WITH_HOTKEY
import com.faendir.zachtronics.bot.validation.ValidationException
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource
import strikt.api.expectThat
import strikt.api.expectThrows


class OmSolutionVerifierTest {
    @Test
    fun goodAndBad() {
        val puzzle = ClassPathResource("om/puzzle/P009.puzzle").inputStream.readAllBytes()
        val solution = ClassPathResource("Face_Powder_Height_1.solution").inputStream.readAllBytes()
        OmSolutionVerifier(puzzle, solution).use { verifier ->
            // good
            expectThat(verifier.getMetric(OmSimMetric.HEIGHT)).equals(1)
            expectThat(verifier.getApproximateMetric(OmSimMetric.PER_REPETITION_SQUARED_AREA)).equals(0.0)
            // bad on purpose
            expectThrows<ValidationException> { verifier.getMetric(INSTRUCTIONS_WITH_HOTKEY("wrong!")) }
            // ensure error is reset
            expectThat(verifier.getMetric(OmSimMetric.HEIGHT)).equals(1)
        }
    }
}