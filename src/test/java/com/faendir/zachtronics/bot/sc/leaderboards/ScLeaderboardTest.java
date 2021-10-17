/*
 * Copyright (c) 2021
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.faendir.zachtronics.bot.sc.leaderboards;

import com.faendir.zachtronics.bot.Application;
import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.leaderboards.UpdateResult;
import com.faendir.zachtronics.bot.sc.model.ScCategory;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.ScRecord;
import com.faendir.zachtronics.bot.sc.model.ScScore;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@BotTest(Application.class)
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
        goodRecord = scLeaderboard.get(ScPuzzle.production_tutorial_1, ScCategory.RC);
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
    public void testAllRecords() {
        Map<ScCategory, ScRecord> allRecords = scLeaderboard.getAll(ScPuzzle.research_example_1,
                                                                    Arrays.asList(ScCategory.values()));
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
                                                .toList();
            Collection<ScRecord> records = new HashSet<>(scLeaderboard.getAll(p, categories).values());
            for (ScRecord r : records)
                scLeaderboard.update(p, r);

            System.out.println("Done " + p.getDisplayName());
        }
        System.out.println("Done");
    }

    @Test
    public void testUpdate() {
        // (**817**/2/104) | ← | (**819**/2/44)
        ScPuzzle p = ScPuzzle.sensing_4;
        ScScore s = new ScScore(819, 2, 43, false, true);
        ScRecord r = new ScRecord(s, "auth", "lnk", "alnk", false);

        UpdateResult ur = scLeaderboard.update(p, r);
        assertTrue(ur instanceof UpdateResult.BetterExists);

        s = new ScScore(819, 2, 43, false, false);
        r = new ScRecord(s, "auth", "lnk", "alnk", false);
        ur = scLeaderboard.update(p, r);
        assertTrue(ur instanceof UpdateResult.Success);
    }

}
