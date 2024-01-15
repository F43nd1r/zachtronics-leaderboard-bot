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

package com.faendir.zachtronics.bot.inf.repository;

import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.inf.model.*;
import com.faendir.zachtronics.bot.inf.validation.IfSave;
import com.faendir.zachtronics.bot.inf.validation.IfValidator;
import com.faendir.zachtronics.bot.repository.CategoryRecord;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@BotTest
@Disabled("Massive tests only for manual testing or migrations")
class IfManualTest {

    @Autowired
    private IfSolutionRepository repository;

    @Test
    public void testFullIO() throws IOException {
        /*
        cp -a ../infinifactory/leaderboard/* src/test/resources/repositories/if-leaderboard/
         */
        for (IfPuzzle p : IfPuzzle.values()) {

            Iterable<IfRecord> records = repository.findCategoryHolders(p, true).stream()
                                                   .map(CategoryRecord::getRecord)
                    ::iterator;
            for (IfRecord r : records) {
                if (r.getDataPath() != null && Files.exists(r.getDataPath())) {
                    IfSubmission submission = new IfSubmission(p, r.getScore(), r.getAuthor(), r.getDisplayLinks(),
                                                               Files.readString(r.getDataPath()));
                    repository.submit(submission);
                }
            }

            System.out.println("Done " + p.getDisplayName());
        }

        /*
        rm -r src/test/resources/repositories/if-leaderboard/
        git restore --source=HEAD --staged --worktree -- src/test/resources/repositories/if-leaderboard/
        rsync -a --delete --exclude=README.txt $(ls -1dt /tmp/if-leaderboard* | head -n1)/* ../infinifactory/leaderboard/
         */

        System.out.println("Done");
    }

    @Test
    public void rebuildAllWiki() {
        for (IfPuzzle puzzle : IfPuzzle.values()) {
            repository.rebuildRedditLeaderboard(puzzle, "");
            System.out.println("Done " + puzzle.getDisplayName());
        }

        String page = repository.getRedditService().getWikiPage(repository.getSubreddit(), repository.getWikiPageName())
                                .replaceAll("file:/tmp/if-leaderboard[0-9]+/",
                                            "https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master");
        System.out.println(page);
    }

    @Test
    public void createWiki() {
        StringBuilder page = new StringBuilder();
        for (IfGroup group : IfGroup.values()) {
            String header = String.format("""
                                          ### %s

                                          | Name | Cycles | Cycles (No GRAs) | Footprint | Blocks
                                          | ---  | ---  | --- | --- | ---
                                          """, group.getDisplayName());
            page.append(header);
            String groupTable = Arrays.stream(IfPuzzle.values())
                                      .filter(p -> p.getGroup() == group)
                                      .map(p -> String.format("| [%s](%s) | | | | \n", p.getDisplayName(), p.getLink()))
                                      .collect(Collectors.joining("|\n"));
            page.append(groupTable).append('\n');
        }
        System.out.println(page);
    }

    @Test
    public void tagNewCategories() throws IOException {
        Path repoPath = Paths.get("../infinifactory/leaderboard");

        for (IfPuzzle puzzle : IfPuzzle.values()) {
            Path puzzlePath = repoPath.resolve(repository.relativePuzzlePath(puzzle));
            List<IfSolution> solutions = repository.unmarshalSolutions(puzzlePath);
            if (solutions.isEmpty())
                continue;

            solutions.stream().map(IfSolution::getCategories).forEach(Set::clear);
            for (IfCategory category : puzzle.getSupportedCategories()) {
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

    @Test
    public void unGRATheUnGRAable() throws IOException {
        Path repoPath = Paths.get("../infinifactory/leaderboard");

        for (IfPuzzle puzzle : IfPuzzle.values()) {
            Path puzzlePath = repoPath.resolve(repository.relativePuzzlePath(puzzle));
            List<IfSolution> solutions = repository.unmarshalSolutions(puzzlePath);
            if (solutions.isEmpty())
                continue;

            for (ListIterator<IfSolution> it = solutions.listIterator(); it.hasNext(); ) {
                IfSolution solution = it.next();
                IfScore score = solution.getScore();
                Path archivePath = repository.makeArchivePath(puzzlePath, score);
                if (Files.exists(archivePath)) {
                    String solutionData = Files.readAllLines(archivePath).get(1).split(" = ")[1];
                    IfSave save = IfSave.unmarshal(solutionData);
                    if (!IfValidator.couldHaveGRA(save, puzzle) && score.usesGRA()) {
                        IfScore newScore = new IfScore(score.getCycles(), score.getFootprint(), score.getBlocks(),
                                                       score.isOutOfBounds(), false, score.isFinite());
                        it.set(new IfSolution(newScore, solution.getAuthor(), solution.getDisplayLinks()));
                        Files.move(archivePath, repository.makeArchivePath(puzzlePath, newScore));
                        System.out.println("UnGRAed " + puzzle + ", " + newScore);
                    }
                }
            }
            repository.marshalSolutions(solutions, puzzlePath);
        }
    }

    @Test
    public void reflagSomeone() throws IOException {
        Path repoPath = Paths.get("../infinifactory/leaderboard");
        String author = "NAME";

        for (IfPuzzle puzzle : IfPuzzle.values()) {
            Path puzzlePath = repoPath.resolve(repository.relativePuzzlePath(puzzle));
            List<IfSolution> solutions = repository.unmarshalSolutions(puzzlePath);
            if (solutions.isEmpty())
                continue;

            for (ListIterator<IfSolution> it = solutions.listIterator(); it.hasNext(); ) {
                IfSolution solution = it.next();
                if (!solution.getAuthor().equals(author))
                    continue;
                IfScore score = solution.getScore();
                Path archivePath = repository.makeArchivePath(puzzlePath, score);
                IfScore newScore = new IfScore(score.getCycles(), score.getFootprint(), score.getBlocks(),
                                               false, false,
                                               score.isFinite() /* edit condition as needed */);
                it.set(new IfSolution(newScore, author, solution.getDisplayLinks()));
                Files.move(archivePath, repository.makeArchivePath(puzzlePath, newScore));
                System.out.println("Cleaned " + puzzle + ", " + newScore);
            }
            repository.marshalSolutions(solutions, puzzlePath);
        }
    }
}