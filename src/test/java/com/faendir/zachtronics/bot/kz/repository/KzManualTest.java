/*
 * Copyright (c) 2026
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

import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.TestConfigurationKt;
import com.faendir.zachtronics.bot.config.GitProperties;
import com.faendir.zachtronics.bot.git.GitRepository;
import com.faendir.zachtronics.bot.kz.model.*;
import com.faendir.zachtronics.bot.reddit.RedditService;
import com.faendir.zachtronics.bot.reddit.Subreddit;
import com.faendir.zachtronics.bot.repository.CategoryRecord;
import com.faendir.zachtronics.bot.utils.UtilsKt;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@BotTest
@Disabled("Massive tests only for manual testing or migrations")
public class KzManualTest {

    @Autowired
    private KzSolutionRepository repository;
    @Autowired
    private RedditService redditService;

    @TestConfiguration
    static class RepositoryConfiguration {
        @Bean("kzRepository")
        public static @NotNull GitRepository kzRepository(GitProperties gitProperties) {
            return TestConfigurationKt.readOnlyLocalClone("../kaizen/leaderboard", gitProperties);
        }
    }

    @Test
    public void testFullIO() throws IOException {
        for (KzPuzzle p : repository.getTrackedPuzzles()) {

            Iterable<KzRecord> records = repository.findCategoryHolders(p, true).stream()
                                                   .map(CategoryRecord::getRecord)
                ::iterator;
            for (KzRecord r : records) {
                KzSubmission submission = new KzSubmission(p, r.getScore(), r.getAuthor(), r.getDisplayLink(),
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
        String page = redditService.getWikiPage(Subreddit.KAIZEN, "index")
                                   .replaceAll("file:[^()]+/leaderboard/",
                                               "https://raw.githubusercontent.com/12345ieee/kaizen-leaderboard/master");
        System.out.println(page);
    }

    @Test
    public void createWiki() {
        StringBuilder page = new StringBuilder();
        for (KzGroup group : KzGroup.values()) {
            String header = String.format("""
                                          ### %s
                                          
                                          | Level | Time | Cost | Area
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
    public void initLeaderboard() throws IOException {
        Path repoPath = Paths.get("../kaizen/leaderboard");
        for (KzPuzzle puzzle : repository.getTrackedPuzzles()) {
            Path puzzlePath = repoPath.resolve(repository.relativePuzzlePath(puzzle));
            if (!Files.exists(puzzlePath)) {
                Files.createDirectories(puzzlePath);
            }
            Path indexPath = puzzlePath.resolve("solutions.psv");
            if (!Files.exists(indexPath)) {
                Files.createFile(indexPath);
            }
            Path readmePath = puzzlePath.resolve("README.txt");
            if (!Files.exists(readmePath)) {
                Files.createSymbolicLink(readmePath, Path.of("solutions.psv"));
            }
        }
    }

    @Test
    public void validateLeaderboard() throws IOException {
        Path repoPath = Paths.get("../kaizen/leaderboard");

        for (KzPuzzle puzzle : repository.getTrackedPuzzles()) {
            Path puzzlePath = repoPath.resolve(repository.relativePuzzlePath(puzzle));
            List<KzSolution> solutions = repository.unmarshalSolutions(puzzlePath);

            for (KzSolution solution : solutions) {
                Path dataPath = repository.makeArchivePath(puzzlePath, solution.getScore());
                if (!Files.exists(dataPath)) {
                    System.err.println("Missing data for " + repoPath.relativize(dataPath));
                    continue;
                }
                byte[] data = Files.readAllBytes(dataPath);
                KzSubmission submission;
                try {
                    submission = KzSubmission.fromData(data, solution.getAuthor(), solution.getDisplayLink());
                } catch (ValidationException e) {
                    System.err.println(repoPath.relativize(dataPath) + ": " + e.getMessage());
                    continue;
                }

                assertEquals(puzzle, submission.getPuzzle());
                assertEquals(solution.getScore(), submission.getScore());
            }
        }
    }

    @Test
    public void tagNewCategories() throws IOException {
        Path repoPath = Paths.get("../kaizen/leaderboard");

        for (KzPuzzle puzzle : repository.getTrackedPuzzles()) {
            Path puzzlePath = repoPath.resolve(repository.relativePuzzlePath(puzzle));
            List<KzSolution> solutions = repository.unmarshalSolutions(puzzlePath);
            if (solutions.isEmpty())
                continue;

            solutions.stream().map(KzSolution::getCategories).forEach(Set::clear);
            for (KzCategory category : puzzle.getSupportedCategories()) {
                solutions.stream()
                         .filter(s -> category.supportsScore(s.getScore()))
                         .min(Comparator.comparing(KzSolution::getScore, category.getScoreComparator()))
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
//        List<String> authors = Files.list(Paths.get("../kaizen/saves/")).map(p -> p.getFileName().toString()).sorted().toList();

        for (String author : authors) {
            System.out.println("Starting: " + author);
            Path basePath = Paths.get("../kaizen/saves/" + author);

            try (Stream<Path> walkStream = Files.walk(basePath, FileVisitOption.FOLLOW_LINKS)) {
                Iterable<Path> paths = walkStream.filter(p -> p.getFileName().toString().endsWith(".solution"))
                    ::iterator;
                for (Path path : paths) {
                    byte[] data = Files.readAllBytes(path);
                    KzSubmission submission;
                    try {
                        submission = KzSubmission.fromData(data, author, null);
                    }
                    catch (Exception e) {
                        if (e instanceof ValidationException) {
                            String message = e.getMessage();
                            if (message.equals("Puzzle was not solved.") || message.startsWith("A product was ") ||
                                message.equals("Tools cannot run two commands at once.") ||
                                message.equals("A tool was pushed into another tool.") ||
                                message.equals("Only tools on tracks can slide.") ||
                                message.startsWith("Solution was manipulated"))
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
        rsync -a --delete --exclude=README.txt $(ls -1dt /tmp/leaderboard* | head -n1)/* ../kaizen/leaderboard/
         */

        System.out.println("Done");
    }

    @Test
    public void importFromObsoleteLb() throws IOException {
        // Portable Radio     | [(1/10/18) Community](https://imgur.com/a/xI5sG3k)
        Pattern pattern = Pattern.compile("\\[?\\(" + KzScore.REGEX_SCORE + "\\) (?<author>[^]]+)(?:\\]\\((?<displayLink>[^)]+)\\))?");
        Path obsoletePath = Paths.get("../kaizen/Obsolete_Kaizen_Leaderboard.md");
        List<String> lines = Files.readAllLines(obsoletePath);
        for (String line : lines) {
            String[] fields = line.split(" *\\| *");
            KzPuzzle puzzle = findPuzzle(fields[0]);
            for (int i = 1; i < fields.length; i++) {
                Matcher m = pattern.matcher(fields[i]);
                if (!m.matches()) {
                    System.err.println("Failed to parse: " + fields[i]);
                    continue;
                }
                String author = m.group("author");
                String displayLink = m.group("displayLink");
                KzScore score = KzScore.parseScore(m);
                KzSubmission submission = new KzSubmission(puzzle, score, author + "@" + displayLink, null, new byte[0]);
                repository.submit(submission);
            }
        }
        // find . -size 0 -print -delete
    }

    private static KzPuzzle findPuzzle(@NotNull String levelName) {
        List<KzPuzzle> candidates = UtilsKt.fuzzyMatch(Arrays.asList(KzPuzzle.values()), levelName, KzPuzzle::getDisplayName);
        if (candidates.size() == 1)
            return candidates.get(0);
        else
            throw new IllegalStateException("\"" + levelName + "\" bad");
    }

}
