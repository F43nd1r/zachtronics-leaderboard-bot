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

package com.faendir.zachtronics.bot.sc.repository;

import com.faendir.zachtronics.bot.repository.CategoryRecord;
import com.faendir.zachtronics.bot.repository.Solution;
import com.faendir.zachtronics.bot.sc.model.ScCategory;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.ScRecord;
import com.faendir.zachtronics.bot.sc.model.ScScore;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Value
public class ScSolution implements Solution<ScCategory, ScPuzzle, ScScore, ScRecord> {
    @NotNull ScScore score;
    @NotNull String author;
    String displayLink;
    boolean oldVideoRNG;
    /** empty if it holds no categories */
    EnumSet<ScCategory> categories = EnumSet.noneOf(ScCategory.class);

    public ScRecord extendToRecord(ScPuzzle puzzle, String dataLink, Path dataPath) {
        if (dataPath != null && Files.exists(dataPath))
            return new ScRecord(puzzle, score, author, displayLink, oldVideoRNG, dataLink, dataPath);
        else
            return new ScRecord(puzzle, score, author, displayLink, oldVideoRNG, null, null);
    }

    @Override
    public CategoryRecord<ScRecord, ScCategory> extendToCategoryRecord(ScPuzzle puzzle, String dataLink, Path dataPath) {
        return new CategoryRecord<>(extendToRecord(puzzle, dataLink, dataPath), categories);
    }

    @NotNull
    public static ScSolution unmarshal(@NotNull String[] fields) {
        assert fields.length == 5;
        ScScore score = Objects.requireNonNull(ScScore.parseBPScore(fields[0]));
        String author = fields[1];
        String displayLink = fields[2];
        boolean oldVideoRNG = fields[3] != null;
        String categories = fields[4];

        ScSolution solution = new ScSolution(score, author, displayLink, oldVideoRNG);
        if (categories != null)
            Pattern.compile(",").splitAsStream(categories).map(ScCategory::valueOf).forEach(solution.getCategories()::add);
        return solution;
    }

    @Override
    @NotNull
    public String[] marshal() {
        return new String[]{
                score.toDisplayString(),
                author,
                displayLink,
                oldVideoRNG ? "linux" : null,
                categories.stream()
                          .map(ScCategory::name)
                          .collect(Collectors.joining(","))
        };
    }
}