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

package com.faendir.zachtronics.bot.tis.repository;

import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.TestConfigurationKt;
import com.faendir.zachtronics.bot.config.GitProperties;
import com.faendir.zachtronics.bot.git.GitRepository;
import com.faendir.zachtronics.bot.repository.CategoryRecord;
import com.faendir.zachtronics.bot.tis.model.*;
import com.faendir.zachtronics.bot.tis.validation.TIS100CXX;
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
import java.util.ListIterator;
import java.util.Set;
import java.util.stream.Stream;

@BotTest
@Disabled("Massive tests only for manual testing or migrations")
class TISManualTest {

    @Autowired
    private TISSolutionRepository repository;

    @TestConfiguration
    static class RepositoryConfiguration {
        @Bean("tisRepository")
        public static @NotNull GitRepository tisRepository(GitProperties gitProperties) {
            return TestConfigurationKt.readOnlyLocalClone("../tis100/leaderboard", gitProperties);
        }
    }

    @Test
    public void testFullIO() throws IOException {
        for (TISPuzzle p : repository.getTrackedPuzzles()) {

            Iterable<TISRecord> records = repository.findCategoryHolders(p, true).stream()
                                                    .map(CategoryRecord::getRecord)
                    ::iterator;
            for (TISRecord r : records) {
                if (r.getDataPath() != null && Files.exists(r.getDataPath())) {
                    TISSubmission submission = new TISSubmission(p, r.getScore(), r.getAuthor(), r.getDisplayLink(),
                                                                 Files.readString(r.getDataPath()));
                    repository.submit(submission);
                }
            }

            System.out.println("Done " + p.getDisplayName());
        }

        /*
        rsync -a --delete --exclude=README.txt $(ls -1dt /tmp/leaderboard* | head -n1)/* ../tis100/leaderboard/
         */

        System.out.println("Done");
    }

    @Test
    public void rebuildAllWiki() {
        repository.rebuildRedditLeaderboard(null);
        String page = repository.getRedditService().getWikiPage(repository.getSubreddit(), repository.wikiPageName(null))
                                .replaceAll("file:[^()]+/leaderboard/",
                                            "https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master");
        System.out.println(page);
    }

    @Test
    public void tagNewCategories() throws IOException {
        Path repoPath = Paths.get("../tis100/leaderboard");

        for (TISPuzzle puzzle : repository.getTrackedPuzzles()) {
            Path puzzlePath = repoPath.resolve(repository.relativePuzzlePath(puzzle));
            List<TISSolution> solutions = repository.unmarshalSolutions(puzzlePath);
            if (solutions.isEmpty())
                continue;

            solutions.stream().map(TISSolution::getCategories).forEach(Set::clear);
            for (TISCategory category : puzzle.getSupportedCategories()) {
                solutions.stream()
                         .filter(s -> category.supportsScore(s.getScore()))
                         .min(Comparator.comparing(TISSolution::getScore, category.getScoreComparator()))
                         .ifPresent(s -> s.getCategories().add(category));
            }
            repository.marshalSolutions(solutions, puzzlePath);
        }
    }

    @Test
    public void reverifyLeaderboard() throws IOException {
        Path repoPath = Paths.get("../tis100/leaderboard");

        for (TISPuzzle puzzle : List.of(TISPuzzle.SIGNAL_PRESCALER)) {
            Path puzzlePath = repoPath.resolve(repository.relativePuzzlePath(puzzle));
            List<TISSolution> solutions = repository.unmarshalSolutions(puzzlePath);

            for (ListIterator<TISSolution> it = solutions.listIterator(); it.hasNext(); ) {
                TISSolution solution = it.next();
                Path dataPath = repository.makeArchivePath(puzzlePath, solution.getScore());
                if (Files.exists(dataPath)) {
                    String data = Files.readString(dataPath);
                    TISScore newScore = TIS100CXX.validate(data, puzzle);
                    if (!newScore.equals(solution.getScore())) {
                        it.set(new TISSolution(newScore, solution.getAuthor(), solution.getDisplayLink()));
                        Files.move(dataPath, repository.makeArchivePath(puzzlePath, newScore));
                        System.out.println("Changed " + puzzle.getId() + ": " +
                                           solution.getScore().toDisplayString() + " -> " + newScore.toDisplayString());
                    }
                }
            }
            repository.marshalSolutions(solutions, puzzlePath);
            System.out.println("Done " + puzzle.getId());
        }

        System.out.println("Done");
    }

    @Test
    public void submitSaveFolder() throws IOException {
        Path savesRoot = Paths.get("../tis100/saves");
//        List<String> authors = Files.list(savesRoot)
//                                    .filter(Files::isDirectory)
//                                    .map(p -> p.getFileName().toString())
//                                    .sorted(String.CASE_INSENSITIVE_ORDER)
//                                    .toList();
        List<String> authors = List.of("12345ieee");

        for (String author : authors) {
            System.out.println("Starting " + author);

            for (TISPuzzle puzzle : repository.getTrackedPuzzles()) {
                try (Stream<Path> walkStream = Files.walk(savesRoot.resolve(author), FileVisitOption.FOLLOW_LINKS)) {
                    Iterable<Path> paths = walkStream.filter(p -> p.getFileName().toString().matches(puzzle.getId() + ".*\\..*txt"))
                        ::iterator;
                    for (Path path : paths) {
                        String data = Files.readString(path).replace("\r\n", "\n");
                        TISSubmission submission;
                        try {
                            submission = TISSubmission.fromData(data, puzzle, author, null);
                        }
                        catch (Exception e) {
                            if (e instanceof ValidationException) {
                                String message = e.getMessage();
                                if (message.startsWith("ERROR: ")) {
                                    System.err.print(path + message.substring(7));
                                    continue;
                                }
                                if (message.contains("validation failed for fixed test"))
                                    continue;
                            }
                            System.err.println(path);
                            throw e;
                        }

                        System.out.println(repository.submit(submission));
                    }
                }
            }
        }

        /*
        rsync -a --delete --exclude=README.txt $(ls -1dt /tmp/leaderboard* | head -n1)/* ../tis100/leaderboard/
         */

        System.out.println("Done");
    }
}