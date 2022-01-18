/*
 * Copyright (c) 2021
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

package com.faendir.zachtronics.bot.sc.repository;

import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.reddit.RedditService;
import com.faendir.zachtronics.bot.reddit.Subreddit;
import com.faendir.zachtronics.bot.repository.CategoryRecord;
import com.faendir.zachtronics.bot.sc.model.*;
import com.faendir.zachtronics.bot.validation.ValidationResult;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.faendir.zachtronics.bot.sc.model.ScCategory.*;
import static java.util.function.Predicate.not;

@BotTest
@Disabled("Massive tests only for manual testing or migrations")
class SolRepoManualTest {

    @Autowired
    private ScSolutionRepository repository;
    @Autowired
    private RedditService redditService;

    @Test
    public void testFullIO() {
        for (ScPuzzle p : ScPuzzle.values()) {
            List<ValidationResult<ScSubmission>> submissions =
                    repository.findCategoryHolders(p, true)
                              .stream()
                              .map(CategoryRecord::getRecord)
                              .filter(not(ScRecord::isOldVideoRNG)) // would be added with no asterisk
                              .mapMulti(SolRepoManualTest::addRecordToSubmissions)
                              .<ValidationResult<ScSubmission>>map(ValidationResult.Valid::new)
                              .toList();

            repository.submitAll(submissions);

            System.out.println("Done " + p.getDisplayName());
        }
        System.out.println("Done");
    }

    private static void addRecordToSubmissions(@NotNull ScRecord record, Consumer<ScSubmission> cons) {
        if (record.getDataPath() != null) {
            try {
                String data = Files.readString(record.getDataPath());
                ScSubmission s = new ScSubmission(record.getPuzzle(), record.getScore(), record.getAuthor(),
                                                  record.getDisplayLink(), data);
                cons.accept(s);
            }
            catch (IOException ignored) {
            }
        }
    }

    @Test
    public void rebuildAllWiki() {
        for (ScPuzzle puzzle: ScPuzzle.values()) {
            repository.rebuildRedditLeaderboard(puzzle, "");
            System.out.println("Done " + puzzle.getDisplayName());
        }

        String pages = Arrays.stream(ScGroup.values())
                             .map(ScGroup::getWikiPage).distinct()
                             .map(p -> redditService.getWikiPage(Subreddit.SPACECHEM, p))
                             .map(s -> s.replaceAll("file:/tmp/sc-archive[0-9]+/",
                                                    "https://raw.githubusercontent.com/spacechem-community-developers/spacechem-archive/master"))
                             .collect(Collectors.joining("\n\n---\n"));
        System.out.println(pages);
    }

    @Test
    public void bootstrapPsv() throws IOException {
        Path repoPath = Paths.get("../spacechem/archive");

        for (ScPuzzle puzzle : ScPuzzle.values()) {
            Path indexPath = repoPath.resolve(puzzle.getGroup().name()).resolve(puzzle.name()).resolve("solutions.psv");
            try (ICSVWriter writer = new CSVWriterBuilder(Files.newBufferedWriter(indexPath)).withSeparator('|').build()) {

                for (CategoryRecord<ScRecord, ScCategory> cr : repository.findCategoryHolders(puzzle, true)) {
                    ScRecord record = cr.getRecord();
                    String author = record.getAuthor();
                    String categories = cr.getCategories().stream()
                                          .map(ScCategory::name)
                                          .collect(Collectors.joining(","));

                    String[] csvRecord = new String[]{record.getScore().toDisplayString(),
                                                      author,
                                                      record.getDisplayLink(),
                                                      record.isOldVideoRNG() ? "linux" : null,
                                                      categories};
                    writer.writeNext(csvRecord, false);
                }
            }
        }
    }

    @Test
    public void tagNewCategories() throws IOException {
        Path repoPath = Paths.get("../spacechem/archive");
        List<ScCategory> newCategories = List.of(CNBP, SNBP, RCNBP, RSNBP);

        for (ScPuzzle puzzle : ScPuzzle.values()) {
            Path puzzlePath = repoPath.resolve(puzzle.getGroup().name()).resolve(puzzle.name());
            List<ScSolution> solutions = ScSolutionRepository.unmarshalSolutions(puzzlePath);
            if (solutions.isEmpty())
                continue;
            for (ScCategory category : newCategories) {
                if (!puzzle.getSupportedCategories().contains(category))
                    continue;
                solutions.stream()
                         .filter(s -> category.supportsScore(s.getScore()))
                         .min(Comparator.comparing(ScSolution::getScore, category.getScoreComparator()))
                         .orElseThrow()
                         .getCategories()
                         .add(category);
            }
            ScSolutionRepository.marshalSolutions(solutions, puzzlePath);
        }
    }

    @Test
    public void pushStateAuthorsToSolutionFiles() throws IOException {
        Path repoPath = Paths.get("../spacechem/archive");

        for (ScPuzzle puzzle : ScPuzzle.values()) {
            Path puzzlePath = repoPath.resolve(puzzle.getGroup().name()).resolve(puzzle.name());
            List<ScSolution> solutions = ScSolutionRepository.unmarshalSolutions(puzzlePath);
            for (ScSolution solution : solutions) {
                Path path = puzzlePath.resolve(ScSolutionRepository.makeScoreFilename(solution.getScore()));
                if (Files.exists(path)) {
                    String export = Files.readString(path);
                    ScSolutionMetadata metadata = ScSolutionMetadata.fromHeader(export, puzzle);
                    if (!solution.getAuthor().equalsIgnoreCase(metadata.getAuthor())) {
                        ScSolutionMetadata newMetadata = new ScSolutionMetadata(puzzle, solution.getAuthor(), solution.getScore(),
                                                                                metadata.getDescription());
                        ScSubmission submission = newMetadata.extendToSubmission(null, export);
                        Files.writeString(path, submission.getData(), StandardOpenOption.TRUNCATE_EXISTING);
                    }
                }
            }
        }
    }
}