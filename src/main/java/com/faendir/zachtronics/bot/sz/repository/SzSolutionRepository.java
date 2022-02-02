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

package com.faendir.zachtronics.bot.sz.repository;

import com.faendir.zachtronics.bot.git.GitRepository;
import com.faendir.zachtronics.bot.model.DisplayContext;
import com.faendir.zachtronics.bot.model.StringFormat;
import com.faendir.zachtronics.bot.reddit.RedditService;
import com.faendir.zachtronics.bot.reddit.Subreddit;
import com.faendir.zachtronics.bot.repository.AbstractSolutionRepository;
import com.faendir.zachtronics.bot.repository.CategoryRecord;
import com.faendir.zachtronics.bot.repository.SubmitResult;
import com.faendir.zachtronics.bot.sz.model.*;
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

import static com.faendir.zachtronics.bot.sz.model.SzCategory.*;

@Component
@RequiredArgsConstructor
public class SzSolutionRepository extends AbstractSolutionRepository<SzCategory, SzPuzzle, SzScore, SzSubmission, SzRecord, SzSolution> {
    private static final SzCategory[][] CATEGORIES = {{CP, PC, LC},
                                                      {CL, PL, LP}};

    private final RedditService redditService;
    @Getter(AccessLevel.PROTECTED)
    @Qualifier("szRepository")
    private final GitRepository gitRepo;
    @Getter
    Function<String[], SzSolution> solUnmarshaller = SzSolution::unmarshal;

    @NotNull
    @Override
    public SubmitResult<SzRecord, SzCategory> submit(@NotNull SzSubmission submission) {
        try (GitRepository.ReadWriteAccess access = getGitRepo().acquireWriteAccess()) {
            return submitOne(access, submission, (s, c) -> access.push());
        }
    }

    @Override
    protected void writeToRedditLeaderboard(SzPuzzle puzzle, Path puzzlePath, @NotNull List<SzSolution> solutions, String updateMessage) {
        Map<SzCategory, SzRecord> recordMap = new EnumMap<>(SzCategory.class);
        for (SzSolution solution: solutions) {
            SzRecord record = solution.extendToRecord(puzzle,
                                                      makeArchiveLink(puzzle, solution.getScore()),
                                                      makeArchivePath(puzzlePath, solution.getScore()));
            for (SzCategory category : solution.getCategories()) {
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

            SzCategory[] blockCategories = CATEGORIES[rowIdx];

            boolean usefulLine = false;
            for (int i = 0; i < 3; i++) {
                SzCategory thisCategory = blockCategories[i];
                row.append(" | ");
                SzRecord thisRecord = recordMap.get(thisCategory);
                if (rowIdx == 0 || thisRecord != recordMap.get(CATEGORIES[0][i])) {
                    DisplayContext<SzCategory> displayContext = new DisplayContext<>(StringFormat.REDDIT, thisCategory);
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
    protected Path relativePuzzlePath(@NotNull SzPuzzle puzzle) {
        return Paths.get(puzzle.getGroup().getRepoFolder()).resolve(puzzle.getId());
    }

    @NotNull
    static String makeFilename(@NotNull String puzzleId, @NotNull SzScore score) {
        return puzzleId + "-" + score.toDisplayString(DisplayContext.fileName()) + ".txt";
    }

    @NotNull
    @Override
    protected String makeArchiveLink(@NotNull SzPuzzle puzzle, @NotNull SzScore score) {
        return makeArchiveLink(puzzle, makeFilename(puzzle.getId(), score));
    }

    @Override
    @NotNull
    protected Path makeArchivePath(@NotNull Path puzzlePath, SzScore score) {
        return puzzlePath.resolve(makeFilename(puzzlePath.getFileName().toString(), score));
    }

    /** Sorting order of the solutions index */
    private static final Comparator<SzSolution> COMPARATOR = Comparator.comparing(SzSolution::getScore, SzCategory.CP.getScoreComparator());

    /**
     * @param solutions the list is modified with the updated state
     */
    @Override
    @NotNull
    protected SubmitResult<SzRecord, SzCategory> archiveOne(@NotNull GitRepository.ReadWriteAccess access,
                                                            @NotNull List<SzSolution> solutions,
                                                            @NotNull SzSubmission submission) {
        SzPuzzle puzzle = submission.getPuzzle();
        Path puzzlePath = getPuzzlePath(access, puzzle);

        List<CategoryRecord<SzRecord, SzCategory>> beatenCategoryRecords = new ArrayList<>();
        SzSolution candidate = new SzSolution(submission.getScore(), submission.getAuthor());

        try {
            for (ListIterator<SzSolution> it = solutions.listIterator(); it.hasNext(); ) {
                SzSolution solution = it.next();
                int r = dominanceCompare(candidate.getScore(), solution.getScore());
                if (r > 0) {
                    // TODO actually return all of the beating sols
                    CategoryRecord<SzRecord, SzCategory> categoryRecord =
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
            for (SzSolution solution: solutions) {
                EnumSet<SzCategory> lostCategories = EnumSet.noneOf(SzCategory.class);
                for (SzCategory category : solution.getCategories()) {
                    if (category.supportsScore(candidate.getScore()) &&
                        category.getScoreComparator().compare(candidate.getScore(), solution.getScore()) < 0) {
                        lostCategories.add(category);
                    }
                }
                if (!lostCategories.isEmpty()) {
                    // add a CR holding the lost categories, then correct the solutions
                    CategoryRecord<SzRecord, SzCategory> beatenCR = new CategoryRecord<>(
                            solution.extendToRecord(puzzle,
                                                    makeArchiveLink(puzzle, solution.getScore()),
                                                    makeArchivePath(puzzlePath, solution.getScore())),
                            lostCategories);
                    beatenCategoryRecords.add(beatenCR);

                    solution.getCategories().removeAll(lostCategories);
                    candidate.getCategories().addAll(lostCategories);
                }
            }

            int index = Collections.binarySearch(solutions, candidate, COMPARATOR);
            if (index < 0) {
                index = -index - 1;
            }
            solutions.add(index, candidate);

            String filename = makeFilename(submission.getPuzzle().getId(), candidate.getScore());
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
    private static int dominanceCompare(@NotNull SzScore s1, @NotNull SzScore s2) {
        int r1 = Integer.compare(s1.getCost(), s2.getCost());
        int r2 = Integer.compare(s1.getPower(), s2.getPower());
        int r3 = Integer.compare(s1.getLines(), s2.getLines());
        if (r1 <= 0 && r2 <= 0 && r3 <= 0) {
            // s1 dominates
            return -1;
        }
        else if (r1 >= 0 && r2 >= 0 && r3 >= 0) {
            // s2 dominates
            return 1;
        }
        else {
            // equal is already captured by the 1st check, this is for "not comparable"
            return 0;
        }
    }
}
