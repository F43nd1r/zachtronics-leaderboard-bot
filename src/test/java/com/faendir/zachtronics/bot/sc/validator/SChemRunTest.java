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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.*;

@BotTest(Application.class)
@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Uses SChem")
class SChemRunTest {

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

        SChemResult result = SChem.run(export);
        SChemResult expected = new SChemResult("QT-1", null, 20, 1, 5, "12345ieee", "s", false,
                                               result.getPrecogExplanation(), null);
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

        result = SChem.run(export);
        expected = new SChemResult("Tunnels I", new int[]{1, 1, 1}, 20, 1, 6, "12345ieee", null, false,
                                   result.getPrecogExplanation(), null);
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
        SChemResult result = SChem.run(export);
        assertNotNull(result.getError());
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
        SChemResult result = SChem.run(export);
        assertNotNull(result.getError());
    }

    @Test
    public void runPrecog() {
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

        SChemResult result = SChem.run(export);
        SChemResult expected = new SChemResult("Freon", new int[]{1, 10, 2}, 112, 1, 63, "Archiver",
                                               "/P Archived Solution",
                                               // no need to care about the specific precog details
                                               true, result.getPrecogExplanation(), null);
        assertEquals(expected, result);
    }
}