/*
 * Copyright (c) 2022
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

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Collects the metadata info of a CE export, which are stored in its header, like:<br>
 * <tt>SOLUTION:No Employment Record Found,12345ieee,1284-1-16,Unnamed Solution</tt> */
@Value
public class ScSolutionMetadata {
    @NotNull ScPuzzle puzzle;
    @NotNull String author;
    @NotNull ScScore score;
    /** Contains flags at start if any */
    String description;

    public static final Pattern SOLUTION_NAME_REGEX = Pattern.compile(
            "'?(?:[/-](?<Bflag>[bB])?(?<Pflag>[pP])?(?:$| ))?.*'?");

    /** <tt>SOLUTION:$puzzle,$author,$c-$r-$s[,$description]</tt> */
    private static final Pattern SOLUTION_HEADER = Pattern.compile(
            "^SOLUTION:(?<puzzle>[^,]+|'(?:[^']|'')+')," +
            "(?<author>[^,]+)," +
            "(?<cycles>\\d+)-(?<reactors>\\d+)-(?<symbols>\\d+)" +
            "(?:,(?<description>" + SOLUTION_NAME_REGEX + "))?$", Pattern.MULTILINE);

    /**
     * @param header can also be the whole export, the first <tt>SOLUTION:</tt> match will dictate the line
     * @throws IllegalArgumentException if we can't correctly parse metadata
     */
    @NotNull
    public static ScSolutionMetadata fromHeader(@NotNull String header, @Nullable ScPuzzle puzzle) throws IllegalArgumentException {
        Matcher m = SOLUTION_HEADER.matcher(header);
        if (!m.find()) {
            throw new IllegalArgumentException("Invalid header");
        }

        if (puzzle == null) {
            String puzzleStr = decode(m.group("puzzle"));
            puzzle = ScPuzzle.parsePuzzle(puzzleStr).orElseThrow();
        }

        String author = decode(m.group("author"));

        ScScore score = ScScore.parseBPScore(m);
        if (score.getCycles() == 0) {
            throw new IllegalArgumentException("Invalid score");
        }

        String description = decode(m.group("description"));

        return new ScSolutionMetadata(puzzle, author, score, description);
    }

    private static String decode(String field) {
        if (field != null && field.matches("'.+'"))
            return field.replaceAll("^'|'$", "").replace("''", "'");
        else
            return field;
    }

    @NotNull
    private static String encode(@NotNull String field) {
        if (field.contains(","))
            return "'" + field.replace("'", "''") + "'";
        else
            return field;
    }

    /**
     *
     * @param description must be decoded
     * @return normalized and cleaned description, not including comma
     */
    @NotNull
    private static String normalizeDescription(@NotNull String description) {
        String descr = description.replace(" (copy)", ""); // try to cut down on duplicate churn
        if (descr.length() > 100) {
            descr = descr.substring(0, 100) + "...";
        }
        return descr;
    }

    private String toHeader() {
        String commaDescr = "";
        if (description != null) {
            commaDescr = "," + encode(normalizeDescription(description));
        }
        return String.format("SOLUTION:%s,%s,%d-%d-%d%s",
                             encode(puzzle.getExportName()), encode(author),
                             score.getCycles(), score.getReactors(), score.getSymbols(), commaDescr);
    }

    public ScSubmission extendToSubmission(String displayLink, @NotNull String data) {
        data = SOLUTION_HEADER.matcher(data).replaceFirst(this.toHeader()); // normalization
        return new ScSubmission(puzzle, score, author, displayLink, data);
    }

}
