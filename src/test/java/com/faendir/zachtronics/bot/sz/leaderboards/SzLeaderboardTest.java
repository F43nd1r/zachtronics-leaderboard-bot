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

package com.faendir.zachtronics.bot.sz.leaderboards;

import com.faendir.zachtronics.bot.Application;
import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.sz.model.SzCategory;
import com.faendir.zachtronics.bot.sz.model.SzPuzzle;
import com.faendir.zachtronics.bot.sz.model.SzRecord;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@BotTest(Application.class)
public class SzLeaderboardTest {

    @Autowired
    private SzGitLeaderboard szLeaderboard;

    @Test
    @Disabled("Massive test only for manual testing")
    public void testFullIO() {
        for (SzPuzzle p : SzPuzzle.values()) {
            List<SzCategory> categories = Arrays.stream(SzCategory.values())
                    .filter(c -> c.supportsPuzzle(p))
                    .toList();
            Collection<SzRecord> records = new HashSet<>(szLeaderboard.getAll(p, categories).values());
            for (SzRecord r : records)
                szLeaderboard.update(p, r);

            System.out.println("Done " + p.getDisplayName());
        }
        System.out.println("Done");
    }

}
