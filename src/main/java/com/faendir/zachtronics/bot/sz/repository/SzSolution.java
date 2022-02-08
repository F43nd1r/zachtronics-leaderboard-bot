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

package com.faendir.zachtronics.bot.sz.repository;

import com.faendir.zachtronics.bot.repository.CategoryRecord;
import com.faendir.zachtronics.bot.repository.Solution;
import com.faendir.zachtronics.bot.sz.model.SzCategory;
import com.faendir.zachtronics.bot.sz.model.SzPuzzle;
import com.faendir.zachtronics.bot.sz.model.SzRecord;
import com.faendir.zachtronics.bot.sz.model.SzScore;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Value
public class SzSolution implements Solution<SzCategory, SzPuzzle, SzScore, SzRecord> {
    @NotNull SzScore score;
    @NotNull String author;
    /** empty if it holds no categories */
    EnumSet<SzCategory> categories = EnumSet.noneOf(SzCategory.class);

    public SzRecord extendToRecord(SzPuzzle puzzle, String dataLink, Path dataPath) {
        if (dataPath != null)
            return new SzRecord(puzzle, score, author, dataLink, dataPath);
        else
            return new SzRecord(puzzle, score, author, null, null);
    }

    @Override
    public CategoryRecord<SzRecord, SzCategory> extendToCategoryRecord(SzPuzzle puzzle, String dataLink, Path dataPath) {
        return new CategoryRecord<>(extendToRecord(puzzle, dataLink, dataPath), categories);
    }

    @NotNull
    public static SzSolution unmarshal(@NotNull String[] fields) {
        assert fields.length == 3;
        SzScore score = Objects.requireNonNull(SzScore.parseScore(fields[0]));
        String author = fields[1];
        String categories = fields[2];

        SzSolution solution = new SzSolution(score, author);
        if (categories != null)
            Pattern.compile(",").splitAsStream(categories).map(SzCategory::valueOf).forEach(solution.categories::add);
        return solution;
    }

    @Override
    @NotNull
    public String[] marshal() {
        return new String[]{
                score.toDisplayString(),
                author,
                categories.stream()
                          .map(SzCategory::name)
                          .collect(Collectors.joining(","))
        };
    }
}