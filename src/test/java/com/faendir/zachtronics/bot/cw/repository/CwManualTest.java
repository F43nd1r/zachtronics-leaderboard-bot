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

package com.faendir.zachtronics.bot.cw.repository;

import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.cw.model.*;
import com.faendir.zachtronics.bot.repository.CategoryRecord;
import com.faendir.zachtronics.bot.utils.LambdaUtils;
import com.faendir.zachtronics.bot.validation.ValidationResult;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@BotTest
@Disabled("Massive tests only for manual testing or migrations")
class CwManualTest {

    @Autowired
    private CwSolutionRepository repository;

    @Test
    public void testFullIO() {
        for (CwPuzzle p : repository.getTrackedPuzzles()) {
            List<ValidationResult<CwSubmission>> submissions =
                    repository.findCategoryHolders(p, true)
                              .stream()
                              .map(CategoryRecord::getRecord)
                              .map(CwManualTest::recordToSubmissions)
                              .<ValidationResult<CwSubmission>>map(ValidationResult.Valid::new)
                              .toList();

            repository.submitAll(submissions);

            System.out.println("Done " + p.getDisplayName());
        }
        System.out.println("Done");
    }

    @NotNull
    private static CwSubmission recordToSubmissions(@NotNull CwRecord record) {
        assert record.getDataPath() != null;
        String data = LambdaUtils.<Path, String>uncheckIOException(Files::readString).apply(record.getDataPath());
        return new CwSubmission(record.getPuzzle(), record.getScore(), record.getAuthor(),
                                record.getDisplayLink(), data);
    }

    @Test
    public void rebuildAllWiki() {
        repository.rebuildRedditLeaderboard(null);
        String page = repository.getRedditService().getWikiPage(repository.getSubreddit(), repository.wikiPageName(null))
                                .replaceAll("file:/tmp/cw-leaderboard[0-9]+/",
                                            "https://raw.githubusercontent.com/lastcallbbs-community-developers/forbidden-path-leaderboard/master");
        System.out.println(page);
    }

    @Test
    public void createWiki() {
        StringBuilder page = new StringBuilder();
        for (CwGroup group: CwGroup.values()) {
            String header = String.format("""
                                          ### %s

                                          | Name | Size | Footprint
                                          | ---  | ---  | ---
                                          """, group.getDisplayName());
            page.append(header);
            String groupTable = repository.getTrackedPuzzles().stream()
                                          .filter(p -> p.getGroup() == group)
                                          .map(p -> String.format("| [%s](%s) | | \n", p.getDisplayName(), p.getLink()))
                                          .collect(Collectors.joining("|\n"));
            page.append(groupTable).append('\n');
        }
        System.out.println(page);
    }

    @Test
    public void tagNewCategories() throws IOException {
        Path repoPath = Paths.get("../bbs/forbidden-path-leaderboard");

        for (CwPuzzle puzzle : repository.getTrackedPuzzles()) {
            Path puzzlePath = repoPath.resolve(repository.relativePuzzlePath(puzzle));
            List<CwSolution> solutions = repository.unmarshalSolutions(puzzlePath);
            if (solutions.isEmpty())
                continue;

            solutions.stream().map(CwSolution::getCategories).forEach(Set::clear);
            for (CwCategory category : puzzle.getSupportedCategories()) {
                solutions.stream()
                         .filter(s -> category.supportsScore(s.getScore()))
                         .min(Comparator.comparing(CwSolution::getScore, category.getScoreComparator()))
                         .orElseThrow()
                         .getCategories()
                         .add(category);
            }
            repository.marshalSolutions(solutions, puzzlePath);
        }
    }
}