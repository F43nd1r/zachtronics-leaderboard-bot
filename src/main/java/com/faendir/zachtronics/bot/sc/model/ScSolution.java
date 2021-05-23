package com.faendir.zachtronics.bot.sc.model;

import com.faendir.zachtronics.bot.model.Solution;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Levels with a comma in their name aren't supported */
@Value
public class ScSolution implements Solution {
    @NotNull ScPuzzle puzzle;
    @NotNull ScScore score;
    /** null content indicates a score-only solution */
    @Nullable String content;

    public ScSolution(@NotNull ScPuzzle puzzle, ScScore score, @NotNull String content) {
        Matcher m = Pattern.compile("(SOLUTION:[^,]+),[^,]+,(?<cycles>\\d+)-(?<reactors>\\d+)-(?<symbols>\\d+),.+")
                           .matcher(content);
        if (m.find()) {
            this.puzzle = puzzle;
            this.score = score != null ? score : ScScore.parseSimpleScore(m);
            this.content = m.replaceFirst("$1,Archiver,$2-$3-$4,Archived Solution");
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    public ScSolution(@NotNull ScPuzzle puzzle, @NotNull ScScore score) {
        this.puzzle = puzzle;
        this.score = score;
        this.content = null;
    }
}
