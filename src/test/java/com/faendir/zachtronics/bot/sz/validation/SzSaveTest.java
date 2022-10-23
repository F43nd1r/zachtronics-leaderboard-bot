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

package com.faendir.zachtronics.bot.sz.validation;

import com.faendir.zachtronics.bot.sz.model.SzPuzzle;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SzSaveTest {
    @Test
    void unmarshalGood() throws IOException {
        ClassPathResource resource = new ClassPathResource(
                "repositories/sz-leaderboard/first_campaign/fake-surveillance-camera/fake-surveillance-camera-6-57-8.txt");
        String solution = Files.readString(resource.getFile().toPath());
        SzSave save = SzSave.unmarshal(solution);

        assertEquals("Top solution Power", save.getName());
        assertEquals(SzPuzzle.Sz000, save.getPuzzle());
        assertEquals(6, save.cost());
        assertEquals(57, save.getPowerUsage());
        assertEquals(8, save.lines());
    }
}