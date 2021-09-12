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
import com.faendir.zachtronics.bot.sc.model.ScScore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@BotTest(Application.class)
@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Uses SChem")
class SChemTest {

    @Test
    void runGood() {
        String export = """
                SOLUTION:QT-1,12345ieee,20-1-5,s
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

        SChemResult expected = new SChemResult("QT-1", null, 20, 1, 5, "12345ieee", "s", false);
        SChemResult result = SChem.run(export);
        assertEquals(expected, result);

        export = """
                SOLUTION:Tunnels I,12345ieee,20-1-6
                COMPONENT:'custom-research-reactor',2,0,''
                MEMBER:'instr-start',-90,0,32,3,2,0,0
                MEMBER:'instr-start',180,0,128,1,7,0,0
                MEMBER:'feature-tunnel',-1,0,1,2,1,0,0
                MEMBER:'feature-tunnel',-1,0,1,6,3,0,0
                MEMBER:'instr-input',-1,0,32,3,1,0,0
                MEMBER:'instr-grab',-1,1,32,1,1,0,0
                MEMBER:'instr-arrow',180,0,16,3,1,0,0
                MEMBER:'instr-arrow',0,0,16,1,1,0,0
                MEMBER:'instr-output',-1,0,32,2,1,0,0
                MEMBER:'instr-swap',-1,0,128,0,7,0,0
                PIPE:0,4,1
                PIPE:1,4,2
                """;

        expected = new SChemResult("Tunnels I", new int[]{1, 1, 1}, 20, 1, 6, "12345ieee", null, false);
        result = SChem.run(export);
        assertEquals(expected, result);
    }

    @Test
    void runInvalid() {
        String export = "invalid";
        assertThrows(SChemException.class, () -> SChem.run(export));
    }

    @Test
    void runCrashing() {
        String export = """
                SOLUTION:Of Pancakes and Spaceships,12345ieee,45-1-14
                COMPONENT:'empty-research-reactor',2,0,''
                MEMBER:'instr-start',-90,0,128,2,5,0,0
                MEMBER:'instr-start',-90,0,32,2,2,0,0
                MEMBER:'instr-grab',-1,1,128,2,1,0,0
                MEMBER:'instr-arrow',0,0,64,2,1,0,0
                MEMBER:'instr-arrow',180,0,64,6,1,0,0
                MEMBER:'instr-grab',-1,2,128,6,1,0,0
                MEMBER:'instr-input',-1,0,32,4,1,0,0
                MEMBER:'instr-arrow',0,0,16,2,1,0,0
                MEMBER:'instr-arrow',180,0,16,6,1,0,0
                MEMBER:'instr-grab',-1,2,32,6,1,0,0
                MEMBER:'instr-output',-1,0,128,5,1,0,0
                MEMBER:'instr-rotate',-1,1,128,4,1,0,0
                MEMBER:'instr-rotate',-1,1,32,5,1,0,0
                MEMBER:'instr-input',-1,0,128,2,4,0,0
                MEMBER:'instr-output',-1,0,32,3,1,0,0
                PIPE:0,4,1
                PIPE:1,4,2
                """; // we're missing: MEMBER:'instr-grab',-1,1,32,2,1,0,0
        assertThrows(SChemException.class, () -> SChem.run(export));
    }

    @Test
    void runNotMatching() {
        String export = """
                SOLUTION:Of Pancakes and Spaceships,12345ieee,1000-1-14
                COMPONENT:'empty-research-reactor',2,0,''
                MEMBER:'instr-start',-90,0,128,2,5,0,0
                MEMBER:'instr-start',-90,0,32,2,2,0,0
                MEMBER:'instr-grab',-1,1,128,2,1,0,0
                MEMBER:'instr-arrow',0,0,64,2,1,0,0
                MEMBER:'instr-arrow',180,0,64,6,1,0,0
                MEMBER:'instr-grab',-1,2,128,6,1,0,0
                MEMBER:'instr-input',-1,0,32,4,1,0,0
                MEMBER:'instr-arrow',0,0,16,2,1,0,0
                MEMBER:'instr-arrow',180,0,16,6,1,0,0
                MEMBER:'instr-grab',-1,2,32,6,1,0,0
                MEMBER:'instr-grab',-1,1,32,2,1,0,0
                MEMBER:'instr-output',-1,0,128,5,1,0,0
                MEMBER:'instr-rotate',-1,1,128,4,1,0,0
                MEMBER:'instr-rotate',-1,1,32,5,1,0,0
                MEMBER:'instr-input',-1,0,128,2,4,0,0
                MEMBER:'instr-output',-1,0,32,3,1,0,0
                PIPE:0,4,1
                PIPE:1,4,2
                """; // we've messed up the cycles count
        assertThrows(SChemException.class, () -> SChem.run(export));
    }

    @Test
    public void validatePrecog() {
        String export = """
                SOLUTION:Freon,Archiver,112-1-63,/P Archived Solution
                COMPONENT:'custom-research-reactor',2,0,''
                MEMBER:'instr-start',180,0,128,9,2,0,0
                MEMBER:'instr-start',0,0,32,0,1,0,0
                MEMBER:'feature-bonder',-1,0,1,4,2,0,0
                MEMBER:'feature-bonder',-1,0,1,5,2,0,0
                MEMBER:'feature-bonder',-1,0,1,6,2,0,0
                MEMBER:'feature-bonder',-1,0,1,3,2,0,0
                MEMBER:'instr-input',-1,0,128,8,2,0,0
                MEMBER:'instr-grab',-1,1,32,1,1,0,0
                MEMBER:'instr-arrow',90,0,16,4,2,0,0
                MEMBER:'instr-bond',-1,1,32,4,2,0,0
                MEMBER:'instr-arrow',0,0,16,1,2,0,0
                MEMBER:'instr-input',-1,0,32,4,3,0,0
                MEMBER:'instr-arrow',90,0,16,1,1,0,0
                MEMBER:'instr-arrow',0,0,16,4,4,0,0
                MEMBER:'instr-arrow',-90,0,16,5,4,0,0
                MEMBER:'instr-arrow',0,0,16,5,2,0,0
                MEMBER:'instr-bond',-1,0,32,5,2,0,0
                MEMBER:'instr-rotate',-1,0,32,6,2,0,0
                MEMBER:'instr-grab',-1,2,32,7,2,0,0
                MEMBER:'instr-arrow',-90,0,16,7,2,0,0
                MEMBER:'instr-arrow',180,0,16,7,1,0,0
                MEMBER:'instr-sync',-1,0,32,3,2,0,0
                MEMBER:'instr-sync',-1,0,32,5,0,0,0
                MEMBER:'instr-arrow',-90,0,16,5,1,0,0
                MEMBER:'instr-arrow',90,0,16,4,0,0,0
                MEMBER:'instr-arrow',180,0,16,4,1,0,0
                MEMBER:'instr-grab',-1,1,32,5,1,0,0
                MEMBER:'instr-grab',-1,2,32,4,1,0,0
                MEMBER:'instr-arrow',180,0,64,7,1,0,0
                MEMBER:'instr-arrow',-90,0,64,5,1,0,0
                MEMBER:'instr-arrow',90,0,64,4,0,0,0
                MEMBER:'instr-arrow',180,0,64,4,1,0,0
                MEMBER:'instr-arrow',90,0,64,1,1,0,0
                MEMBER:'instr-arrow',90,0,64,4,2,0,0
                MEMBER:'instr-arrow',0,0,64,4,4,0,0
                MEMBER:'instr-arrow',-90,0,64,5,4,0,0
                MEMBER:'instr-arrow',0,0,64,5,2,0,0
                MEMBER:'instr-arrow',-90,0,64,7,2,0,0
                MEMBER:'instr-sync',-1,0,128,7,1,0,0
                MEMBER:'instr-sync',-1,0,128,4,4,0,0
                MEMBER:'instr-grab',-1,1,128,1,1,0,0
                MEMBER:'instr-grab',-1,1,128,5,1,0,0
                MEMBER:'instr-grab',-1,2,128,4,1,0,0
                MEMBER:'instr-input',-1,0,128,4,3,0,0
                MEMBER:'instr-bond',-1,1,128,4,2,0,0
                MEMBER:'instr-bond',-1,0,128,5,2,0,0
                MEMBER:'instr-rotate',-1,0,128,6,2,0,0
                MEMBER:'instr-grab',-1,2,128,7,2,0,0
                MEMBER:'instr-arrow',0,0,64,1,3,0,0
                MEMBER:'instr-arrow',-90,0,64,7,3,0,0
                MEMBER:'instr-rotate',-1,1,128,7,3,0,0
                MEMBER:'instr-sync',-1,0,128,3,3,0,0
                MEMBER:'instr-toggle',90,0,32,2,2,0,0
                MEMBER:'instr-arrow',-90,0,16,2,3,0,0
                MEMBER:'instr-output',-1,0,32,7,1,0,0
                MEMBER:'instr-sync',-1,0,32,3,3,0,0
                MEMBER:'instr-arrow',-90,0,16,7,3,0,0
                MEMBER:'instr-rotate',-1,1,32,7,3,0,0
                MEMBER:'instr-toggle',0,0,32,2,3,0,0
                MEMBER:'instr-toggle',90,0,128,1,2,0,0
                MEMBER:'instr-arrow',0,0,64,2,3,0,0
                MEMBER:'instr-toggle',90,0,128,2,2,0,0
                MEMBER:'instr-arrow',0,0,64,1,2,0,0
                MEMBER:'instr-toggle',90,0,128,3,2,0,0
                MEMBER:'instr-arrow',0,0,64,3,3,0,0
                MEMBER:'instr-arrow',180,0,16,5,0,0,0
                MEMBER:'instr-arrow',0,0,16,2,2,0,0
                MEMBER:'instr-output',-1,0,32,1,2,0,0
                MEMBER:'instr-arrow',180,0,64,5,0,0,0
                PIPE:0,4,1
                PIPE:1,4,2
                """;

        SChemResult expected = new SChemResult("Freon", new int[]{1, 10, 2}, 112, 1, 63, "Archiver",
                "/P Archived Solution", true);
        SChemResult result = SChem.run(export);
        assertEquals(expected, result);
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

        assertThrows(SChemException.class, () -> SChem.validate(export));
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

        assertThrows(SChemException.class, () -> SChem.validate(export));
    }

    @Test
    public void testArchiveBugged() {
        String export = "SOLUTION:QT-3,Zig,109-1-24,/B telekinesys"; // it doesn't have to run at all

        ScScore expected = new ScScore(109, 1, 24, true, false);
        ScScore result = SChem.validateMultiExport(export, null).get(0).getScore();
        assertEquals(expected, result);
    }
}