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

package com.faendir.zachtronics.bot.fc.repository;

import com.faendir.zachtronics.bot.fc.model.FcCategory;
import com.faendir.zachtronics.bot.fc.model.FcPuzzle;
import com.faendir.zachtronics.bot.fc.model.FcRecord;
import com.faendir.zachtronics.bot.fc.model.FcScore;
import com.faendir.zachtronics.bot.repository.Solution;
import lombok.Value;
import lombok.With;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Value
public class FcSolution implements Solution<FcCategory, FcPuzzle, FcScore, FcRecord> {
    @NotNull FcScore score;
    @NotNull String author;
    @With String displayLink;
    /** empty if it holds no categories */
    EnumSet<FcCategory> categories = EnumSet.noneOf(FcCategory.class);

    @Override
    public FcRecord extendToRecord(FcPuzzle puzzle, String dataLink, Path dataPath) {
        if (dataPath != null)
            return new FcRecord(puzzle, score, author, displayLink, dataLink, dataPath);
        else
            return new FcRecord(puzzle, score, author, displayLink, null, null);
    }

    @NotNull
    public static FcSolution unmarshal(@NotNull String[] fields) {
        assert fields.length == 4;
        FcScore score = Objects.requireNonNull(FcScore.parseScore(fields[0]));
        String author = fields[1];
        String displayLink = fields[2];
        String categories = fields[3];

        FcSolution solution = new FcSolution(score, author, displayLink);
        if (categories != null)
            Pattern.compile(",").splitAsStream(categories).map(FcCategory::valueOf).forEach(solution.categories::add);
        return solution;
    }

    @Override
    @NotNull
    public String[] marshal() {
        return new String[]{
                score.toDisplayString(),
                author,
                displayLink,
                categories.stream()
                          .map(FcCategory::name)
                          .collect(Collectors.joining(","))
        };
    }
}