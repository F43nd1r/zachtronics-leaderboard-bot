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

package com.faendir.zachtronics.bot.repository;

import com.faendir.zachtronics.bot.git.GitRepository;
import com.faendir.zachtronics.bot.model.Record;
import com.faendir.zachtronics.bot.model.*;
import com.opencsv.*;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class AbstractSolutionRepository<C extends Enum<C> & Category, P extends Puzzle<C>, S extends Score<C>,
                                                 Sub extends Submission<C, P>, R extends Record<C>, Sol extends Solution<C, P, S, R>>
        implements SolutionRepository<C, P, Sub, R> {

    protected abstract GitRepository getGitRepo();
    protected abstract Function<String[], Sol> getSolUnmarshaller();

    @NotNull
    @Override
    public List<CategoryRecord<R, C>> findCategoryHolders(@NotNull P puzzle, boolean includeFrontier) {
        try (GitRepository.ReadAccess access = getGitRepo().acquireReadAccess()) {
            Path puzzlePath = access.getRepo().toPath().resolve(relativePuzzlePath(puzzle));

            List<Sol> solutions = unmarshalSolutions(puzzlePath);

            List<CategoryRecord<R, C>> result = new ArrayList<>();
            for (Sol sol : solutions) {
                if (includeFrontier || !sol.getCategories().isEmpty()) {
                    CategoryRecord<R, C> categoryRecord = sol.extendToCategoryRecord(puzzle,
                                                                                     makeArchiveLink(puzzle, sol.getScore()),
                                                                                     makeArchivePath(puzzlePath, sol.getScore()));
                    result.add(categoryRecord);
                }
            }
            return result;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @NotNull
    protected SubmitResult<R, C> submitOne(@NotNull GitRepository.ReadWriteAccess access, @NotNull Sub submission) {
        P puzzle = submission.getPuzzle();
        Path puzzlePath = getPuzzlePath(access, puzzle);
        List<Sol> solutions;
        try {
            solutions = unmarshalSolutions(puzzlePath);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        SubmitResult<R, C> submitResult = archiveOne(access, solutions, submission);

        if (submitResult instanceof SubmitResult.Success<R, C>) {
            Sol submissionSolution = solutions.stream()
                                              .filter(s -> s.getScore().equals(submission.getScore()))
                                              .findFirst()
                                              .orElseThrow();
            Set<C> wonCategories = submissionSolution.getCategories();
            if (!wonCategories.isEmpty()) {
                // write the reddit lb, as there are changes to write
                String updateMessage = puzzle.getDisplayName() + " " + submission.getScore().toDisplayString() +
                                       " by " + submission.getAuthor();
                writeToRedditLeaderboard(puzzle, puzzlePath, solutions, updateMessage);
            }
        }

        return submitResult;
    }

    protected abstract SubmitResult<R,C> archiveOne(GitRepository.ReadWriteAccess access, List<Sol> solutions, Sub submission);

    @NotNull
    protected String commit(@NotNull GitRepository.ReadWriteAccess access, @NotNull Sub submission, @NotNull Path puzzlePath) {
        access.addAll(puzzlePath.toFile());
        String result = Stream.concat(access.status().getChanged().stream(),
                                      access.status().getAdded().stream())
                              .map(f -> "[" + f.replaceFirst(".+/", "") + "]" +
                                        "(" + getGitRepo().getRawFilesUrl() + "/" + f + ")")
                              .collect(Collectors.joining(", "));
        RevCommit rev = access.commit("Added " + submission.getScore().toDisplayString() +
                                      " for " + submission.getPuzzle().getDisplayName() +
                                      " by " + submission.getAuthor());
        result += "\n[commit " + rev.name().substring(0, 7) + "]" +
                  "(" + getGitRepo().getUrl().replaceFirst(".git$", "") + "/commit/" + rev.name() + ")";
        return result;
    }

    /**
     * @return a mutable list
     */
    public List<Sol> unmarshalSolutions(@NotNull Path puzzlePath) throws IOException {
        Path indexPath = puzzlePath.resolve("solutions.psv");
        try (BufferedReader reader = Files.newBufferedReader(indexPath)) {

            CSVParser parser = new CSVParserBuilder().withSeparator('|').withFieldAsNull(CSVReaderNullFieldIndicator.BOTH).build();
            CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(parser).build();
            return StreamSupport.stream(csvReader.spliterator(), false)
                                .map(getSolUnmarshaller())
                                .collect(Collectors.toList());
        }
        catch (NoSuchFileException e) {
            Files.createDirectories(puzzlePath);
            Files.createFile(indexPath);
            return new ArrayList<>();
        }
    }

    public void marshalSolutions(@NotNull List<Sol> solutions, @NotNull Path puzzlePath) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(puzzlePath.resolve("solutions.psv"),
                                                             StandardOpenOption.TRUNCATE_EXISTING)) {
            ICSVWriter csvWriter = new CSVWriterBuilder(writer).withSeparator('|').build();
            csvWriter.writeAll(solutions.stream().map(Sol::marshal)::iterator, false);
        }
    }

    protected abstract void writeToRedditLeaderboard(P puzzle, Path puzzlePath, @NotNull List<Sol> solutions, String updateMessage);

    @NotNull
    protected Path getPuzzlePath(@NotNull GitRepository.ReadAccess access, P puzzle) {
        return access.getRepo().toPath().resolve(relativePuzzlePath(puzzle));
    }

    @NotNull
    protected abstract Path relativePuzzlePath(@NotNull P puzzle);

    @NotNull
    protected abstract String makeArchiveLink(@NotNull P puzzle, @NotNull S score);

    @NotNull
    protected abstract Path makeArchivePath(@NotNull Path puzzlePath, S score);

    protected String makeArchiveLink(@NotNull P puzzle, @NotNull String filename) {
        return String.format("%s/%s/%s", getGitRepo().getRawFilesUrl(), relativePuzzlePath(puzzle),
                             filename);
    }
}
