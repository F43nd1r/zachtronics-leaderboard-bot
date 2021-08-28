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
        if (score == null) {
            this.score = contentScore;
        }
        else {
            if (score.getCycles() != contentScore.getCycles() ||
                score.getReactors() != contentScore.getReactors() ||
                score.getSymbols() != contentScore.getSymbols()) {
                // if the given score doesn't match the solution metadata, refuse it
                throw new IllegalArgumentException("score");
            }
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
        try (InputStream is = new URL(Utils.rawContentURL(exportLink)).openStream()) {
            String export = new String(is.readAllBytes());
            return Pattern.compile("(?=SOLUTION:)").splitAsStream(export)
                          .map(content -> new ScSolution(score, content))
                          .collect(Collectors.toList());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Could not parse your link");
        } catch (IOException e) {
            throw new IllegalArgumentException("Couldn't read your solution");
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Could not parse a valid solution");
        }
    }
}
