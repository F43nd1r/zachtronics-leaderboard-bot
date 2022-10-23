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

package com.faendir.zachtronics.bot.inf.validation;

import com.faendir.zachtronics.bot.inf.model.IfPuzzle;
import com.faendir.zachtronics.bot.inf.model.IfScore;
import com.faendir.zachtronics.bot.inf.model.IfSubmission;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A solution file is of the form:
 * <pre>
 * InputRate.1-1.1 = 1
 * Solution.1-1.1 = AwAAAAAAAAA=
 * </pre>
 *
 * the solution is a base64 encoded string represented by {@link IfSave}
 */
@Value
public
class IfValidator {
    /** InputRate.4-3.1 = 1 */
    private static final Pattern INPUT_RATE_PATTERN = Pattern.compile("InputRate.(?<id>\\d+-\\db?).(?<slot>\\d) = \\d+");
    /** Solution.4-3.1 = AAAA== */
    private static final Pattern SOLUTION_PATTERN = Pattern.compile(
            "Solution.(?<id>\\d+-\\db?).(?<slot>\\d) = (?<solution>[A-Za-z0-9+/]+={0,2})");

    public static IfSubmission validate(@NotNull String data, @NotNull String author, @NotNull IfScore score,
                                        @NotNull List<String> displayLinks) {
        Iterator<String> it = Pattern.compile("\r?\n").splitAsStream(data).dropWhile(String::isBlank).iterator();
        String inputRateLine = it.next();
        Matcher m = INPUT_RATE_PATTERN.matcher(inputRateLine);
        if (!m.matches())
            throw new IllegalArgumentException("Invalid solution file, first line: \"" + inputRateLine + "\"");

        String id = m.group("id");
        String slot = m.group("slot");
        IfPuzzle puzzle = Arrays.stream(IfPuzzle.values()).filter(p -> p.getId().equals(id)).findFirst().orElseThrow();

        String solutionLine = it.next();
        m = SOLUTION_PATTERN.matcher(solutionLine);
        if (!m.matches())
            throw new IllegalArgumentException("Invalid solution file, solution line: \"" + solutionLine + "\"");

        if (!m.group("id").equals(id) || !m.group("slot").equals(slot))
            throw new IllegalArgumentException("Incoherent solution lines");

        IfSave save = IfSave.unmarshal(m.group("solution"));

        int blockScore = save.blockScore();
        if (blockScore != score.getBlocks())
            throw new IllegalStateException("Solution has " + blockScore + " blocks, score has " + score.getBlocks());
        int footprintBound = save.footprintLowerBound();
        if (footprintBound > score.getFootprint())
            throw new IllegalStateException("Solution has at least " + footprintBound + " footprint, score has " + score.getFootprint());

        data = data.replaceAll("\\." + puzzle.getId() + "\\.\\d", "." + puzzle.getId() + ".0"); // slot normalization
        return new IfSubmission(puzzle, score, author, displayLinks, data);
    }
}
