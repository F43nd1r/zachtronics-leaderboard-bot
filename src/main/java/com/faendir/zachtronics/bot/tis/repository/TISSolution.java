/*
 * Copyright (c) 2024
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

package com.faendir.zachtronics.bot.tis.repository;

import com.faendir.zachtronics.bot.repository.Solution;
import com.faendir.zachtronics.bot.tis.model.TISCategory;
import com.faendir.zachtronics.bot.tis.model.TISPuzzle;
import com.faendir.zachtronics.bot.tis.model.TISRecord;
import com.faendir.zachtronics.bot.tis.model.TISScore;
import lombok.Value;
import lombok.With;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Value
public class TISSolution implements Solution<TISCategory, TISPuzzle, TISScore, TISRecord> {
    @NotNull TISScore score;
    @NotNull String author;
    @With String displayLink;
    /** empty if it holds no categories */
    EnumSet<TISCategory> categories = EnumSet.noneOf(TISCategory.class);

    @Override
    public TISRecord extendToRecord(TISPuzzle puzzle, String dataLink, Path dataPath) {
        if (dataPath != null && Files.exists(dataPath))
            return new TISRecord(puzzle, score, author, displayLink, dataLink, dataPath);
        else
            return new TISRecord(puzzle, score, author, displayLink, null, null);
    }

    @NotNull
    public static TISSolution unmarshal(String @NotNull [] fields) {
        assert fields.length == 4;
        TISScore score = Objects.requireNonNull(TISScore.parseScore(fields[0]));
        String author = fields[1];
        String displayLink = fields[2];
        String categories = fields[3];

        TISSolution solution = new TISSolution(score, author, displayLink);
        if (categories != null)
            Pattern.compile(",").splitAsStream(categories).map(TISCategory::valueOf).forEach(solution.categories::add);
        return solution;
    }

    @Override
    public String @NotNull [] marshal() {
        return new String[]{
                score.toDisplayString(),
                author,
                displayLink,
                categories.stream()
                          .map(TISCategory::name)
                          .collect(Collectors.joining(","))
        };
    }
}