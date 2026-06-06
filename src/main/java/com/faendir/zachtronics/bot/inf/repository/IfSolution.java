/*
 * Copyright (c) 2026
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
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Value
public class IfSolution implements Solution<IfCategory, IfPuzzle, IfScore, IfRecord> {
    IfScore score;
    String author;
    List<String> displayLinks;
    /** empty if it holds no categories */
    EnumSet<IfCategory> categories = EnumSet.noneOf(IfCategory.class);

    @Override
    public @Nullable String getDisplayLink() {
        return displayLinks.isEmpty() ? null : displayLinks.get(0);
    }

    @Override
    public IfRecord extendToRecord(IfPuzzle puzzle, @Nullable String dataLink, @Nullable Path dataPath) {
        if (dataPath != null)
            return new IfRecord(puzzle, score, author, displayLinks, dataLink, dataPath);
        else
            return new IfRecord(puzzle, score, author, displayLinks, null, null);
    }

    public static IfSolution unmarshal(@Nullable String[] fields) {
        assert fields.length == 4;
        IfScore score = requireNonNull(IfScore.parseScore(requireNonNull(fields[0])));
        String author = requireNonNull(fields[1]);
        String displayLinksStr = fields[2];
        String categories = fields[3];

        List<String> displayLinks = displayLinksStr == null ? Collections.emptyList()
                                                            : List.of(displayLinksStr.split(","));
        IfSolution solution = new IfSolution(score, author, displayLinks);
        if (categories != null)
            Pattern.compile(",").splitAsStream(categories).map(IfCategory::valueOf).forEach(solution.categories::add);
        return solution;
    }

    @Override
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
