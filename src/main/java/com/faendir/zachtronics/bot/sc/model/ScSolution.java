package com.faendir.zachtronics.bot.sc.model;

import com.faendir.zachtronics.bot.model.Solution;
import com.faendir.zachtronics.bot.utils.Utils;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** Levels with a comma in their name aren't supported */
@Value
public class ScSolution implements Solution {
    @NotNull ScPuzzle puzzle;
    @NotNull ScScore score;
    /** null content indicates a score-only solution */
    @Nullable String content;

    public ScSolution(@Nullable ScScore score, @NotNull String content) {
        Matcher m = Pattern.compile("^SOLUTION:(?<puzzle>[^,]+),[^,]+," +
                                    "(?<cycles>\\d+)-(?<reactors>\\d+)-(?<symbols>\\d+)(?:,.+)?(\r?\n)")
                           .matcher(content);
        if (!m.find()) {
            throw new IllegalArgumentException("header");
        }

        this.puzzle = ScPuzzle.DISPLAY_NAME_2_PUZZLE.get(m.group("puzzle"));
        if (this.puzzle == null)
            throw new IllegalArgumentException("puzzle");

        ScScore contentScore = ScScore.parseSimpleScore(m);
        contentScore.setPrecognitive(!this.puzzle.isDeterministic());
        if (score == null ||
            score.getCycles() != contentScore.getCycles() ||
            score.getReactors() != contentScore.getReactors() ||
            score.getSymbols() != contentScore.getSymbols()) {
            // if no given score or it doesn't match the solution metadata, ignore it
            this.score = contentScore;
        }
        else {
            this.score = score;
        }
        this.content = m.replaceFirst("SOLUTION:$1,Archiver,$2-$3-$4,Archived Solution$5");
    }

    public ScSolution(@NotNull ScPuzzle puzzle, @NotNull ScScore score) {
        this.puzzle = puzzle;
        this.score = score;
        this.content = null;
    }

    @NotNull
    public static List<ScSolution> fromExportLink(@NotNull String exportLink, ScScore score) {
        String export;
        try (InputStream is = new URL(Utils.rawContentURL(exportLink)).openStream()) {
            export = new String(is.readAllBytes());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Could not parse your link");
        } catch (IOException e) {
            throw new IllegalArgumentException("Couldn't read your solution");
        }

        List<ScSolution> result = fromMultiContent(export, score);
        if (result.isEmpty())
            throw new IllegalArgumentException("Could not parse a valid solution");
        return result;
    }

    @NotNull
    public static List<ScSolution> fromMultiContent(@NotNull String export, ScScore score) {
        // TODO use Stream#mapMulti in java 16
        List<ScSolution> result = new ArrayList<>();
        for (String content : export.split("(?=SOLUTION:)")) {
            try {
                result.add(new ScSolution(score, content));
            } catch (IllegalArgumentException ignored) {

            }
        }
        return result;
    }
}
