package com.faendir.zachtronics.bot.sc.archive;

import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.sc.SpaceChemMarker;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.ScScore;
import com.faendir.zachtronics.bot.sc.model.ScSolution;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@BotTest(SpaceChemMarker.SpaceChemConfiguration.class)
public class ScArchiveTest {

    @Autowired
    private ScArchive archive;

    @Test
    public void testArchiveScore() {
        // we start with a 100/100/100
        ScScore score = new ScScore(100, 100, 100);
        assertEquals("", doArchiveScore(score)); // our score is a BP, fail
        score.setBugged(false);
        assertEquals("", doArchiveScore(score)); // our score is a P, fail
        score.setPrecognitive(false);
        assertEquals("", doArchiveScore(score)); // identical score, fail
        score = new ScScore(10, 100, 1000);
        assertNotEquals("", doArchiveScore(score)); // new frontier piece
        score = new ScScore(1000, 100, 10);
        assertNotEquals("", doArchiveScore(score)); // new frontier piece
        score = new ScScore(10, 10, 10);
        assertNotEquals("", doArchiveScore(score)); // beats them both
        score = new ScScore(20, 20, 20);
        assertEquals("", doArchiveScore(score)); // fails flat
        score.setBugged(false);
        assertNotEquals("", doArchiveScore(score)); // new non bugged record
    }

    @Test
    public void testArchiveAttachment() {
        // we start at 100/100/100
        ScScore score = new ScScore(50, 50, 50);
        assertNotEquals("", doArchiveScore(score)); // 50/50/50

        String content = "SOLUTION:Of Pancakes and Spaceships,12345ieee,45-1-14\nbunch of stuff...";
        assertNotEquals("", doArchiveScoreContent(null, content)); // 45/1/14/B

        score = new ScScore(45, 1, 14, false, false);
        assertNotEquals("", doArchiveScoreContent(score, content)); // 45/1/14
    }

    private String doArchiveScore(ScScore score) {
        return archive.archive(new ScSolution(ScPuzzle.research_example_1, score)).getSecond();
    }

    private String doArchiveScoreContent(ScScore score, String content) {
        return archive.archive(new ScSolution(score, content)).getSecond();
    }
}
