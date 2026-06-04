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

package com.faendir.zachtronics.bot.cw.repository;

import com.faendir.zachtronics.bot.cw.model.CwCategory;
import com.faendir.zachtronics.bot.cw.model.CwPuzzle;
import com.faendir.zachtronics.bot.cw.model.CwRecord;
import com.faendir.zachtronics.bot.cw.model.CwScore;
import com.faendir.zachtronics.bot.repository.Solution;
import lombok.Value;
import lombok.With;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.EnumSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Value
public class CwSolution implements Solution<CwCategory, CwPuzzle, CwScore, CwRecord> {
    CwScore score;
    String author;
    @With @Nullable String displayLink;
    /** empty if it holds no categories */
    EnumSet<CwCategory> categories = EnumSet.noneOf(CwCategory.class);

    @Override
    public CwRecord extendToRecord(CwPuzzle puzzle, @Nullable String dataLink, @Nullable Path dataPath) {
        if (dataPath != null)
            return new CwRecord(puzzle, score, author, displayLink, dataLink, dataPath);
        else
            return new CwRecord(puzzle, score, author, displayLink, null, null);
    }

    public static CwSolution unmarshal(@Nullable String[] fields) {
        assert fields.length == 4;
        CwScore score = requireNonNull(CwScore.parseScore(requireNonNull(fields[0])));
        String author = requireNonNull(fields[1]);
        String displayLink = fields[2];
        String categories = fields[3];

        CwSolution solution = new CwSolution(score, author, displayLink);
        if (categories != null)
            Pattern.compile(",").splitAsStream(categories).map(CwCategory::valueOf).forEach(solution.categories::add);
        return solution;
    }

    @Override
    public @Nullable String[] marshal() {
        return new @Nullable String[]{
                score.toDisplayString(),
                author,
                displayLink,
                categories.stream()
                          .map(CwCategory::name)
                          .collect(Collectors.joining(","))
        };
    }
}
