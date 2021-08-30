package com.faendir.zachtronics.bot.sc.validator;

import com.faendir.zachtronics.bot.Application;
import com.faendir.zachtronics.bot.BotTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@BotTest(Application.class)
@Disabled("Uses SChem")
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

        SChemResult expected = new SChemResult("QT-1", null, 20, 1, 5, "12345ieee", "s");
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

        expected = new SChemResult("Tunnels I", new int[]{1, 1, 1}, 20, 1, 6, "12345ieee", null);
        result = SChem.run(export);
        assertEquals(expected, result);
    }

    @Test
    void runInvalid() {
        String export = "invalid";
        assertSChemBreaks(export);
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
        assertSChemBreaks(export);
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
        assertSChemBreaks(export);
    }

    private static void assertSChemBreaks(String export) {
        assertThrows(SChemException.class, () -> SChem.run(export));
    }
}