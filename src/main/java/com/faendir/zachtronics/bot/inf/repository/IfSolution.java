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

package com.faendir.zachtronics.bot.inf.repository;

import com.faendir.zachtronics.bot.inf.model.IfCategory;
import com.faendir.zachtronics.bot.inf.model.IfPuzzle;
import com.faendir.zachtronics.bot.inf.model.IfRecord;
import com.faendir.zachtronics.bot.inf.model.IfScore;
import com.faendir.zachtronics.bot.repository.Solution;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Value
public class IfSolution implements Solution<IfCategory, IfPuzzle, IfScore, IfRecord> {
    @NotNull IfScore score;
    @NotNull String author;
    @NotNull List<String> displayLinks;
    /** empty if it holds no categories */
    EnumSet<IfCategory> categories = EnumSet.noneOf(IfCategory.class);

    @Override
    public IfRecord extendToRecord(IfPuzzle puzzle, String dataLink, Path dataPath) {
        if (dataPath != null)
            return new IfRecord(puzzle, score, author, displayLinks, dataLink, dataPath);
        else
            return new IfRecord(puzzle, score, author, displayLinks, null, null);
    }

    @NotNull
    public static IfSolution unmarshal(@NotNull String[] fields) {
        assert fields.length == 4;
        IfScore score = Objects.requireNonNull(IfScore.parseScore(fields[0]));
        String author = fields[1];
        String displayLinks = fields[2];
        String categories = fields[3];

        IfSolution solution = new IfSolution(score, author, List.of(displayLinks.split(",")));
        if (categories != null)
            Pattern.compile(",").splitAsStream(categories).map(IfCategory::valueOf).forEach(solution.categories::add);
        return solution;
    }

    @Override
    @NotNull
    public String[] marshal() {
        return new String[]{
                score.toDisplayString(),
                author,
                String.join(",", displayLinks),
                categories.stream()
                          .map(IfCategory::name)
                          .collect(Collectors.joining(","))
        };
    }
}