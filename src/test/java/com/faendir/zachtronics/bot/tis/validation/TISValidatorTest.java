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

package com.faendir.zachtronics.bot.tis.validation;

import com.faendir.zachtronics.bot.tis.model.TISPuzzle;
import com.faendir.zachtronics.bot.tis.model.TISScore;
import com.faendir.zachtronics.bot.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "libTIS100 doesn't load without lua")
class TISValidatorTest {
    @Test
    public void good() throws IOException {
        ClassPathResource resource = new ClassPathResource(
            "repositories/tis-leaderboard/TIS_100_SEGMENT_MAP/00150/00150.83-8-8.txt");
        String solution = Files.readString(resource.getFile().toPath());

        TISScore expected = new TISScore(83, 8, 8, false, false, false);
        TISScore result = TISValidator.validate(solution, TISPuzzle.SELF_TEST_DIAGNOSTIC);
        assertEquals(expected, result);
    }

    @Test
    public void cheating() {
        String solution = """
                          @0
                          
                          @1
                          MOV 5 ACC
                          SUB UP
                          MOV ACC,RIGHT
                          SUB 10
                          ADD UP
                          NEG
                          MOV ACC,RIGHT
                          
                          @2
                          JRO LEFT
                          MOV UP,NIL
                          MOV UP,NIL
                          MOV UP,NIL
                          MOV UP,NIL
                          N:MOV UP,DOWN
                          MOV UP,DOWN
                          JRO LEFT
                          MOV UP,DOWN
                          MOV UP,DOWN
                          MOV UP,DOWN
                          MOV 0 DOWN
                          B:MOV UP,ACC
                          JNZ B
                          
                          @3
                          
                          
                          @4
                          
                          
                          @5
                          
                          
                          @6
                          MOV UP,DOWN
                          
                          @7
                          
                          
                          @8
                          
                          
                          @9
                          MOV UP,DOWN
                          
                          @10
                          """;

        TISScore expected = new TISScore(113, 4, 23, false, true, false);
        TISScore result = TISValidator.validate(solution, TISPuzzle.SUBSEQUENCE_EXTRACTOR);
        assertEquals(expected, result);
    }

    @Test
    public void timeout() {
        String solution = """
                          @0
                          
                          @1
                          
                          @2
                          
                          @3
                          
                          @4
                          
                          @5
                          
                          @6
                          
                          @7
                          MOV UP DOWN
                          """;

        try {
            TISValidator.validate(solution, TISPuzzle.SELF_TEST_DIAGNOSTIC);
            fail("Validation should fail");
        }
        catch (ValidationException e) {
            assertThat(e.getMessage()).contains("timeout");
        }
    }

    @Test
    public void unparseable() {
        List<Map.Entry<String, String>> solutions = List.of(Map.entry("opcode", "@1\nMUL 2\n"),
                                                            Map.entry("out of range", "@1234\nADD 1\n"),
                                                            Map.entry("duplicate node", "@1\nADD 1\n\n@1\nADD 2\n"));

        for (Map.Entry<String, String> entry: solutions) {
            try {
                TISValidator.validate(entry.getValue(), TISPuzzle.SELF_TEST_DIAGNOSTIC);
                fail("Validation should fail");
            }
            catch (ValidationException e) {
                assertThat(e.getMessage()).contains(entry.getKey());
            }
        }
    }

    @Test
    public void custom() throws IOException {
        // also a fixed image
        ClassPathResource resource = new ClassPathResource(
            "repositories/tis-leaderboard/TOURNAMENT_2018/SPEC65521532/SPEC65521532.1623-6-62.txt");
        String solution = Files.readString(resource.getFile().toPath());

        TISScore expected = new TISScore(1623, 6, 62, false, false, false);
        TISScore result = TISValidator.validate(solution, TISPuzzle.IMAGE_TEST_PATTERN_HWAIR);
        assertEquals(expected, result);
    }
}
