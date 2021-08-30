package com.faendir.zachtronics.bot.sc.model;

import com.faendir.zachtronics.bot.model.Solution;
import com.faendir.zachtronics.bot.sc.validator.SChem;
import com.faendir.zachtronics.bot.utils.Utils;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Levels with a comma in their name aren't supported */
@Value
public class ScSolution implements Solution {
    @NotNull ScPuzzle puzzle;
    @NotNull ScScore score;
    @NotNull String content;

    @NotNull
    public static ScSolution fromContentNoValidation(@NotNull String content, @Nullable ScPuzzle puzzle,
                                                     @Nullable ScScore score) {
        Matcher m = Pattern.compile("^SOLUTION:(?<puzzle>[^,]+),[^,]+," +
                                    "(?<cycles>\\d+)-(?<reactors>\\d+)-(?<symbols>\\d+)(?:,.+)?$", Pattern.MULTILINE)
                           .matcher(content);
        if (!m.find()) {
            throw new IllegalArgumentException("header");
        }

        ScPuzzle destPuzzle = Objects.requireNonNullElseGet(puzzle, () -> SpaceChem.parsePuzzle(m.group("puzzle")));

        ScScore destScore;
        ScScore contentScore = ScScore.parseSimpleScore(m);
        contentScore.setPrecognitive(!destPuzzle.isDeterministic());
        if (score == null ||
            score.getCycles() != contentScore.getCycles() ||
            score.getReactors() != contentScore.getReactors() ||
            score.getSymbols() != contentScore.getSymbols()) {
            // if no given score or it doesn't match the solution metadata, ignore it
            destScore = contentScore;
        }
        else {
            destScore = score;
        }
        content = m.replaceFirst("SOLUTION:$1,Archiver,$2-$3-$4,Archived Solution");

        return new ScSolution(destPuzzle, destScore, content);
    }

    @NotNull
    public static List<ScSolution> fromExportLink(@NotNull String exportLink, ScPuzzle puzzle, ScScore score) {
        String export;
        try (InputStream is = new URL(Utils.rawContentURL(exportLink)).openStream()) {
            export = new String(is.readAllBytes()).replace("\r\n", "\n");
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Could not parse your link");
        } catch (IOException e) {
            throw new IllegalArgumentException("Couldn't read your solution");
        }

        return SChem.validateMultiExport(export, puzzle, score);
    }
}
