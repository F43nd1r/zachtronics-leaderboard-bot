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

package com.faendir.zachtronics.bot.fc.validation;

import com.faendir.zachtronics.bot.BotTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

import static com.faendir.zachtronics.bot.fc.model.FcPuzzle.TWO_TWELVE;
import static org.junit.jupiter.api.Assertions.assertEquals;

@BotTest
@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Uses FoodCourtSim")
class FcSimResultTest {

    @Test
    void validateGood() throws IOException {
        ClassPathResource resource = new ClassPathResource("repositories/fc-leaderboard/CAMPAIGN/TWO_TWELVE/2twelve-8T-65k-16S-4W.solution");
        byte[] data = Files.readAllBytes(resource.getFile().toPath());

        FcSimResult result = validateSingle(data);
        FcSimResult expected = new FcSimResult(TWO_TWELVE.getNumber(), TWO_TWELVE.getDisplayName(), TWO_TWELVE.getInternalName(),
                                               "New Solution 1", null,
                                               true, true,
                                               65, 8, 16, 4,
                                               new String(Base64.getEncoder().encode(data)), null, null);
        assertEquals(expected, result);
    }

    private static FcSimResult validateSingle(byte[] data) {
        return FoodCourtSim.validate(data)[0];
    }
}