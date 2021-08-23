package com.faendir.zachtronics.bot.sc.archive;

import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.model.Solution;
import com.faendir.zachtronics.bot.sc.SpaceChemMarker;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.ScScore;
import com.faendir.zachtronics.bot.sc.model.ScSolution;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@BotTest(SpaceChemMarker.SpaceChemConfiguration.class)
public class ScArchiveTest {

    @Autowired
    private ScArchive archive;

    @Test
    public void testArchiveScore() {
        // we start with a 100/100/100
        ScScore score = new ScScore(100, 100, 100);
        assertEquals(0, doArchiveScore(score).size()); // our score is a BP, fail
        score.setBugged(false);
        assertEquals(0, doArchiveScore(score).size()); // our score is a P, fail
        score.setPrecognitive(false);
        assertEquals(1, doArchiveScore(score).size()); // identical score, replace
        score = new ScScore(10, 100, 1000);
        assertEquals(1, doArchiveScore(score).size()); // new frontier piece
        score = new ScScore(1000, 100, 10);
        assertEquals(1, doArchiveScore(score).size()); // new frontier piece
        score = new ScScore(10, 10, 10);
        assertEquals(2, doArchiveScore(score).size()); // beats them both
        score = new ScScore(20, 20, 20);
        assertEquals(0, doArchiveScore(score).size()); // fails flat
        score.setBugged(false);
        assertEquals(1, doArchiveScore(score).size()); // new non bugged record
    }

    @Test
    public void testArchiveAttachment() {
        // we start at 100/100/100
        ScScore score = new ScScore(50, 50, 50);
        assertEquals(1, doArchiveScore(score).size()); // 50/50/50

        String content = "SOLUTION:Of Pancakes and Spaceships,12345ieee,45-1-14\nbunch of stuff...";
        assertEquals(1, doArchiveScoreContent(null, content).size()); // 45/1/14/BP

        score = new ScScore(45, 1, 14);
        assertEquals(1, doArchiveScoreContent(score, content).size()); // 45/1/14
    }

    private List<String> doArchiveScore(ScScore score) {
        return archive.archive(new ScSolution(ScPuzzle.research_example_1, score)).block();
    }

    private List<String> doArchiveScoreContent(ScScore score, String content) {
        return archive.archive(new ScSolution(ScPuzzle.research_example_1, score, content)).block();
    }
}
