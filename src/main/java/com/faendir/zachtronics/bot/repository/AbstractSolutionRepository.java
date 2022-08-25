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
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class AbstractSolutionRepository<C extends Enum<C> & CategoryJava<C, S, ?, ?>, P extends Puzzle<C>, S extends Score<C>,
                                                 Sub extends Submission<C, P>, R extends Record<C>, Sol extends Solution<C, P, S, R>>
        implements SolutionRepository<C, P, Sub, R> {

    protected abstract GitRepository getGitRepo();
    protected abstract Class<C> getCategoryClass();
    protected abstract Function<String[], Sol> getSolUnmarshaller();
    /** Sorting order of the solutions index */
    protected abstract Comparator<Sol> getArchiveComparator();

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
    protected SubmitResult<R, C> submitOne(@NotNull GitRepository.ReadWriteAccess access, @NotNull Sub submission,
                                           BiConsumer<Sub, Collection<C>> successCallback) {
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
            successCallback.accept(submission, wonCategories);
        }

        return submitResult;
    }

    protected abstract Sol makeCandidateSolution(@NotNull Sub submission);
    /** If equal, s1 dominates */
    protected abstract int dominanceCompare(@NotNull S s1, @NotNull S s2);
    /** Allow same-score changes if you bring a display link or you are the original author and don't regress the display link state */
    protected abstract boolean alreadyPresent(@NotNull Sol candidate, @NotNull Sol solution);

    protected void removeOrReplaceFromIndex(@NotNull Sol candidate, @NotNull Sol solution, @NotNull ListIterator<Sol> it) {
        it.remove();
    }

    /**
     * @param solutions the list is modified with the updated state
     */
    protected SubmitResult<R, C> archiveOne(@NotNull GitRepository.ReadWriteAccess access,
                                            @NotNull List<Sol> solutions,
                                            @NotNull Sub submission) {
        P puzzle = submission.getPuzzle();
        Path puzzlePath = getPuzzlePath(access, puzzle);

        List<CategoryRecord<R, C>> beatenCategoryRecords = new ArrayList<>();
        Sol candidate = makeCandidateSolution(submission);

        try {
            for (ListIterator<Sol> it = solutions.listIterator(); it.hasNext(); ) {
                Sol solution = it.next();
                int r = dominanceCompare(candidate.getScore(), solution.getScore());
                if (r > 0) {
                    // TODO actually return all of the beating sols
                    CategoryRecord<R, C> categoryRecord =
                            solution.extendToCategoryRecord(puzzle,
                                                            makeArchiveLink(puzzle, solution.getScore()),
                                                            makeArchivePath(puzzlePath, solution.getScore()));
                    return new SubmitResult.NothingBeaten<>(Collections.singletonList(categoryRecord));
                }
                else if (r < 0) {
                    if (alreadyPresent(candidate, solution)) {
                        return new SubmitResult.AlreadyPresent<>();
                    }

                    // remove beaten score and get categories
                    candidate.getCategories().addAll(solution.getCategories());
                    Files.deleteIfExists(makeArchivePath(puzzlePath, solution.getScore()));
                    beatenCategoryRecords.add(solution.extendToCategoryRecord(puzzle, null, null)); // the beaten record has no data anymore
                    removeOrReplaceFromIndex(candidate, solution, it);
                }
            }

            // the new record may have gained categories of records it didn't pareto-beat, do the transfers
            for (Sol solution: solutions) {
                EnumSet<C> lostCategories = EnumSet.noneOf(getCategoryClass());
                for (C category : solution.getCategories()) {
                    if (category.supportsScore(candidate.getScore()) &&
                        category.getScoreComparator().compare(candidate.getScore(), solution.getScore()) < 0) {
                        lostCategories.add(category);
                    }
                }
                if (!lostCategories.isEmpty()) {
                    // add a CR holding the lost categories, then correct the solutions
                    CategoryRecord<R, C> beatenCR = new CategoryRecord<>(
                            solution.extendToRecord(puzzle,
                                                    makeArchiveLink(puzzle, solution.getScore()),
                                                    makeArchivePath(puzzlePath, solution.getScore())),
                            lostCategories);
                    beatenCategoryRecords.add(beatenCR);

                    solution.getCategories().removeAll(lostCategories);
                    candidate.getCategories().addAll(lostCategories);
                }
            }

            // if it's the first in line our new sol steals from the void all the categories it can
            if (solutions.isEmpty()) {
                puzzle.getSupportedCategories().stream()
                      .filter(c -> c.supportsScore(candidate.getScore()))
                      .forEach(candidate.getCategories()::add);
            }

            int index = Collections.binarySearch(solutions, candidate, getArchiveComparator());
            if (index < 0) {
                index = -index - 1;
            }
            solutions.add(index, candidate);

            Path solutionPath = makeArchivePath(puzzlePath, candidate.getScore());
            String data = ((String)submission.getData()).replaceFirst("\\s*$", "\n"); // ensure there is one and only one newline at the end
            Files.writeString(solutionPath, data, StandardOpenOption.CREATE_NEW);

            marshalSolutions(solutions, puzzlePath);
        }
        catch (IOException e) {
            // failures could happen after we dirtied the repo, so we call reset&clean on the puzzle dir
            access.resetAndClean(puzzlePath.toFile());
            return new SubmitResult.Failure<>(e.toString());
        }

        if (access.status().isClean()) {
            // the same exact sol was already archived,
            return new SubmitResult.AlreadyPresent<>();
        }

        String result = commit(access, submission, puzzlePath);
        return new SubmitResult.Success<>(result, beatenCategoryRecords);
    }

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

    public void rebuildRedditLeaderboard(P puzzle, String updateMessage) {
        try (GitRepository.ReadWriteAccess access = getGitRepo().acquireWriteAccess()) {
            Path puzzlePath = getPuzzlePath(access, puzzle);
            List<Sol> solutions = unmarshalSolutions(puzzlePath);
            writeToRedditLeaderboard(puzzle, puzzlePath, solutions, updateMessage);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
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
