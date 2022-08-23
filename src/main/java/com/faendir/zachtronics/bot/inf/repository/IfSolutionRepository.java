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

package com.faendir.zachtronics.bot.inf.repository;

import com.faendir.zachtronics.bot.git.GitRepository;
import com.faendir.zachtronics.bot.inf.model.*;
import com.faendir.zachtronics.bot.model.DisplayContext;
import com.faendir.zachtronics.bot.model.StringFormat;
import com.faendir.zachtronics.bot.reddit.RedditService;
import com.faendir.zachtronics.bot.reddit.Subreddit;
import com.faendir.zachtronics.bot.repository.AbstractSolutionRepository;
import com.faendir.zachtronics.bot.repository.CategoryRecord;
import com.faendir.zachtronics.bot.repository.SubmitResult;
import com.faendir.zachtronics.bot.utils.Markdown;
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

import static com.faendir.zachtronics.bot.inf.model.IfCategory.*;


@Component
@RequiredArgsConstructor
public class IfSolutionRepository extends AbstractSolutionRepository<IfCategory, IfPuzzle, IfScore, IfSubmission, IfRecord, IfSolution> {
    private static final IfCategory[][] CATEGORIES = {{C, CNG}, {F}, {B}}; // TODO

    private final RedditService redditService;
    @Getter(AccessLevel.PROTECTED)
    @Qualifier("ifRepository")
    private final GitRepository gitRepo;
    @Getter
    Function<String[], IfSolution> solUnmarshaller = IfSolution::unmarshal;

    @NotNull
    @Override
    public SubmitResult<IfRecord, IfCategory> submit(@NotNull IfSubmission submission) {
        try (GitRepository.ReadWriteAccess access = gitRepo.acquireWriteAccess()) {
            return submitOne(access, submission, (s, c) -> access.push());
        }
    }

    @Override
    protected void writeToRedditLeaderboard(IfPuzzle puzzle, Path puzzlePath, @NotNull List<IfSolution> solutions, String updateMessage) {
        if (true) // TODO
            return;
        Map<IfCategory, IfRecord> recordMap = new EnumMap<>(IfCategory.class);
        for (IfSolution solution: solutions) {
            IfRecord record = solution.extendToRecord(puzzle,
                                                      makeArchiveLink(puzzle, solution.getScore()),
                                                      makeArchivePath(puzzlePath, solution.getScore()));
            for (IfCategory category : solution.getCategories()) {
                recordMap.put(category, record);
            }
        }

        List<String> lines = Pattern.compile("\\r?\\n")
                                    .splitAsStream(redditService.getWikiPage(Subreddit.INFINIFACTORY, "index"))
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

            IfCategory[] blockCategories = CATEGORIES[rowIdx];

            boolean usefulLine = false;
            for (int i = 0; i < 3; i++) {
                IfCategory thisCategory = blockCategories[i];
                row.append(" | ");
                IfRecord thisRecord = recordMap.get(thisCategory);
                if (rowIdx == 0 || thisRecord != recordMap.get(CATEGORIES[0][i])) {
                    DisplayContext<IfCategory> displayContext = new DisplayContext<>(StringFormat.REDDIT, thisCategory);
                    String cell = thisRecord.toDisplayString(displayContext);
                    row.append(cell);
                    usefulLine = true;
                }
            }
            if (usefulLine)
                it.add(row.toString());
        }

        redditService.updateWikiPage(Subreddit.INFINIFACTORY, "index", String.join("\n", lines), updateMessage);
    }

    @Override
    @NotNull
    protected Path relativePuzzlePath(@NotNull IfPuzzle puzzle) {
        return Paths.get(puzzle.getId());
    }

    @NotNull
    static String makeScoreFilename(@NotNull IfScore score) {
        return score.toDisplayString(DisplayContext.fileName()) + ".txt";
    }

    @NotNull
    @Override
    protected String makeArchiveLink(@NotNull IfPuzzle puzzle, @NotNull IfScore score) {
        return makeArchiveLink(puzzle, makeScoreFilename(score));
    }

    @Override
    @NotNull
    protected Path makeArchivePath(@NotNull Path puzzlePath, IfScore score) {
        return puzzlePath.resolve(makeScoreFilename(score));
    }

    /** Sorting order of the solutions index */
    private static final Comparator<IfSolution> COMPARATOR = Comparator.comparing(IfSolution::getScore, C.getScoreComparator());

    /**
     * @param solutions the list is modified with the updated state
     */
    @Override
    @NotNull
    protected SubmitResult<IfRecord, IfCategory> archiveOne(@NotNull GitRepository.ReadWriteAccess access,
                                                            @NotNull List<IfSolution> solutions,
                                                            @NotNull IfSubmission submission) {
        IfPuzzle puzzle = submission.getPuzzle();
        Path puzzlePath = getPuzzlePath(access, puzzle);

        List<CategoryRecord<IfRecord, IfCategory>> beatenCategoryRecords = new ArrayList<>();
        IfSolution candidate = new IfSolution(submission.getScore(), submission.getAuthor(), submission.getDisplayLinks());

        try {
            for (ListIterator<IfSolution> it = solutions.listIterator(); it.hasNext(); ) {
                IfSolution solution = it.next();
                int r = dominanceCompare(candidate.getScore(), solution.getScore());
                if (r > 0) {
                    // TODO actually return all of the beating sols
                    CategoryRecord<IfRecord, IfCategory> categoryRecord =
                            solution.extendToCategoryRecord(puzzle,
                                                            makeArchiveLink(puzzle, solution.getScore()),
                                                            makeArchivePath(puzzlePath, solution.getScore()));
                    return new SubmitResult.NothingBeaten<>(Collections.singletonList(categoryRecord));
                }
                else if (r < 0) {
                    // allow same-score changes if you bring an image or you are the original author and don't regress the image state
                    if (candidate.getScore().equals(solution.getScore()) &&
                        candidate.getDisplayLinks().isEmpty() &&
                        !(candidate.getAuthor().equals(solution.getAuthor()) && solution.getDisplayLinks().isEmpty())) {
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
            for (IfSolution solution: solutions) {
                EnumSet<IfCategory> lostCategories = EnumSet.noneOf(IfCategory.class);
                for (IfCategory category : solution.getCategories()) {
                    if (category.supportsScore(candidate.getScore()) &&
                        category.getScoreComparator().compare(candidate.getScore(), solution.getScore()) < 0) {
                        lostCategories.add(category);
                    }
                }
                if (!lostCategories.isEmpty()) {
                    // add a CR holding the lost categories, then correct the solutions
                    CategoryRecord<IfRecord, IfCategory> beatenCR = new CategoryRecord<>(
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

            String filename = makeScoreFilename(candidate.getScore());
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
    private static int dominanceCompare(@NotNull IfScore s1, @NotNull IfScore s2) {
        int r1 = Integer.compare(s1.getCycles(), s2.getCycles());
        int r2 = Integer.compare(s1.getFootprint(), s2.getFootprint());
        int r3 = Integer.compare(s1.getBlocks(), s2.getBlocks());
        int r4 = Boolean.compare(s1.usesGRA(), s2.usesGRA());
        if (r1 <= 0 && r2 <= 0 && r3 <= 0 && r4 <= 0) {
            // s1 dominates
            return -1;
        }
        else if (r1 >= 0 && r2 >= 0 && r3 >= 0 && r4 >= 0) {
            // s2 dominates
            return 1;
        }
        else {
            // equal is already captured by the 1st check, this is for "not comparable"
            return 0;
        }
    }
}
