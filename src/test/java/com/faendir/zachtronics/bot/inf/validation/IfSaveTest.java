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

package com.faendir.zachtronics.bot.inf.validation;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IfSaveTest {

    @Test
    void unmarshalGood() throws IOException {
        ClassPathResource resource = new ClassPathResource("repositories/if-leaderboard/ZONE_1/1-1/106-37-23.txt");
        String solution = Files.readAllLines(resource.getFile().toPath()).get(1).split(" = ")[1];
        IfSave save = IfSave.unmarshal(solution);
        
        assertEquals(3, save.getVersion());
        assertEquals(51, save.getBlocks().length);
        assertEquals(23, save.blockScore());
        assertTrue(37 >= save.footprintLowerBound());
    }
}