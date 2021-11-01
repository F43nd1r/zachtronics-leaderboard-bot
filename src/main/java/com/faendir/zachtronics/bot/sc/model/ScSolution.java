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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Value
public class ScSolution implements Solution<ScPuzzle> {
    @NotNull ScPuzzle puzzle;
    @NotNull ScScore score;
    @NotNull String content;

    public static final Pattern SOLUTION_NAME_REGEX = Pattern.compile("'?(?:[/-](?<Bflag>B)?(?<Pflag>P)?(?:$| ))?.*'?");

    /** <tt>SOLUTION:$puzzle,$author,$c-$r-$s[,$description]</tt> */
    public static final Pattern SOLUTION_HEADER = Pattern.compile(
            "^SOLUTION:(?<puzzle>[^,]+|'(?:[^']|'')+')," +
            "(?<author>[^,]+)," +
            "(?<cycles>\\d+)-(?<reactors>\\d+)-(?<symbols>\\d+)" +
            "(?:," + SOLUTION_NAME_REGEX + ")?$", Pattern.MULTILINE);
    /**
     * @throws IllegalArgumentException if we can't correctly parse metadata
     */
    @NotNull
    public static ScSolution fromContentNoValidation(@NotNull String content, @Nullable ScPuzzle puzzle) throws IllegalArgumentException {
        Matcher m = SOLUTION_HEADER.matcher(content);
        if (!m.find()) {
            throw new IllegalArgumentException("Invalid header");
        }

        if (puzzle == null) {
            String rawPuzzle = m.group("puzzle");
            if (rawPuzzle.matches("'.+'"))
                rawPuzzle = rawPuzzle.replaceAll("^'|'$", "").replace("''", "'");
            puzzle = ScPuzzle.parsePuzzle(rawPuzzle).orElseThrow();
        }

        ScScore score = ScScore.parseBPScore(m);
        if (score.getCycles() == 0) {
            throw new IllegalArgumentException("Invalid score");
        }

        String newHeader = m.group().replace(" (copy)", ""); // try to cut down on duplicate churn
        content = m.replaceFirst(newHeader);

        return new ScSolution(puzzle, score, content);
    }

    @NotNull
    public static List<ScSolution> fromExportLink(@NotNull String exportLink, ScPuzzle puzzle, boolean bypassValidation) {
        String export;
        try (InputStream is = new URL(Utils.rawContentURL(exportLink)).openStream()) {
            export = new String(is.readAllBytes()).replace("\r\n", "\n");
            if (export.length() > 0 && export.charAt(0) == '\uFEFF') // remove BOM
                export = export.substring(1);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Could not parse your link");
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read your solution");
        }

        return SChem.validateMultiExport(export, puzzle, bypassValidation);
    }

    public static String authorFromSolutionHeader(String header) {
        Matcher m = SOLUTION_HEADER.matcher(header);
        if (!m.find()) {
            throw new IllegalArgumentException("Invalid header");
        }
        return m.group("author");
    }
}
