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

package com.faendir.zachtronics.bot.sc.repository;

import com.faendir.zachtronics.bot.repository.Solution;
import com.faendir.zachtronics.bot.sc.model.ScCategory;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.ScRecord;
import com.faendir.zachtronics.bot.sc.model.ScScore;
import lombok.Value;
import lombok.With;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Value
public class ScSolution implements Solution<ScCategory, ScPuzzle, ScScore, ScRecord> {
    @NotNull ScScore score;
    @NotNull String author;
    @With String displayLink;
    @With boolean videoOnly;
    /** empty if it holds no categories */
    EnumSet<ScCategory> categories = EnumSet.noneOf(ScCategory.class);

    @Override
    public ScRecord extendToRecord(ScPuzzle puzzle, String dataLink, Path dataPath) {
        if (videoOnly || dataPath == null)
            return new ScRecord(puzzle, score, author, displayLink, null, null);
        else
            return new ScRecord(puzzle, score, author, displayLink, dataLink, dataPath);
    }

    @NotNull
    public static ScSolution unmarshal(String @NotNull [] fields) {
        assert fields.length == 5;
        ScScore score = Objects.requireNonNull(ScScore.parseScore(fields[0]));
        String author = fields[1];
        String displayLink = fields[2];
        boolean videoOnly = fields[3] != null;
        String categories = fields[4];

        ScSolution solution = new ScSolution(score, author, displayLink, videoOnly);
        if (categories != null)
            Pattern.compile(",").splitAsStream(categories).map(ScCategory::valueOf).forEach(solution.categories::add);
        return solution;
    }

    @Override
    public String @NotNull [] marshal() {
        return new String[]{
                score.toDisplayString(),
                author,
                displayLink,
                videoOnly ? "video" : null,
                categories.stream()
                          .map(ScCategory::name)
                          .collect(Collectors.joining(","))
        };
    }
}