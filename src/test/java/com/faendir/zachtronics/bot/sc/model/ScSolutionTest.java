package com.faendir.zachtronics.bot.sc.model;

import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.sc.SpaceChemMarker;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@BotTest(SpaceChemMarker.SpaceChemConfiguration.class)
class ScSolutionTest {
    @Test
    public void testCoherentContentScore() {
        ScSolution s1 = ScSolution.fromContentNoValidation(
                "SOLUTION:Of Pancakes and Spaceships,12345ieee,50-50-50\nstuff...", null,
                new ScScore(50, 50, 50, true, false));
        ScSolution s2 = ScSolution.fromContentNoValidation(
                "SOLUTION:Of Pancakes and Spaceships,12345ieee,50-50-50\nstuff...", null,
                new ScScore(50, 50, 50, false, false));
        ScSolution s3 = ScSolution.fromContentNoValidation(
                "SOLUTION:Of Pancakes and Spaceships,12345ieee,50-50-50\nstuff...", null, null);
        assertNotEquals(s1, s2);
        assertEquals(s1, s3);
    }

    @Test
    public void testIncoherentContentScore() {
        ScSolution s1 = ScSolution.fromContentNoValidation(
                "SOLUTION:Of Pancakes and Spaceships,12345ieee,50-50-50\nstuff...", null,
                new ScScore(1, 1, 1, false, false));
        ScSolution s2 = ScSolution.fromContentNoValidation(
                "SOLUTION:Of Pancakes and Spaceships,12345ieee,50-50-50\nstuff...", null, null);
        assertEquals(s1, s2);
    }

    @Test
    public void testInvalidContent() {
        ScScore score = new ScScore(50, 50, 50, false, false);
        assertBreaks(score, "SOLUTION:No puzzle that exists,12345ieee,50-50-50\nstuff...");
        assertBreaks(score, "stuff...SOLUTION:Of Pancakes and Spaceships,12345ieee,50-50-50\nstuff...");
    }

    private static void assertBreaks(ScScore score, String content) {
        assertThrows(IllegalArgumentException.class, () -> ScSolution.fromContentNoValidation(content, null, score));
    }
}