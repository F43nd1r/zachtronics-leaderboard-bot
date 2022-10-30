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

package com.faendir.zachtronics.bot.sc.repository;

import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.reddit.RedditService;
import com.faendir.zachtronics.bot.reddit.Subreddit;
import com.faendir.zachtronics.bot.repository.CategoryRecord;
import com.faendir.zachtronics.bot.sc.model.*;
import com.faendir.zachtronics.bot.utils.LambdaUtils;
import com.faendir.zachtronics.bot.validation.ValidationResult;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import org.apache.commons.text.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.groupingBy;

@BotTest
@Disabled("Massive tests only for manual testing or migrations")
class ScManualTest {

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
                              .map(ScManualTest::recordToSubmissions)
                              .<ValidationResult<ScSubmission>>map(ValidationResult.Valid::new)
                              .toList();

            repository.submitAll(submissions);

            System.out.println("Done " + p.getDisplayName());
        }
        System.out.println("Done");
    }

    @NotNull
    private static ScSubmission recordToSubmissions(@NotNull ScRecord record) {
        assert record.getDataPath() != null;
        String data = LambdaUtils.<Path, String>uncheckIOException(Files::readString).apply(record.getDataPath());
        return new ScSubmission(record.getPuzzle(), record.getScore(), record.getAuthor(),
                                record.getDisplayLink(), data);
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

                    String[] csvRecord = {record.getScore().toDisplayString(),
                                          author,
                                          record.getDisplayLink(),
                                          record.getDataPath() == null ? "video" : null,
                                          categories};
                    writer.writeNext(csvRecord, false);
                }
            }
        }
    }

    @Test
    public void tagNewCategories() throws IOException {
        Path repoPath = Paths.get("../spacechem/archive");
        List<ScPuzzle> puzzles = List.of(ScPuzzle.values());

        for (ScPuzzle puzzle : puzzles) {
            Path puzzlePath = repoPath.resolve(repository.relativePuzzlePath(puzzle));
            List<ScSolution> solutions = repository.unmarshalSolutions(puzzlePath);
            if (solutions.isEmpty())
                continue;

            solutions.stream().map(ScSolution::getCategories).forEach(Set::clear);
            for (ScCategory category : puzzle.getSupportedCategories()) {
                solutions.stream()
                         .filter(s -> category.supportsScore(s.getScore()))
                         .min(Comparator.comparing(ScSolution::getScore, category.getScoreComparator()))
                         .orElseThrow()
                         .getCategories()
                         .add(category);
            }
            repository.marshalSolutions(solutions, puzzlePath);
        }
    }

    @Test
    public void markVideoOnly() throws IOException {
        Path repoPath = Paths.get("../spacechem/archive");

        for (ScPuzzle puzzle : ScPuzzle.values()) {
            Path puzzlePath = repoPath.resolve(repository.relativePuzzlePath(puzzle));
            List<ScSolution> solutions = repository.unmarshalSolutions(puzzlePath);
            if (solutions.isEmpty())
                continue;

            boolean edited = false;
            for (ListIterator<ScSolution> it = solutions.listIterator(); it.hasNext(); )
            {
                ScSolution solution = it.next();
                Path dataPath = repository.makeArchivePath(puzzlePath, solution.getScore());
                if (!Files.exists(dataPath)) {
                    it.set(solution.withVideoOnly(true));
                    edited = true;
                }
            }
            if (edited)
                repository.marshalSolutions(solutions, puzzlePath);
        }
    }

    @Test
    public void loadSolnetVideos() throws IOException {
        Path solnetDumpPath = Paths.get("../spacechem/solutionnet/data/score_dump.csv");
        Path repoPath = Paths.get("../spacechem/archive");

        CSVReader reader = new CSVReaderBuilder(Files.newBufferedReader(solnetDumpPath)).withFieldAsNull(CSVReaderNullFieldIndicator.BOTH)
                                                                                        .withSkipLines(1)
                                                                                        .build();
        Map<ScPuzzle, List<ScSubmission>> solnetSubmissions = StreamSupport.stream(reader.spliterator(), false)
                                                                           .map(ScManualTest::fromSolnetData)
                                                                           .filter(s -> s.getDisplayLink() != null)
                                                                           .collect(groupingBy(ScSubmission::getPuzzle));

        for (ScPuzzle puzzle : ScPuzzle.values()) {
            List<ScSubmission> puzzleSubmissions = solnetSubmissions.get(puzzle);
            if (puzzleSubmissions == null)
                continue;
            Path puzzlePath = repoPath.resolve(puzzle.getGroup().name()).resolve(puzzle.name());
            List<ScSolution> solutions = repository.unmarshalSolutions(puzzlePath);
            List<ScSolution> newSolutions = new ArrayList<>();
            boolean edited = false;
            for (ScSolution solution : solutions) {
                String matchingVideo = null;
                if (solution.getDisplayLink() == null) {
                    ScScore searchScore = new ScScore(solution.getScore().getCycles(), solution.getScore().getReactors(),
                                                      solution.getScore().getSymbols(), false, false);
                    String searchAuthor = solution.getAuthor();
                    matchingVideo = puzzleSubmissions.stream()
                                                     .filter(s -> s.getScore().equals(searchScore) &&
                                                                  s.getAuthor().equalsIgnoreCase(searchAuthor))
                                                     .findFirst()
                                                     .map(ScSubmission::getDisplayLink)
                                                     .orElse(null);
                }

                if (matchingVideo != null) {
                    String cleanVideoLink = matchingVideo.replaceAll("&hd=1|hd=1&|&feature=share|&feature=related", "");
                    newSolutions.add(solution.withDisplayLink(cleanVideoLink));
                    edited = true;
                }
                else {
                    newSolutions.add(solution);
                }
            }
            if (edited) {
                repository.marshalSolutions(newSolutions, puzzlePath);
            }
        }
    }

    @NotNull
    private static ScSubmission fromSolnetData(@NotNull String[] fields) {
        // Username,Level Category,Level Number,Level Name,Reactor Count,Cycle Count,Symbol Count,Upload Time,Youtube Link
        // Iridium,63corvi,1,QT-1,1,20,5,2011-07-09 07:51:58.320983,https://www.youtube.com/watch?v=hRM5IpSv5aU
        // ToughThought,researchnet,1-7-2,Glyoxylic Acid,1,167,23,2014-01-17 09:32:10.854625,https://youtu.be/GUz4sihkigQ
        // jp26,researchnet,44-3,ResearchNet Published 44-3,1,162,43,2013-01-22 17:47:11.370534,

        assert fields.length == 9 : Arrays.toString(fields);
        String author = fields[0];

        ScPuzzle puzzle;
        String levelName = StringEscapeUtils.unescapeHtml3(fields[3]);
        if (levelName.startsWith("ResearchNet Published")) {
            // badly named resnet puzzles, we extract the real code from the level number
            String levelCode = "published_" + fields[2].replace("-", "_");
            puzzle = ScPuzzle.valueOf(levelCode);
        }
        else {
            try {
                puzzle = ScPuzzle.findUniqueMatchingPuzzle(levelName);
            }
            catch (IllegalStateException e) {
                puzzle = ScPuzzle.findUniqueMatchingPuzzle(levelName + " (" + fields[2] + ")");
            }
        }

        ScScore score = new ScScore(Integer.parseInt(fields[5]), Integer.parseInt(fields[4]), Integer.parseInt(fields[6]), false, false);
        String videoLink = fields[8];
        return new ScSubmission(puzzle, score, author, videoLink, "");
    }
}