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

package com.faendir.zachtronics.bot.exa.validation;

import com.faendir.zachtronics.bot.exa.model.ExaPuzzle;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExaSaveTest {
    @Test
    void unmarshalGood() throws IOException {
        ClassPathResource resource = new ClassPathResource(
            "repositories/exa-leaderboard/MAIN_CAMPAIGN/PB000/trash-world-news-4-3-2.solution");
        byte[] solution = Files.readAllBytes(resource.getFile().toPath());
        ExaSave save = ExaSave.unmarshal(solution);

        assertEquals("ALL", save.getName());
        assertEquals(ExaPuzzle.PB000.name(), save.getPuzzle());
        assertEquals(4, save.getCycles());
        assertEquals(3, save.getSize());
        assertEquals(3, save.actualSize());
        assertEquals(2, save.getActivity());
    }
}