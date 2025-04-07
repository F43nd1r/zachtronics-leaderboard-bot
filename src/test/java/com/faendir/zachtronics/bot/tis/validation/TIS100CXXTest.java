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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Uses TIS-100-CXX")
class TIS100CXXTest {
    @Test
    public void good() {
        String solution = """
                @0
                MOV UP, DOWN
                
                @1
                MOV RIGHT, DOWN
                
                @2
                MOV UP, LEFT
                
                @3
                MOV UP, DOWN
                
                @4
                MOV UP, DOWN
                
                @5
                MOV UP, DOWN
                
                @6
                MOV UP, RIGHT
                
                @7
                MOV LEFT, DOWN
                """;

        TISScore expected = new TISScore(83, 8, 8, false, false, false);
        TISScore result = TIS100CXX.validate(solution, TISPuzzle.SELF_TEST_DIAGNOSTIC);
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
        TISScore result = TIS100CXX.validate(solution, TISPuzzle.SUBSEQUENCE_EXTRACTOR);
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
                NOP
                
                @6
                
                @7
                """;

        try {
            TIS100CXX.validate(solution, TISPuzzle.SELF_TEST_DIAGNOSTIC);
            fail("Validation should fail");
        }
        catch (ValidationException e) {
            assertThat(e.getMessage()).contains("timeout");
        }
    }

    @Test
    public void unparseable() {
        String solution = """
                @1234
                MUL 2 # no way
                """;

        assertThrows(ValidationException.class,
                     () -> TIS100CXX.validate(solution, TISPuzzle.SELF_TEST_DIAGNOSTIC));
    }
}