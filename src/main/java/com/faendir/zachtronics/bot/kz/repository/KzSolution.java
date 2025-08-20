/*
 * Copyright (c) 2025
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

package com.faendir.zachtronics.bot.kz.repository;

import com.faendir.zachtronics.bot.kz.model.KzCategory;
import com.faendir.zachtronics.bot.kz.model.KzPuzzle;
import com.faendir.zachtronics.bot.kz.model.KzRecord;
import com.faendir.zachtronics.bot.kz.model.KzScore;
import com.faendir.zachtronics.bot.repository.Solution;
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
public class KzSolution implements Solution<KzCategory, KzPuzzle, KzScore, KzRecord> {
    @NotNull KzScore score;
    @NotNull String author;
    @With String displayLink;
    /** empty if it holds no categories */
    EnumSet<KzCategory> categories = EnumSet.noneOf(KzCategory.class);

    @Override
    public KzRecord extendToRecord(KzPuzzle puzzle, String dataLink, Path dataPath) {
        if (dataPath != null && Files.exists(dataPath))
            return new KzRecord(puzzle, score, author, displayLink, dataLink, dataPath);
        else
            return new KzRecord(puzzle, score, author, displayLink, null, null);
    }

    @NotNull
    public static KzSolution unmarshal(String @NotNull [] fields) {
        assert fields.length == 4;
        KzScore score = Objects.requireNonNull(KzScore.parseScore(fields[0]));
        String author = fields[1];
        String displayLink = fields[2];
        String categories = fields[3];

        KzSolution solution = new KzSolution(score, author, displayLink);
        if (categories != null)
            Pattern.compile(",").splitAsStream(categories).map(KzCategory::valueOf).forEach(solution.categories::add);
        return solution;
    }

    @Override
    public String @NotNull [] marshal() {
        return new String[]{
                score.toDisplayString(),
                author,
                displayLink,
                categories.stream()
                          .map(KzCategory::name)
                          .collect(Collectors.joining(","))
        };
    }
}