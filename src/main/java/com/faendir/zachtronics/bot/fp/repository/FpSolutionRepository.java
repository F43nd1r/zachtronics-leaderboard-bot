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

package com.faendir.zachtronics.bot.fp.repository;

import com.faendir.zachtronics.bot.fp.model.*;
import com.faendir.zachtronics.bot.git.GitRepository;
import com.faendir.zachtronics.bot.model.DisplayContext;
import com.faendir.zachtronics.bot.model.StringFormat;
import com.faendir.zachtronics.bot.reddit.RedditService;
import com.faendir.zachtronics.bot.reddit.Subreddit;
import com.faendir.zachtronics.bot.repository.AbstractSolutionRepository;
import com.faendir.zachtronics.bot.repository.CategoryRecord;
import com.faendir.zachtronics.bot.repository.SubmitResult;
import com.faendir.zachtronics.bot.utils.Markdown;
import com.faendir.zachtronics.bot.validation.ValidationResult;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.faendir.zachtronics.bot.fp.model.FpCategory.RCRF;

@Component
@RequiredArgsConstructor
public class FpSolutionRepository extends AbstractSolutionRepository<FpCategory, FpPuzzle, FpScore, FpSubmission, FpRecord, FpSolution> {
    private static final FpCategory[][] CATEGORIES = {};

    private final RedditService redditService;
    @Getter(AccessLevel.PROTECTED)
    @Qualifier("fpRepository")
    private final GitRepository gitRepo;
    @Getter
    Function<String[], FpSolution> solUnmarshaller = FpSolution::unmarshal;

    @NotNull
    @Override
    public SubmitResult<FpRecord, FpCategory> submit(@NotNull FpSubmission submission) {
        try (GitRepository.ReadWriteAccess access = gitRepo.acquireWriteAccess()) {
            return submitOne(access, submission, (s, c) -> access.push());
        }
    }

    @NotNull
    @Override
    public List<SubmitResult<FpRecord, FpCategory>> submitAll(
            @NotNull Collection<? extends ValidationResult<FpSubmission>> validationResults) {
        try (GitRepository.ReadWriteAccess access = gitRepo.acquireWriteAccess()) {
            List<SubmitResult<FpRecord, FpCategory>> submitResults = new ArrayList<>();

            for (ValidationResult<FpSubmission> validationResult : validationResults) {
                if (validationResult instanceof ValidationResult.Valid<FpSubmission>) {
                    FpSubmission submission = validationResult.getSubmission();
                    submitResults.add(submitOne(access, submission, (sub, wonCategories) -> {}));
                }
                else {
                    submitResults.add(new SubmitResult.Failure<>(validationResult.getMessage()));
                }
            }

            access.push();
            return submitResults;
        }
    }

    @Override
    protected void writeToRedditLeaderboard(FpPuzzle puzzle, Path puzzlePath, @NotNull List<FpSolution> solutions, String updateMessage) {
        if (true) // TODO
            return;
        
        Map<FpCategory, FpRecord> recordMap = new EnumMap<>(FpCategory.class);
        for (FpSolution solution: solutions) {
            FpRecord record = solution.extendToRecord(puzzle,
                                                      makeArchiveLink(puzzle, solution.getScore()),
                                                      makeArchivePath(puzzlePath, solution.getScore()));
            for (FpCategory category : solution.getCategories()) {
                recordMap.put(category, record);
            }
        }

        List<String> lines = Pattern.compile("\\r?\\n")
                                    .splitAsStream(redditService.getWikiPage(Subreddit.SHENZHEN_IO, "index"))
                                    .collect(Collectors.toList()); // mutable list
        Pattern puzzleRegex = Pattern.compile("^\\| \\[" + Pattern.quote(puzzle.getDisplayName()));

        ListIterator<String> it = lines.listIterator();

        // | [Puzzle](https://zlbb) | [(**c**/pp/l)](https://cp.txt) | [(c/**pp**/l)](https://pc.txt) | [(c/pp/**l**)](https://lc.txt)
        // |                        | [(**c**/pp/l)](https://cl.txt) |                                | [(c/pp/**l**)](https://lp.txt)
        while (it.hasNext()) {
            String line = it.next();
            if (puzzleRegex.matcher(line).find()) {
                it.remove();
                break;
            }
        }

        while (it.hasNext()) {
            String line = it.next();
            if (line.equals("|") || line.isBlank()) {
                it.previous();
                break;
            } else {
                it.remove();
            }
        }

        for (int rowIdx = 0; rowIdx < 2; rowIdx++) {
            StringBuilder row = new StringBuilder("| ");
            if (rowIdx == 0)
                row.append(Markdown.linkOrText(puzzle.getDisplayName(), puzzle.getLink()));

            FpCategory[] blockCategories = CATEGORIES[rowIdx];

            boolean usefulLine = false;
            for (int i = 0; i < 3; i++) {
                FpCategory thisCategory = blockCategories[i];
                row.append(" | ");
                FpRecord thisRecord = recordMap.get(thisCategory);
                if (rowIdx == 0 || thisRecord != recordMap.get(CATEGORIES[0][i])) {
                    DisplayContext<FpCategory> displayContext = new DisplayContext<>(StringFormat.REDDIT, thisCategory);
                    String cell = thisRecord.toDisplayString(displayContext);
                    row.append(cell);
                    usefulLine = true;
                }
            }
            if (usefulLine)
                it.add(row.toString());
        }

        redditService.updateWikiPage(Subreddit.SHENZHEN_IO, "index", String.join("\n", lines), updateMessage);
    }

    @Override
    @NotNull
    protected Path relativePuzzlePath(@NotNull FpPuzzle puzzle) {
        return Paths.get(puzzle.getGroup().getRepoFolder()).resolve(puzzle.name());
    }

    @NotNull
    static String makeFilename(@NotNull String puzzleId, @NotNull FpScore score) {
        return puzzleId + "-" + score.toDisplayString(DisplayContext.fileName()) + ".txt";
    }

    @NotNull
    @Override
    protected String makeArchiveLink(@NotNull FpPuzzle puzzle, @NotNull FpScore score) {
        return makeArchiveLink(puzzle, makeFilename(puzzle.name(), score));
    }

    @Override
    @NotNull
    protected Path makeArchivePath(@NotNull Path puzzlePath, FpScore score) {
        return puzzlePath.resolve(makeFilename(puzzlePath.getFileName().toString(), score));
    }

    /** Sorting order of the solutions index */
    private static final Comparator<FpSolution> COMPARATOR = Comparator.comparing(FpSolution::getScore, RCRF.getScoreComparator());

    /**
     * @param solutions the list is modified with the updated state
     */
    @Override
    @NotNull
    protected SubmitResult<FpRecord, FpCategory> archiveOne(@NotNull GitRepository.ReadWriteAccess access,
                                                            @NotNull List<FpSolution> solutions,
                                                            @NotNull FpSubmission submission) {
        FpPuzzle puzzle = submission.getPuzzle();
        Path puzzlePath = getPuzzlePath(access, puzzle);

        List<CategoryRecord<FpRecord, FpCategory>> beatenCategoryRecords = new ArrayList<>();
        FpSolution candidate = new FpSolution(submission.getScore(), submission.getAuthor(), submission.getDisplayLink());

        try {
            for (ListIterator<FpSolution> it = solutions.listIterator(); it.hasNext(); ) {
                FpSolution solution = it.next();
                int r = dominanceCompare(candidate.getScore(), solution.getScore());
                if (r > 0) {
                    // TODO actually return all of the beating sols
                    CategoryRecord<FpRecord, FpCategory> categoryRecord =
                            solution.extendToCategoryRecord(puzzle,
                                                            makeArchiveLink(puzzle, solution.getScore()),
                                                            makeArchivePath(puzzlePath, solution.getScore()));
                    return new SubmitResult.NothingBeaten<>(Collections.singletonList(categoryRecord));
                }
                else if (r < 0) {
                    // allow same-score solution changes only if you are the original author
                    if (candidate.getScore().equals(solution.getScore()) &&
                        !candidate.getAuthor().equals(solution.getAuthor())) {
                        return new SubmitResult.AlreadyPresent<>();
                    }
                    // remove beaten score and get categories
                    candidate.getCategories().addAll(solution.getCategories());
                    Files.delete(makeArchivePath(puzzlePath, solution.getScore()));
                    beatenCategoryRecords.add(solution.extendToCategoryRecord(puzzle, null, null)); // the beaten record has no data anymore
                    it.remove();
                }
            }

            // the new record may have gained categories of records it didn't pareto-beat, do the transfers
            for (FpSolution solution: solutions) {
                EnumSet<FpCategory> lostCategories = EnumSet.noneOf(FpCategory.class);
                for (FpCategory category : solution.getCategories()) {
                    if (category.supportsScore(candidate.getScore()) &&
                        category.getScoreComparator().compare(candidate.getScore(), solution.getScore()) < 0) {
                        lostCategories.add(category);
                    }
                }
                if (!lostCategories.isEmpty()) {
                    // add a CR holding the lost categories, then correct the solutions
                    CategoryRecord<FpRecord, FpCategory> beatenCR = new CategoryRecord<>(
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

            int index = Collections.binarySearch(solutions, candidate, COMPARATOR);
            if (index < 0) {
                index = -index - 1;
            }
            solutions.add(index, candidate);

            String filename = makeFilename(submission.getPuzzle().name(), candidate.getScore());
            Path solutionPath = puzzlePath.resolve(filename);
            String data = submission.getData().replaceFirst("\\s*$", "\n"); // ensure there is one and only one newline at the end
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

    /** If equal, s1 dominates */
    private static int dominanceCompare(@NotNull FpScore s1, @NotNull FpScore s2) {
        int r1 = Integer.compare(s1.getRules(), s2.getRules());
        int r2 = Integer.compare(s1.getConditionalRules(), s2.getConditionalRules());
        int r3 = Integer.compare(s1.getFrames(), s2.getFrames());
        // waste is a special boy, the metric has no direction, so different waste = uncomparable
        int r4 = Integer.compare(s1.getWaste(), s2.getWaste());

        if (r1 <= 0 && r2 <= 0 && r3 <= 0 && r4 == 0) {
            // s1 dominates
            return -1;
        }
        else if (r1 >= 0 && r2 >= 0 && r3 >= 0 && r4 == 0) {
            // s2 dominates
            return 1;
        }
        else {
            // equal is already captured by the 1st check, this is for "not comparable"
            return 0;
        }
    }
}
