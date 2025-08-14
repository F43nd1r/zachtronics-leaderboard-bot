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

package com.faendir.zachtronics.bot.kz.validation;

import com.faendir.zachtronics.bot.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;

import static com.faendir.zachtronics.bot.kz.model.KzPuzzle.CORPORATE_BINDER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Uses kaizensim")
class KaizenSimTest {

    @Test
    void good() throws IOException {
        ClassPathResource resource = new ClassPathResource(
            "repositories/kz-leaderboard/NORMAL_CAMPAIGN/CORPORATE_BINDER/corporate-binder-4-38-130.solution");
        byte[] data = Files.readAllBytes(resource.getFile().toPath());

        KzSimResult result = KaizenSim.validate(data);
        KzSimResult expected = new KzSimResult(4, 38, 130, CORPORATE_BINDER.getId(), false, null);
        assertEquals(expected, result);
    }

    @Test
    void bad() {
        byte[] nonsense = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        KzSimResult result = KaizenSim.validate(nonsense);
        KzSimResult expected = new KzSimResult(null, null, null, null, null, "UnknownVersion(50462976)");
        assertEquals(expected, result);

        assertThrows(ValidationException.class, () -> KaizenSim.validate(nonsense, "someGuy", null));
    }

}