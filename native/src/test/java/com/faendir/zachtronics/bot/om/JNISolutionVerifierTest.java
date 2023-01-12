/*
 * Copyright (c) 2023
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

package com.faendir.zachtronics.bot.om;


import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JNISolutionVerifierTest {

    @Test
    void goodAndBad() throws IOException {
        try (InputStream puzzle = getClass().getClassLoader().getResource("P009.puzzle").openStream();
             InputStream solution = getClass().getClassLoader().getResource("Face_Powder_Height_1.solution").openStream();
             JNISolutionVerifier verifier = JNISolutionVerifier.open(puzzle.readAllBytes(), solution.readAllBytes())) {
            // good
            assertEquals(1, verifier.getMetric(OmSimMetric.HEIGHT.INSTANCE));
            // bad on purpose
            assertThrows(OmSimException.class, () -> verifier.getMetric(new OmSimMetric.INSTRUCTIONS_WITH_HOTKEY("wrong!")));
            // ensure error is reset
            assertEquals(1, verifier.getMetric(OmSimMetric.HEIGHT.INSTANCE));
        }
    }
}