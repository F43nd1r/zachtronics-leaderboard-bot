/*
 * Copyright (c) 2023
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
import com.faendir.zachtronics.bot.validation.ValidationResult;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A general solution file is of the form:
 * <pre>
 * Best.1-1.Blocks = 44
 * Best.1-1.Cycles = 44
 * Best.1-1.Footprint = 47
 * InputRate.1-1.0 = 1
 * InputRate.1-1.1 = 1
 * Last.1-1.0.Blocks = 44
 * Last.1-1.0.Cycles = 44
 * Last.1-1.0.Footprint = 47
 * Solution.1-1.0 = AwAAAAAAAAA=
 * Last.1-1.1.Blocks = 25
 * Last.1-1.1.Cycles = 58
 * Last.1-1.1.Footprint = 76
 * Solution.1-1.1 = AwAAAAAAAAA=
 * </pre>
 *
 * the solution is a base64 encoded string represented by {@link IfSave}
 *
 * The leaderboard stores just the minimal information needed:
 * <pre>
 * InputRate.1-1.1 = 1
 * Solution.1-1.1 = AwAAAAAAAAA=
 * </pre>
 *
 */
public
class IfValidator {
    /** InputRate.4-3.1 = 1 */
    private static final Pattern INPUT_RATE_PATTERN = Pattern.compile("InputRate.(?<idSlot>\\d+-\\db?\\.\\d) = (?<value>\\d+)");
    /** Last.1-1.1.Blocks = 44 */
    private static final Pattern LAST_BLOCKS_PATTERN = Pattern.compile("Last.(?<idSlot>\\d+-\\db?\\.\\d).Blocks = (?<value>\\d+)");
    /** Last.1-1.1.Cycles = 44 */
    private static final Pattern LAST_CYCLES_PATTERN = Pattern.compile("Last.(?<idSlot>\\d+-\\db?\\.\\d).Cycles = (?<value>\\d+)");
    /** Last.1-1.1.Footprint = 47 */
    private static final Pattern LAST_FOOTPRINT_PATTERN = Pattern.compile("Last.(?<idSlot>\\d+-\\db?\\.\\d).Footprint = (?<value>\\d+)");
    /** Solution.4-3.1 = AAAA== */
    private static final Pattern SOLUTION_PATTERN = Pattern.compile(
            "Solution.(?<idSlot>\\d+-\\db?\\.\\d) = (?<value>[A-Za-z0-9+/]+={0,2})");

    public static Collection<ValidationResult<IfSubmission>> validateSavefile(@NotNull String data, @NotNull String author, IfScore score) {
        Map<String, IfSolutionInfo> infosByIdSlot = new HashMap<>(); // 1-1.0 -> {...}

        for (String line: Pattern.compile("\r?\n").split(data)) {
            if (line.isBlank()) continue;
            loadLine(infosByIdSlot, INPUT_RATE_PATTERN.matcher(line), (i, v) -> i.setInputRate(Integer.parseInt(v)));
            loadLine(infosByIdSlot, LAST_BLOCKS_PATTERN.matcher(line), (i, v) -> i.setBlocks(Integer.parseInt(v)));
            loadLine(infosByIdSlot, LAST_CYCLES_PATTERN.matcher(line), (i, v) -> i.setCycles(Integer.parseInt(v)));
            loadLine(infosByIdSlot, LAST_FOOTPRINT_PATTERN.matcher(line), (i, v) -> i.setFootprint(Integer.parseInt(v)));
            loadLine(infosByIdSlot, SOLUTION_PATTERN.matcher(line), IfSolutionInfo::setSolution);
        }
        return infosByIdSlot.entrySet()
                            .stream()
                            .map(e -> validateOne(e.getKey(), e.getValue(), author, score))
                            .toList();
    }

    @NotNull
    private static ValidationResult<IfSubmission> validateOne(@NotNull String idSlot, @NotNull IfSolutionInfo info, String author,
                                                              IfScore score) {
        if (!(info.hasData() && (info.hasScore() || score != null)))
            return new ValidationResult.Unparseable<>("Incomplete data for idSlot: " + idSlot);
        // if we have no score we load it from the file, by extending minimal trust to it
        if (score == null)
            score = new IfScore(info.getCycles(), info.getFootprint(), info.getBlocks(), true, true);
        String id = idSlot.replaceFirst("\\.\\d+", "");
        IfPuzzle puzzle = Arrays.stream(IfPuzzle.values())
                                .filter(p -> p.getId().equals(id))
                                .findFirst().orElseThrow();
        String leaderboardData = String.format("""
                                               InputRate.%s.0 = %d
                                               Solution.%s.0 = %s
                                               """, id, info.getInputRate(), id, info.getSolution());
        // we use a mutable list, as we could fill it later with display links if we have a single valid submission
        IfSubmission submission = new IfSubmission(puzzle, score, author, new ArrayList<>(), leaderboardData);

        IfSave save = IfSave.unmarshal(info.getSolution());
        int blockScore = save.blockScore();
        if (blockScore != score.getBlocks())
            return new ValidationResult.Invalid<>(submission,
                                                  "Solution has " + blockScore + " blocks, score has " + score.getBlocks());
        int footprintBound = save.footprintLowerBound();
        if (footprintBound > score.getFootprint())
            return new ValidationResult.Invalid<>(submission,
                                                  "Solution has at least " + footprintBound + " footprint, score has " +
                                                  score.getFootprint());
        return new ValidationResult.Valid<>(submission);
    }

    private static void loadLine(Map<String, IfSolutionInfo> infosByIdSlot, @NotNull Matcher m,
                                 BiConsumer<IfSolutionInfo, String> loader) {
        if (m.matches()) {
            String idSlot = m.group("idSlot");
            IfSolutionInfo info = infosByIdSlot.computeIfAbsent(idSlot, p -> new IfSolutionInfo());
            String value = m.group("value");
            loader.accept(info, value);
        }
    }
}
