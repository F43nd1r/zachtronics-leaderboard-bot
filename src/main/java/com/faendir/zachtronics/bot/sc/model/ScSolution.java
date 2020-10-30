package com.faendir.zachtronics.bot.sc.model;

import com.faendir.zachtronics.bot.model.Solution;
import lombok.Value;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Levels with a comma in their name aren't supported */
@Value
public class ScSolution implements Solution {
    ScPuzzle puzzle;
    ScScore score;
    /** null content is possible, it indicates a score-only solution */
    String content;

    public ScSolution(ScPuzzle puzzle, String content) {
        Matcher m = Pattern.compile("(SOLUTION:[^,]+),[^,]+,(?<cycles>\\d+)-(?<reactors>\\d+)-(?<symbols>\\d+),.+")
                           .matcher(content);
        if (m.find()) {
            this.puzzle = puzzle;
            score = ScScore.parseSimpleScore(m);
            this.content = m.replaceFirst("$1,Archiver,$2-$3-$4,Archived Solution");
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    public ScSolution(ScPuzzle puzzle, ScScore score) {
        this.puzzle = puzzle;
        this.score = score;
        this.content = null;
    }
}
