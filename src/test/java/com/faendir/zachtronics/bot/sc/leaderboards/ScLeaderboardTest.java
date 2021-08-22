package com.faendir.zachtronics.bot.sc.leaderboards;

import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.model.UpdateResult;
import com.faendir.zachtronics.bot.sc.SpaceChemMarker;
import com.faendir.zachtronics.bot.sc.model.ScCategory;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.ScRecord;
import com.faendir.zachtronics.bot.sc.model.ScScore;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@BotTest(SpaceChemMarker.SpaceChemConfiguration.class)
public class ScLeaderboardTest {

    @Autowired
    private ScRedditLeaderboard scLeaderboard;

    @Test
    public void testGoodRecords() {
        ScRecord goodRecord = scLeaderboard.get(ScPuzzle.research_example_1, ScCategory.C).block();
        System.out.println(goodRecord);
        goodRecord = scLeaderboard.get(ScPuzzle.published_1_1, ScCategory.S).block();
        System.out.println(goodRecord);
        goodRecord = scLeaderboard.get(ScPuzzle.published_101_3, ScCategory.RC).block();
        System.out.println(goodRecord);
        goodRecord = scLeaderboard.get(ScPuzzle.production_tutorial_1, ScCategory.RC).block();
        System.out.println(goodRecord);
        assertNotNull(goodRecord);
    }

    @Test
    public void testBadRecord() {
        ScRecord badRecord = scLeaderboard.get(ScPuzzle.research_example_1, ScCategory.RC).block();
        assertNull(badRecord);
        badRecord = scLeaderboard.get(ScPuzzle.bonding_7, ScCategory.RCNB).block();
        assertNull(badRecord);
    }

    @Test
    public void testAllRecords() {
        Map<ScCategory, ScRecord> allRecords = scLeaderboard
                .getAll(ScPuzzle.research_example_1, Arrays.asList(ScCategory.values())).block();
        assertNotNull(allRecords);
        System.out.println(allRecords);
        assertEquals(4, allRecords.size());
    }

    @Test
    @Disabled("Massive test only for manual testing")
    public void testFullIO() {
        for (ScPuzzle p : ScPuzzle.values()) {
            List<ScCategory> categories = Arrays.stream(ScCategory.values())
                                                .filter(c -> c.supportsPuzzle(p))
                                                .collect(Collectors.toList());
            Collection<ScRecord> records = new HashSet<>(scLeaderboard.getAll(p, categories).block().values());
            for (ScRecord r : records)
                scLeaderboard.update(p, r).block();

            System.out.println("Done " + p.getDisplayName());
        }
        System.out.println("Done");
    }

    @Test
    public void testUpdate() {
        // (**817**/2/104) | ← | (**819**/2/44)
        ScPuzzle p = ScPuzzle.sensing_4;
        ScScore s = new ScScore(819, 2, 43);
        ScRecord r = new ScRecord(s, "auth", "lnk", false);

        UpdateResult ur = scLeaderboard.update(p, r).block();
        assertTrue(ur instanceof UpdateResult.BetterExists);

        s.setPrecognitive(false);
        ur = scLeaderboard.update(p, r).block();
        assertTrue(ur instanceof UpdateResult.Success);
    }

}
