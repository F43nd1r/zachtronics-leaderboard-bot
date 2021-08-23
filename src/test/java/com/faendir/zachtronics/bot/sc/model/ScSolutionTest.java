package com.faendir.zachtronics.bot.sc.model;

import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.sc.SpaceChemMarker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@BotTest(SpaceChemMarker.SpaceChemConfiguration.class)
class ScSolutionTest {
    @Test
    public void testCoherentAttachmentScore() {
        ScSolution s1 = new ScSolution(ScPuzzle.research_example_1, new ScScore(50, 50, 50, false, false),
                                       "SOLUTION:Of Pancakes and Spaceships,12345ieee,50-50-50\nstuff...");
        ScSolution s2 = new ScSolution(ScPuzzle.research_example_1, new ScScore(50, 50, 50, false, true),
                                       "SOLUTION:Of Pancakes and Spaceships,12345ieee,50-50-50\nstuff...");
        assertNotEquals(s1, s2);
    }

    @Test
    public void testIncoherentAttachmentScore() {
        ScScore score = new ScScore(50, 50, 50, false, false);
        assertBreaks(ScPuzzle.research_example_1, score,
                     "SOLUTION:Of Pancakes and Spaceships,12345ieee,10-10-10\nstuff...");
        assertBreaks(ScPuzzle.research_example_1, score,
                     "SOLUTION:Slightly Different,12345ieee,50-50-50\nstuff...");
        assertBreaks(ScPuzzle.research_example_1, score,
                     "stuff...\nSOLUTION:Of Pancakes and Spaceships,12345ieee,50-50-50\nstuff...");
    }

    private static void assertBreaks(ScPuzzle puzzle, ScScore score, String content) {
        assertThrows(IllegalArgumentException.class, () -> new ScSolution(puzzle, score, content));
    }
}