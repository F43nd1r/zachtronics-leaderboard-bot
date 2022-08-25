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

package com.faendir.zachtronics.bot.fp.validator;

import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.fp.model.FpPuzzle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.assertEquals;

@BotTest
@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Uses XBPGHSim")
class SimResultTest {

    @Test
    void validateGood() {
        String data = "Toronto.Solution.1.2 = eNp7zczAIMDAwMDIAAEgmvE/EDCgC6IwYGxkDooCkBHIgkwcDAjAyDAoOGB3QQUAl6EJHw==";

        SimResult result = validateSingle(data);
        SimResult expected = new SimResult(FpPuzzle.NORMAL_1_1.getDisplayName(), 1, 2,
                                           data.replace(".2", ".0"), // slot normalization
                                           true, 5, 0, 8, true, 0, false);
        assertEquals(expected, result);
    }

    private static SimResult validateSingle(String data) {
        return XBPGHSim.validate(data)[0];
    }
}