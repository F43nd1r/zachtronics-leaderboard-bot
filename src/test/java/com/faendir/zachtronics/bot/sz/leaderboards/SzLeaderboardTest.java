package com.faendir.zachtronics.bot.sz.leaderboards;

import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.sz.ShenzhenIOMarker;
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
import java.util.stream.Collectors;

@BotTest(ShenzhenIOMarker.ShenzhenIOConfiguration.class)
public class SzLeaderboardTest {

    @Autowired
    private SzGitLeaderboard szLeaderboard;

    @Test
    @Disabled("Massive test only for manual testing")
    public void testFullIO() {
        for (SzPuzzle p : SzPuzzle.values()) {
            List<SzCategory> categories = Arrays.stream(SzCategory.values())
                                                .filter(c -> c.supportsPuzzle(p))
                                                .collect(Collectors.toList());
            Collection<SzRecord> records = new HashSet<>(szLeaderboard.getAll(p, categories).block().values());
            for (SzRecord r : records)
                szLeaderboard.update(p, r).block();

            System.out.println("Done " + p.getDisplayName());
        }
        System.out.println("Done");
    }

}
