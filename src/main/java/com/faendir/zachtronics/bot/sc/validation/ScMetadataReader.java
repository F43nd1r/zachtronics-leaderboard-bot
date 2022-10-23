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

package com.faendir.zachtronics.bot.sc.validation;

import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.ScScore;
import com.faendir.zachtronics.bot.sc.model.ScSubmission;
import com.faendir.zachtronics.bot.validation.ValidationException;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Collects the metadata info of a CE export, which are stored in its header, like:<br>
 * <tt>SOLUTION:No Employment Record Found,12345ieee,1284-1-16,Unnamed Solution</tt>
 */
@Value
public class ScMetadataReader {
    public static final Pattern SOLUTION_NAME_REGEX = Pattern.compile(
            "'?(?:[/-](?<Bflag>[bB])?(?<Pflag>[pP])?(?:$| ))?.*'?");

    /** <tt>SOLUTION:$puzzle,$author,$c-$r-$s[,$description]</tt> */
    private static final Pattern SOLUTION_HEADER = Pattern.compile(
            "^SOLUTION:(?<puzzle>[^,]+|'(?:[^']|'')+')," +
            "(?<author>[^,]+)," +
            "(?<cycles>\\d+)-(?<reactors>\\d+)-(?<symbols>\\d+)" +
            "(?:,(?<description>" + SOLUTION_NAME_REGEX + "))?$", Pattern.MULTILINE);

    private ScMetadataReader() {};
    
    /**
     * @param data is the whole export, the first <tt>SOLUTION:</tt> match will dictate the metadata line
     * @throws ValidationException if we can't correctly parse metadata
     */
    @NotNull
    public static ScSubmission fromHeader(@NotNull String data, @Nullable ScPuzzle puzzle, String displayLink)
    throws ValidationException {
        Matcher m = SOLUTION_HEADER.matcher(data);
        if (!m.find()) {
            throw new ValidationException("Invalid header");
        }

        if (puzzle == null) {
            String puzzleStr = decode(m.group("puzzle"));
            puzzle = ScPuzzle.findUniqueMatchingPuzzle(puzzleStr);
        }

        String author = decode(m.group("author"));

        ScScore score = ScScore.parseScore(m);
        if (score.getCycles() == 0) {
            throw new ValidationException("Invalid score");
        }

        String description = decode(m.group("description"));
        return createSubmission(puzzle, author, score, description, displayLink, data);
    }

    @NotNull
    public static ScSubmission createSubmission(@NotNull ScPuzzle puzzle, String author, ScScore score, String description,
                                                String displayLink, String data) {
        String commaDescr = "";
        if (description != null) {
            commaDescr = "," + encode(normalizeDescription(description));
        }
        String header = String.format("SOLUTION:%s,%s,%d-%d-%d%s",
                                      encode(puzzle.getExportName()), encode(author),
                                      score.getCycles(), score.getReactors(), score.getSymbols(), commaDescr);

        data = SOLUTION_HEADER.matcher(data).replaceFirst(header); // normalization
        return new ScSubmission(puzzle, score, author, displayLink, data);
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
}
