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

import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.inf.model.IfCategory;
import com.faendir.zachtronics.bot.inf.model.IfPuzzle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

@BotTest
//@Disabled("Massive tests only for manual testing or migrations")
class IfManualTest {

    @Autowired
    private IfSolutionRepository repository;

    @Test
    public void tagNewCategories() throws IOException {
        Path repoPath = Paths.get("../infinifactory/leaderboard");
        List<IfCategory> categories = List.of(IfCategory.values());
        List<IfPuzzle> puzzles = List.of(IfPuzzle.values());

        for (IfPuzzle puzzle : puzzles) {
            Path puzzlePath = repoPath.resolve(repository.relativePuzzlePath(puzzle));
            List<IfSolution> solutions = repository.unmarshalSolutions(puzzlePath);
            if (solutions.isEmpty())
                continue;
            for (IfCategory category : categories) {
                if (!puzzle.getSupportedCategories().contains(category))
                    continue;
                solutions.stream()
                         .filter(s -> category.supportsScore(s.getScore()))
                         .min(Comparator.comparing(IfSolution::getScore, category.getScoreComparator()))
                         .orElseThrow()
                         .getCategories()
                         .add(category);
            }
            repository.marshalSolutions(solutions, puzzlePath);
        }
    }
}