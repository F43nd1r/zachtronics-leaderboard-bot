package com.faendir.zachtronics.bot.leaderboards.sc;

import com.faendir.zachtronics.bot.leaderboards.UpdateResult;
import com.faendir.zachtronics.bot.model.sc.ScCategory;
import com.faendir.zachtronics.bot.model.sc.ScPuzzle;
import com.faendir.zachtronics.bot.model.sc.ScRecord;
import com.faendir.zachtronics.bot.model.sc.ScScore;
import com.faendir.zachtronics.bot.reddit.RedditService;
import com.faendir.zachtronics.bot.reddit.Subreddit;
import lombok.RequiredArgsConstructor;
import net.dean.jraw.models.WikiPage;
import net.dean.jraw.references.WikiReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ScRedditLeaderboard extends AbstractScLeaderboard {

    @Autowired
    private final RedditService redditService;

    @Nullable
    @Override
    public ScRecord get(@NotNull ScPuzzle puzzle, @NotNull ScCategory category) {
        WikiReference wiki = redditService.subreddit(Subreddit.SPACECHEM).wiki();
        WikiPage page = wiki.page(puzzle.getGroup().getWikiPage());
        String[] lines = page.getContent().split("\\r?\\n");
        return super.get(lines, puzzle, category);
    }

    @NotNull
    @Override
    public UpdateResult<ScCategory, ScScore> update(@NotNull ScPuzzle puzzle, @NotNull ScRecord record) {
        WikiReference wiki = redditService.subreddit(Subreddit.SPACECHEM).wiki();
        WikiPage page = wiki.page(puzzle.getGroup().getWikiPage());
        String[] lines = page.getContent().split("\\r?\\n");

        Map<ScCategory, ScScore> beatenScores = super.update(lines, puzzle, record);

        if (!beatenScores.isEmpty()) {
            wiki.update(puzzle.getGroup().getWikiPage(), String.join("\r\n", lines),
                        puzzle.getDisplayName() + " " + record.getScore().toDisplayString() + " by " +
                        record.getAuthor());
            return new UpdateResult.Success<>(beatenScores);
        }
        else {
            return new UpdateResult.BetterExists<>(Collections.<ScCategory, ScScore>emptyMap());
        }
    }
}
