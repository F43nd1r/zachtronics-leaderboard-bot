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

import com.faendir.zachtronics.bot.kz.model.KzScore;
import com.faendir.zachtronics.bot.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KzValidatorTest {

    @Test
    void good() throws IOException {
        ClassPathResource resource = new ClassPathResource(
            "repositories/kz-leaderboard/NORMAL_CAMPAIGN/CORPORATE_BINDER/corporate-binder-4-38-130.solution");
        byte[] data = Files.readAllBytes(resource.getFile().toPath());

        KzScore result = KzValidator.validate(data, "someGuy", null).getScore();
        KzScore expected = new KzScore(4, 38, 130);
        assertEquals(expected, result);
    }

    @Test
    void bad() {
        byte[] nonsense = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        assertThrows(ValidationException.class, () -> KzValidator.validate(nonsense, "someGuy", null));
    }

}
