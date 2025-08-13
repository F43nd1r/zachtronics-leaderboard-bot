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

package com.faendir.zachtronics.bot.cw.validation;

import com.faendir.zachtronics.bot.cw.model.CwPuzzle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Uses ChipWizardSim")
class CwSimResultTest {

    @Test
    void validateGood() {
        String data = "Volgograd.Solution.12.2 = eNp7xczAIMrCwKjGwMQKwqKMDCyMrAwMIAwCxkwcQMzFgAxAcqI8DIzqXBwY6hWAWBFVOQNMnpEXiQ1SDwAWiwMH";

        CwSimResult result = validateSingle(data);
        CwSimResult expected = new CwSimResult(CwPuzzle.PUZZLE_1_7.getDisplayName(), 12, 2, data.replace(".2", ".0"), // slot normalization
                                               true, false,
                                               19, 2, 5, 4, 7, 0, 6, 0, 3, 3,
                                               9, 12, 37, 4, 3, 12, 21);
        assertEquals(expected, result);
    }

    private static CwSimResult validateSingle(String data) {
        return ChipWizardSim.validate(data)[0];
    }
}