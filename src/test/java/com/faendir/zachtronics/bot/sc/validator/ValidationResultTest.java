/*
 * Copyright (c) 2021
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

package com.faendir.zachtronics.bot.sc.validator;

import com.faendir.zachtronics.bot.Application;
import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.ScScore;
import com.faendir.zachtronics.bot.sc.model.ScSubmission;
import com.faendir.zachtronics.bot.validation.ValidationResult;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.*;

@BotTest(Application.class)
@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Uses SChem")
class ValidationResultTest {

    @Test
    public void importError() {
        String export1 = "Invalid";
        assertThrows(SChemException.class, () -> validateSingle(export1));

        String export = "SOLUTION:Of Pancakes and Spaceships,12345ieee,115-1-6,unparseable\nInvalid";
        assertTrue(validateSingle(export) instanceof ValidationResult.Unparseable);
    }

    @Test
    public void incoherentPrecog() {
        String export = """
                SOLUTION:An Introduction to Sensing,12345ieee,236-1-11,/P precog, but not really
                COMPONENT:'drag-advanced-reactor',2,0,''
                MEMBER:'instr-start',-90,0,128,0,2,0,0
                MEMBER:'instr-start',-90,0,32,6,5,0,0
                MEMBER:'feature-bonder',-1,0,1,4,5,0,0
                MEMBER:'feature-bonder',-1,0,1,5,5,0,0
                MEMBER:'feature-bonder',-1,0,1,4,6,0,0
                MEMBER:'feature-bonder',-1,0,1,5,6,0,0
                MEMBER:'feature-sensor',-1,0,1,5,1,0,0
                MEMBER:'instr-input',-1,0,128,0,1,0,0
                MEMBER:'instr-arrow',0,0,64,0,1,0,0
                MEMBER:'instr-grab',-1,1,128,1,1,0,0
                MEMBER:'instr-arrow',180,0,64,6,1,0,0
                MEMBER:'instr-grab',-1,2,128,6,1,0,0
                MEMBER:'instr-output',-1,0,128,5,1,0,0
                MEMBER:'instr-sensor',90,0,32,6,0,0,18
                MEMBER:'instr-grab',-1,1,32,6,1,0,0
                MEMBER:'instr-grab',-1,2,32,6,4,0,0
                MEMBER:'instr-arrow',-90,0,16,6,4,0,0
                MEMBER:'instr-output',-1,1,32,6,3,0,0
                PIPE:0,4,1
                PIPE:1,4,2
                """;

        assertTrue(validateSingle(export) instanceof ValidationResult.Invalid);
    }

    @Test
    void incoherentBugged() {
        String export = """
                SOLUTION:QT-1,12345ieee,20-1-5,/B bugged, but not really
                COMPONENT:'drag-quantum-reactor-x',2,0,''
                MEMBER:'instr-start',180,0,128,4,1,0,0
                MEMBER:'instr-start',180,0,32,1,7,0,0
                MEMBER:'feature-tunnel',-1,0,1,1,1,0,0
                MEMBER:'feature-tunnel',-1,0,1,7,5,0,0
                MEMBER:'instr-input',-1,0,128,3,1,0,0
                MEMBER:'instr-swap',-1,0,128,2,1,0,0
                MEMBER:'instr-arrow',0,0,64,2,1,0,0
                MEMBER:'instr-arrow',180,0,64,3,1,0,0
                MEMBER:'instr-output',-1,1,32,0,7,0,0
                PIPE:0,4,1
                PIPE:1,4,2
                """;

        assertTrue(validateSingle(export) instanceof ValidationResult.Invalid);
    }

    @Test
    void timeout() {
        String export = """
                SOLUTION:Of Pancakes and Spaceships,12345ieee,100-1-0,fails timeout
                COMPONENT:'empty-research-reactor',2,0,''
                MEMBER:'instr-start',0,0,128,0,1,0,0
                MEMBER:'instr-start',180,0,32,1,1,0,0
                PIPE:0,4,1
                PIPE:1,4,2
                """;

        assertTrue(validateSingle(export) instanceof ValidationResult.Unparseable);
    }

    @Test
    void duplicateLevelName() {
        String export = """
                SOLUTION:Pyridine,12345ieee,123-1-29
                COMPONENT:'custom-research-reactor',2,0,''
                MEMBER:'instr-start',-90,0,128,6,4,0,0
                MEMBER:'instr-start',90,0,32,0,1,0,0
                MEMBER:'feature-bonder',-1,0,1,7,4,0,0
                MEMBER:'feature-bonder',-1,0,1,1,6,0,0
                MEMBER:'feature-bonder',-1,0,1,2,5,0,0
                MEMBER:'feature-bonder',-1,0,1,7,3,0,0
                MEMBER:'feature-bonder',-1,0,1,3,5,0,0
                MEMBER:'feature-bonder',-1,0,1,1,5,0,0
                MEMBER:'feature-bonder',-1,0,1,2,4,0,0
                MEMBER:'feature-bonder',-1,0,1,9,7,0,0
                MEMBER:'instr-grab',-1,0,32,0,2,0,0
                MEMBER:'instr-arrow',0,0,16,0,2,0,0
                MEMBER:'instr-arrow',90,0,16,2,2,0,0
                MEMBER:'instr-bond',-1,1,32,2,5,0,0
                MEMBER:'instr-bond',-1,1,128,5,0,0,0
                MEMBER:'instr-arrow',180,0,16,2,6,0,0
                MEMBER:'instr-input',-1,1,128,4,0,0,0
                MEMBER:'instr-arrow',-90,0,16,0,6,0,0
                MEMBER:'instr-bond',-1,1,128,4,1,0,0
                MEMBER:'instr-grab',-1,0,32,1,6,0,0
                MEMBER:'instr-bond',-1,1,32,2,6,0,0
                MEMBER:'instr-bond',-1,0,128,4,2,0,0
                MEMBER:'instr-bond',-1,0,32,0,5,0,0
                MEMBER:'instr-bond',-1,0,32,0,6,0,0
                MEMBER:'instr-bond',-1,1,128,5,1,0,0
                MEMBER:'instr-grab',-1,0,128,4,3,0,0
                MEMBER:'instr-arrow',0,0,64,4,3,0,0
                MEMBER:'instr-output',-1,1,32,1,2,0,0
                MEMBER:'instr-output',-1,0,32,2,2,0,0
                MEMBER:'instr-arrow',-90,0,64,5,3,0,0
                MEMBER:'instr-arrow',0,0,64,5,1,0,0
                MEMBER:'instr-rotate',-1,1,128,5,2,0,0
                MEMBER:'instr-bond',-1,1,128,5,3,0,0
                MEMBER:'instr-grab',-1,0,128,6,1,0,0
                MEMBER:'instr-arrow',-90,0,64,6,1,0,0
                MEMBER:'instr-arrow',90,0,64,4,0,0,0
                MEMBER:'instr-input',-1,0,32,0,3,0,0
                MEMBER:'instr-toggle',180,0,128,6,0,0,0
                MEMBER:'instr-input',-1,0,128,6,3,0,0
                PIPE:0,4,1
                PIPE:1,4,2
                """;

        ScPuzzle result = validateSingle(export).getSubmission().getPuzzle();
        assertEquals(ScPuzzle.published_13_1, result);
    }

    @Test
    public void testArchiveBugged() {
        String export = "SOLUTION:QT-3,Zig,109-1-0,/B telekinesys"; // it doesn't have to run at all

        ScScore expected = new ScScore(109, 1, 0, true, false);
        ScScore result = SChem.validateMultiExport(export, true).iterator().next().getSubmission().getScore();
        assertEquals(expected, result);
    }

    @NotNull
    private static ValidationResult<ScSubmission> validateSingle(String export) {
        return SChem.validationResultFrom(SChem.validate(export, false)[0], false);
    }
}