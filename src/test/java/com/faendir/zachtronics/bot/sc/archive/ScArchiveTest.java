package com.faendir.zachtronics.bot.sc.archive;

import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.sc.SpaceChemMarker;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.ScScore;
import com.faendir.zachtronics.bot.sc.model.ScSolution;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@BotTest(SpaceChemMarker.SpaceChemConfiguration.class)
public class ScArchiveTest {

    @Autowired
    private ScArchive archive;

    @Test
    public void testArchive() {
        // we start with a 100/100/100
        ScScore score = new ScScore(100, 100, 100);
        assertEquals(0, doArchive(score).size()); // our score is a BP, fail
        score.setBugged(false);
        assertEquals(0, doArchive(score).size()); // our score is a P, fail
        score.setPrecognitive(false);
        assertEquals(1, doArchive(score).size()); // identical score, replace
        score = new ScScore(10, 100, 1000);
        assertEquals(1, doArchive(score).size()); // new frontier piece
        score = new ScScore(1000, 100, 10);
        assertEquals(1, doArchive(score).size()); // new frontier piece
        score = new ScScore(10, 10, 10);
        assertEquals(2, doArchive(score).size()); // beats them both
        score = new ScScore(20, 20, 20);
        assertEquals(0, doArchive(score).size()); // fails flat
        score.setBugged(false);
        assertEquals(1, doArchive(score).size()); // new non bugged record
    }

    private List<String> doArchive(ScScore score) {
        return archive.archive(new ScSolution(ScPuzzle.research_example_1, score)).block();
    }
}
