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

package com.faendir.zachtronics.bot.sc.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.faendir.zachtronics.bot.sc.model.ScPuzzle.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ScPuzzleTest {
    @Test
    public void testParsePuzzle() {
        List<ScPuzzle> puzzles = List.of(research_example_1, // opas
                                         published_1_1, published_1_2, published_1_3, // Tunnels I*
                                         published_13_2, published_51_2 // Breakdown
        );
        for (ScPuzzle puzzle : puzzles) {
            assertEquals(puzzle, ScPuzzle.parsePuzzle(puzzle.getDisplayName()).orElseThrow());
        }
    }
}