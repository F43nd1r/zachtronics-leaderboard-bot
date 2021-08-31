package com.faendir.zachtronics.bot.sc.archive;

import com.faendir.zachtronics.bot.Application;
import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.ScScore;
import com.faendir.zachtronics.bot.sc.model.ScSolution;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@BotTest(Application.class)
public class ScArchiveTest {

    @Autowired
    private ScArchive archive;

    @Test
    public void testArchiveScore() {
        // we start with a 100/100/100
        ScScore score = new ScScore(100, 100, 100, true, true);
        assertEquals("", doArchiveScore(score)); // our score is a BP, fail
        score = new ScScore(100, 100, 100, false, true);
        assertEquals("", doArchiveScore(score)); // our score is a P, fail
        score = new ScScore(100, 100, 100, false, false);
        assertNotEquals("", doArchiveScore(score)); // identical score, different content, accept
        score = new ScScore(10, 100, 1000, true, true);
        assertNotEquals("", doArchiveScore(score)); // new frontier piece
        score = new ScScore(1000, 100, 10, true, true);
        assertNotEquals("", doArchiveScore(score)); // new frontier piece
        score = new ScScore(10, 10, 10, true, true);
        assertNotEquals("", doArchiveScore(score)); // beats them both
        score = new ScScore(20, 20, 20, true, true);
        assertEquals("", doArchiveScore(score)); // fails flat
        score = new ScScore(20, 20, 20, false, true);
        assertNotEquals("", doArchiveScore(score)); // new non bugged record
    }

    @Test
    public void testArchiveAttachment() {
        // we start at 100/100/100
        ScScore score = new ScScore(50, 50, 50, false, false);
        assertNotEquals("", doArchiveScore(score)); // 50/50/50

        String content = "SOLUTION:Of Pancakes and Spaceships,12345ieee,45-1-14\nbunch of stuff...";
        assertNotEquals("", doArchiveContent(content)); // 45/1/14
    }

    private String doArchiveScore(ScScore score) {
        return archive.archive(new ScSolution(ScPuzzle.research_example_1, score, "")).getSecond();
    }

    private String doArchiveContent(String content) {
        return archive.archive(ScSolution.fromContentNoValidation(content, null)).getSecond();
    }
}
