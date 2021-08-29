package com.faendir.zachtronics.bot.sc.model;

import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.sc.SpaceChemMarker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@BotTest(SpaceChemMarker.SpaceChemConfiguration.class)
class ScSolutionTest {
    @Test
    public void testCoherentContentScore() {
        ScSolution s1 = new ScSolution(new ScScore(50, 50, 50, true, false),
                                       "SOLUTION:Of Pancakes and Spaceships,12345ieee,50-50-50\nstuff...");
        ScSolution s2 = new ScSolution(new ScScore(50, 50, 50, false, false),
                                       "SOLUTION:Of Pancakes and Spaceships,12345ieee,50-50-50\nstuff...");
        ScSolution s3 = new ScSolution(null,
                                       "SOLUTION:Of Pancakes and Spaceships,12345ieee,50-50-50\nstuff...");
        assertNotEquals(s1, s2);
        assertEquals(s1, s3);
    }

    @Test
    public void testIncoherentContentScore() {
        ScSolution s1 = new ScSolution(new ScScore(1, 1, 1, false, false),
                                       "SOLUTION:Of Pancakes and Spaceships,12345ieee,50-50-50\nstuff...");
        ScSolution s2 = new ScSolution(null,
                                       "SOLUTION:Of Pancakes and Spaceships,12345ieee,50-50-50\nstuff...");
        assertEquals(s1, s2);
    }

    @Test
    public void testInvalidContent() {
        ScScore score = new ScScore(50, 50, 50, false, false);
        assertBreaks(score, "SOLUTION:No puzzle that exists,12345ieee,50-50-50\nstuff...");
        assertBreaks(score, "stuff...\nSOLUTION:Of Pancakes and Spaceships,12345ieee,50-50-50\nstuff...");
    }

    private static void assertBreaks(ScScore score, String content) {
        assertThrows(IllegalArgumentException.class, () -> new ScSolution(score, content));
    }
}