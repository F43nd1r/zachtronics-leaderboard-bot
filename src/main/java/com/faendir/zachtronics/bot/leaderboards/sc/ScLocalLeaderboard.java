package com.faendir.zachtronics.bot.leaderboards.sc;

import com.faendir.zachtronics.bot.leaderboards.UpdateResult;
import com.faendir.zachtronics.bot.model.sc.ScCategory;
import com.faendir.zachtronics.bot.model.sc.ScPuzzle;
import com.faendir.zachtronics.bot.model.sc.ScRecord;
import com.faendir.zachtronics.bot.model.sc.ScScore;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

@RequiredArgsConstructor
public class ScLocalLeaderboard extends AbstractScLeaderboard {

    private final String localWikiPath = "/home/andreas/Progetti/spacechem/wiki/";

    @Nullable
    @Override
    public ScRecord get(@NotNull ScPuzzle puzzle, @NotNull ScCategory category) {
        Path wikiPath = Paths.get(localWikiPath).resolve(puzzle.getGroup().getWikiPage() + ".md");
        String[] lines;
        try {
            lines = Files.lines(wikiPath).toArray(String[]::new);
        } catch (IOException e) {
            e.printStackTrace();
            lines = null;
        }
        return super.get(lines, puzzle, category);
    }

    @NotNull
    @Override
    public UpdateResult<ScCategory, ScScore> update(@NotNull ScPuzzle puzzle, @NotNull ScRecord record) {
        Path wikiPath = Paths.get(localWikiPath).resolve(puzzle.getGroup().getWikiPage() + ".md");
        String[] lines;
        try {
            lines = Files.lines(wikiPath).toArray(String[]::new);
        } catch (IOException e) {
            e.printStackTrace();
            lines = null;
        }

        Map<ScCategory, ScScore> beatenScores = super.update(lines, puzzle, record);

        if (!beatenScores.isEmpty()) {
            try {
                Files.write(wikiPath, Arrays.asList(lines));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new UpdateResult.Success<>(beatenScores);
        }
        else {
            return new UpdateResult.BetterExists<>(Collections.<ScCategory, ScScore>emptyMap());
        }
    }
}
