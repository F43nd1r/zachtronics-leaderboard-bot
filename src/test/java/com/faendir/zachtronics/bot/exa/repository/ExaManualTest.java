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

package com.faendir.zachtronics.bot.exa.repository;

import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.exa.model.*;
import com.faendir.zachtronics.bot.reddit.RedditService;
import com.faendir.zachtronics.bot.reddit.Subreddit;
import com.faendir.zachtronics.bot.repository.CategoryRecord;
import com.faendir.zachtronics.bot.validation.ValidationException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@BotTest
@Disabled("Massive tests only for manual testing or migrations")
public class ExaManualTest {

    @Autowired
    private ExaSolutionRepository repository;
    @Autowired
    private RedditService redditService;

    @Test
    public void testFullIO() throws IOException {
        for (ExaPuzzle p : repository.getTrackedPuzzles()) {

            Iterable<ExaRecord> records = repository.findCategoryHolders(p, true).stream()
                                                    .map(CategoryRecord::getRecord)
                                                   ::iterator;
            for (ExaRecord r : records) {
                if (r.getDataPath() == null || !Files.exists(r.getDataPath()))
                    continue;
                ExaSubmission submission = new ExaSubmission(p, r.getScore(), r.getAuthor(), r.getDisplayLink(),
                                                             Files.readAllBytes(r.getDataPath()));
                repository.submit(submission);
            }

            System.out.println("Done " + p.getDisplayName());
        }
        System.out.println("Done");
    }

    @Test
    public void rebuildRedditWiki() {
        repository.rebuildRedditLeaderboard(null);
        String page = redditService.getWikiPage(Subreddit.EXAPUNKS, "index")
                                   .replaceAll("file:/tmp/exa-leaderboard[0-9]+/",
                                               "https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master");
        System.out.println(page);
    }

    @Test
    public void createWiki() {
        StringBuilder page = new StringBuilder();
        for (ExaGroup group : ExaGroup.values()) {
            String header = String.format("""
                                          ### %s

                                          | Level | Cycles | Size | Activity
                                          | ---  | ---  | --- | ---
                                          """, group.getDisplayName());
            page.append(header);
            String groupTable = repository.getTrackedPuzzles().stream()
                                          .filter(p -> p.getGroup() == group)
                                          .map(p -> String.format("| [%s](%s) | | | \n", p.getDisplayName(), p.getLink()))
                                          .collect(Collectors.joining("|\n"));
            page.append(groupTable).append('\n');
        }
        System.out.println(page);
    }

    @Test
    public void validateLeaderboard() throws IOException {
        Path repoPath = Paths.get("../exapunks/leaderboard");

        for (ExaPuzzle puzzle : repository.getTrackedPuzzles()) {
            Path puzzlePath = repoPath.resolve(repository.relativePuzzlePath(puzzle));
            List<ExaSolution> solutions = repository.unmarshalSolutions(puzzlePath);

            for (ExaSolution solution : solutions) {
                Path dataPath = repository.makeArchivePath(puzzlePath, solution.getScore());
                if (!Files.exists(dataPath))
                    continue;
                byte[] data = Files.readAllBytes(dataPath);
                ExaSubmission submission = ExaSubmission.fromData(data, solution.getScore().isCheesy(), solution.getAuthor(),
                                                                  solution.getDisplayLink());

                assertEquals(puzzle, submission.getPuzzle());
                assertEquals(solution.getScore(), submission.getScore());
            }
        }
    }

    @Test
    public void tagNewCategories() throws IOException {
        Path repoPath = Paths.get("../exapunks/leaderboard");

        for (ExaPuzzle puzzle : repository.getTrackedPuzzles()) {
            Path puzzlePath = repoPath.resolve(repository.relativePuzzlePath(puzzle));
            List<ExaSolution> solutions = repository.unmarshalSolutions(puzzlePath);
            if (solutions.isEmpty())
                continue;

            solutions.stream().map(ExaSolution::getCategories).forEach(Set::clear);
            for (ExaCategory category : puzzle.getSupportedCategories()) {
                solutions.stream()
                         .filter(s -> category.supportsScore(s.getScore()))
                         .min(Comparator.comparing(ExaSolution::getScore, category.getScoreComparator()))
                         .orElseThrow()
                         .getCategories()
                         .add(category);
            }
            repository.marshalSolutions(solutions, puzzlePath);
        }
    }

    @Test
    public void submitSaveFolder() throws IOException {
        String author = "someGuy";

        Path savesPath = Paths.get("../exapunks/saves/" + author);
        /*
        cp -a ../exapunks/leaderboard/* src/test/resources/repositories/exa-leaderboard/
         */

        Iterable<String> prefixes = Arrays.stream(ExaPuzzle.values())
                                          .map(ExaPuzzle::getPrefix)
                                          .collect(Collectors.toCollection(LinkedHashSet::new));
        for (String prefix : prefixes) {
            try (DirectoryStream<Path> paths = Files.newDirectoryStream(savesPath, prefix + "*.solution")) {
                for (Path path : paths) {
                    byte[] data = Files.readAllBytes(path);
                    ExaSubmission submission;
                    try {
                        submission = ExaSubmission.fromData(data, false, author, "https://li.nk");
                    }
                    catch (Exception e) {
                        if (e instanceof ValidationException) {
                            String message = e.getMessage();
                            if (message.equals("Unsolved solution") || message.equals("Corrupted solution") ||
                                message.startsWith("Size larger than puzzle limit") ||
                                message.startsWith("Actual size different from declared") ||
                                message.startsWith("Hacker battles won") ||
                                message.startsWith("Sandbox indicator") || message.startsWith("Unknown puzzle"))
                                continue;
                        }
                        System.err.println(path);
                        throw e;
                    }

                    System.out.println(repository.submit(submission));
                }
            }
        }

        /*
        rm -r src/test/resources/repositories/exa-leaderboard/*
        git restore --source=HEAD --staged --worktree -- src/test/resources/repositories/exa-leaderboard/
        rsync -a --delete --exclude=README.txt $(ls -1dt /tmp/exa-leaderboard* | head -n1)/* ../exapunks/leaderboard/
         */

        System.out.println("Done");
    }

}
