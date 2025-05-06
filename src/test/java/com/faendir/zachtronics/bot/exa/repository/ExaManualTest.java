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

package com.faendir.zachtronics.bot.exa.repository;

import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.TestConfigurationKt;
import com.faendir.zachtronics.bot.config.GitProperties;
import com.faendir.zachtronics.bot.exa.model.*;
import com.faendir.zachtronics.bot.exa.validation.Exapt;
import com.faendir.zachtronics.bot.git.GitRepository;
import com.faendir.zachtronics.bot.reddit.RedditService;
import com.faendir.zachtronics.bot.reddit.Subreddit;
import com.faendir.zachtronics.bot.repository.CategoryRecord;
import com.faendir.zachtronics.bot.validation.ValidationException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@BotTest
@Disabled("Massive tests only for manual testing or migrations")
public class ExaManualTest {

    @Autowired
    private ExaSolutionRepository repository;
    @Autowired
    private RedditService redditService;

    @TestConfiguration
    static class RepositoryConfiguration {
        @Bean("exaRepository")
        public static @NotNull GitRepository exaRepository(GitProperties gitProperties) {
            return TestConfigurationKt.readOnlyLocalClone("../exapunks/leaderboard", gitProperties);
        }
    }

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
                                   .replaceAll("file:/tmp/leaderboard[0-9]+/",
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
        List<String> authors = List.of("someGuy");
//        List<String> authors = Files.list(Paths.get("../exapunks/saves/")).map(p -> p.getFileName().toString()).sorted().toList();

        for (String author : authors) {
            System.out.println("Starting: " + author);
            Path basePath = Paths.get("../exapunks/saves/" + author);

            try (Stream<Path> walkStream = Files.walk(basePath, FileVisitOption.FOLLOW_LINKS)) {
                Iterable<Path> paths = walkStream.filter(p -> p.getFileName().toString().endsWith(".solution"))
                    ::iterator;
                for (Path path : paths) {
                    byte[] data = Files.readAllBytes(path);
                    ExaSubmission submission;
                    try {
//                        submission = ExaSubmission.fromData(data, false, author, null);
                        submission = Exapt.validateData(data, false, author, null);
                    }
                    catch (Exception e) {
                        if (e instanceof ValidationException) {
                            String message = e.getMessage();
                            if (message.equals("Unsolved solution") || message.equals("Corrupted solution") ||
                                message.startsWith("Size larger than puzzle limit") ||
                                message.startsWith("Actual size different from declared") ||
                                message.startsWith("Hacker battles won") ||
                                message.startsWith("Sandbox indicator") || message.startsWith("Unknown puzzle") ||
                                message.startsWith("Exapt:") || message.startsWith("Error in reading back results"))
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
        rsync -a --delete --exclude=README.txt $(ls -1dt /tmp/leaderboard* | head -n1)/* ../exapunks/leaderboard/
         */

        System.out.println("Done");
    }

}
