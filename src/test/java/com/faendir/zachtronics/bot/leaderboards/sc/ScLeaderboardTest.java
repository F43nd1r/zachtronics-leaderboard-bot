package com.faendir.zachtronics.bot.leaderboards.sc;

import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.model.sc.ScCategory;
import com.faendir.zachtronics.bot.model.sc.ScPuzzle;
import com.faendir.zachtronics.bot.model.sc.ScRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@BotTest
public class ScLeaderboardTest {

    @Autowired
    private ScRedditLeaderboard scLeaderboard;

    @Test
    public void testGoodRecords() {
        ScRecord goodRecord = scLeaderboard.get(ScPuzzle.research_example_1, ScCategory.C);
        System.out.println(goodRecord);
        goodRecord = scLeaderboard.get(ScPuzzle.published_1_1, ScCategory.S);
        System.out.println(goodRecord);
        goodRecord = scLeaderboard.get(ScPuzzle.published_101_3, ScCategory.RC);
        System.out.println(goodRecord);
        assertNotNull(goodRecord);
    }

    @Test
    public void testBadRecord() {
        ScRecord badRecord = scLeaderboard.get(ScPuzzle.research_example_1, ScCategory.RC);
        assertNull(badRecord);
        badRecord = scLeaderboard.get(ScPuzzle.bonding_7, ScCategory.RCNB);
        assertNull(badRecord);
    }

    @Test
    public void testFullIO() {
        for (ScPuzzle p : ScPuzzle.values()) {
            for (ScCategory c : ScCategory.values()) {
                if (c.supportsPuzzle(p)) {
                    ScRecord r = scLeaderboard.get(p, c);
                    if (r != null)
                        scLeaderboard.update(p, r);
                }
            }
            System.out.println("Done " + p.getDisplayName());
        }
    }

}
