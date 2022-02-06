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

import com.faendir.zachtronics.bot.git.GitRepository;
import com.faendir.zachtronics.bot.model.DisplayContext;
import com.faendir.zachtronics.bot.model.StringFormat;
import com.faendir.zachtronics.bot.reddit.RedditService;
import com.faendir.zachtronics.bot.reddit.Subreddit;
import com.faendir.zachtronics.bot.repository.AbstractSolutionRepository;
import com.faendir.zachtronics.bot.repository.CategoryRecord;
import com.faendir.zachtronics.bot.repository.SubmitResult;
import com.faendir.zachtronics.bot.sc.model.*;
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
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Pattern;

import static com.faendir.zachtronics.bot.sc.model.ScCategory.*;

@Component
@RequiredArgsConstructor
public class ScSolutionRepository extends AbstractSolutionRepository<ScCategory, ScPuzzle, ScScore, ScSubmission, ScRecord, ScSolution> {
    private static final ScCategory[][] CATEGORIES = {{ C,  CNB,  CNP,  CNBP}, { S,  SNB,  SNP,  SNBP},
                                                      {RC, RCNB, RCNP, RCNBP}, {RS, RSNB, RSNP, RSNBP}};

    private final RedditService redditService;
    @Getter(AccessLevel.PROTECTED)
    @Qualifier("scArchiveRepository")
    private final GitRepository gitRepo;
    @Getter
    Function<String[], ScSolution> solUnmarshaller = ScSolution::unmarshal;

    @NotNull
    @Override
    public SubmitResult<ScRecord, ScCategory> submit(@NotNull ScSubmission submission) {
        try (GitRepository.ReadWriteAccess access = getGitRepo().acquireWriteAccess()) {
            BiConsumer<ScSubmission, Collection<ScCategory>> successCallback = (sub, wonCategories) -> {
                access.push();
                if (!wonCategories.isEmpty()) {
                    String redditAnnouncement = makeRedditAnnouncement(sub, wonCategories);
                    postAnnouncementToReddit(redditAnnouncement);
                }
            };
            return submitOne(access, submission, successCallback);
        }
    }

    @NotNull
    @Override
    public List<SubmitResult<ScRecord, ScCategory>> submitAll(
            @NotNull Collection<? extends ValidationResult<ScSubmission>> validationResults) {
        try (GitRepository.ReadWriteAccess access = gitRepo.acquireWriteAccess()) {
            List<SubmitResult<ScRecord, ScCategory>> submitResults = new ArrayList<>();
            StringJoiner redditAnnouncement = new StringJoiner("  \n");
            BiConsumer<ScSubmission, Collection<ScCategory>> successCallback = (sub, wonCategories) -> {
                if (!wonCategories.isEmpty())
                    redditAnnouncement.add(makeRedditAnnouncement(sub, wonCategories));
            };

            for (ValidationResult<ScSubmission> validationResult : validationResults) {
                if (validationResult instanceof ValidationResult.Valid<ScSubmission>) {
                    ScSubmission submission = validationResult.getSubmission();
                    submitResults.add(submitOne(access, submission, successCallback));
                }
                else {
                    submitResults.add(new SubmitResult.Failure<>(validationResult.getMessage()));
                }
            }

            access.push();
            if (redditAnnouncement.length() != 0) {
                postAnnouncementToReddit(redditAnnouncement.toString());
            }
            return submitResults;
        }
    }

    @Override
    protected void writeToRedditLeaderboard(@NotNull ScPuzzle puzzle, Path puzzlePath, @NotNull List<ScSolution> solutions,
                                            String updateMessage) {

        Map<ScCategory, ScRecord> recordMap = new EnumMap<>(ScCategory.class);
        Map<ScCategory, ScRecord> videoRecordMap = new EnumMap<>(ScCategory.class);
        List<ScRecord> videoRecords = solutions.stream()
                                               .filter(s -> s.getDisplayLink() != null)
                                               .map(s -> s.extendToRecord(puzzle, null, null)) // no export needed
                                               .toList();
        for (ScSolution solution: solutions) {
            ScRecord record = solution.extendToRecord(puzzle,
                                                      makeArchiveLink(puzzle, solution.getScore()),
                                                      makeArchivePath(puzzlePath, solution.getScore()));
            for (ScCategory category : solution.getCategories()) {
                recordMap.put(category, record);
                if (record.getDisplayLink() == null) {
                    videoRecords.stream()
                                .filter(s -> category.supportsScore(s.getScore()))
                                .min(Comparator.comparing(ScRecord::getScore, category.getScoreComparator()))
                                .ifPresent(videoRecord -> videoRecordMap.put(category, videoRecord)); // bosses have no videos at all
                }
            }
        }

        String[] lines = redditService.getWikiPage(Subreddit.SPACECHEM, puzzle.getGroup().getWikiPage()).split("\\r?\\n");
        Pattern puzzleRegex = Pattern.compile("^\\| \\[" + Pattern.quote(puzzle.getDisplayName()));

        int rowIdx = 0;

        // | [Puzzle](https://zlbb) | [(**ccc**/r/ss) author](https://li.nk) | ← | [(ccc/r/**ss**) author](https://li.nk) | ←
        // | [Puzzle - 1 Reactor](https://zlbb) | [(**ccc**/**r**/ss) author](https://li.nk) | ← | [(ccc/**r**/**ss**) author](https://li.nk) | ←
        for (int lineIdx = 0; lineIdx < lines.length; lineIdx++) {
            String line = lines[lineIdx];
            if (puzzleRegex.matcher(line).find()) {
                String[] prevElems = line.trim().split("\\s*\\|\\s*", -1);
                int halfSize = (prevElems.length - 2) / 2;

                StringBuilder row = new StringBuilder("| ");
                int minReactors = Integer.MAX_VALUE;
                String rowTitle = puzzle.getDisplayName();
                if (rowIdx == 1) {
                    minReactors = recordMap.get(ScCategory.RC).getScore().getReactors();
                    rowTitle += " - " + minReactors + " Reactor" + (minReactors == 1 ? "" : "s");
                }
                row.append(Markdown.linkOrText(rowTitle, puzzle.getLink()));

                for (int block = 0; block < 2; block++) {
                    ScCategory[] blockCategories = CATEGORIES[2 * rowIdx + block];
                    ScRecord[] blockRecords = Arrays.stream(blockCategories)
                                                    .map(recordMap::get)
                                                    .toArray(ScRecord[]::new);
                    ScRecord[] blockVideoRecords = Arrays.stream(blockCategories)
                                                         .map(videoRecordMap::get)
                                                         .toArray(ScRecord[]::new);

                    for (int i = 0; i < halfSize; i++) {
                        ScCategory thisCategory = blockCategories[i];
                        row.append(" | ");
                        if (blockRecords[i] != null) {
                            DisplayContext<ScCategory> displayContext = new DisplayContext<>(StringFormat.REDDIT, thisCategory);
                            String cell = makeLeaderboardCell(blockRecords, i, minReactors, displayContext);
                            row.append(cell);
                            if (blockVideoRecords[i] != null) {
                                String videoCell = makeLeaderboardCell(blockVideoRecords, i, Integer.MAX_VALUE, displayContext);
                                if (!cell.equals(videoCell))
                                    row.append(". Top&nbsp;video&nbsp;").append(videoCell);
                            }
                        }
                        else
                            row.append(prevElems[2 + block * halfSize + i]);
                    }
                }
                lines[lineIdx] = row.toString();

                rowIdx++;
            }
            else if (rowIdx != 0) {
                // we've already found the point and now we're past it, we're done
                break;
            }
        }

        redditService.updateWikiPage(Subreddit.SPACECHEM, puzzle.getGroup().getWikiPage(), String.join("\n", lines),
                                     updateMessage);
    }

    @NotNull
    private static String makeLeaderboardCell(@NotNull ScRecord[] blockRecords, int i, int minReactors,
                                              DisplayContext<ScCategory> displayContext) {
        ScRecord record = blockRecords[i];
        for (int prev = 0; prev < i; prev++) {
            if (record == blockRecords[prev]) {
                return "←".repeat(i - prev);
            }
        }

        String reactorPrefix = (record.getScore().getReactors() > minReactors) ? "† " : "";
        return record.toDisplayString(displayContext, reactorPrefix);
    }

    @NotNull
    private String makeRedditAnnouncement(@NotNull ScSubmission submission, Collection<ScCategory> wonCategories) {
        DisplayContext<ScCategory> context = new DisplayContext<>(StringFormat.REDDIT, wonCategories);
        return "Added " + Markdown.fileLinkOrEmpty(makeArchiveLink(submission.getPuzzle(), submission.getScore())) +
               Markdown.linkOrText(submission.getPuzzle().getDisplayName() +
                                   " (" + submission.getScore().toDisplayString(context) + ")",
                                   submission.getDisplayLink()) +
               " by " + submission.getAuthor();
    }

    private void postAnnouncementToReddit(@NotNull String content) {
        // see: https://www.reddit.com/r/spacechem/comments/mmcuzb
        if (!content.isEmpty())
            redditService.postInSubmission("mmcuzb", content);
    }

    @Override
    @NotNull
    protected Path relativePuzzlePath(@NotNull ScPuzzle puzzle) {
        return Paths.get(puzzle.getGroup().name(), puzzle.name());
    }

    @NotNull
    static String makeScoreFilename(@NotNull ScScore score) {
        return score.toDisplayString(DisplayContext.fileName()) + ".txt";
    }

    @NotNull
    @Override
    protected String makeArchiveLink(@NotNull ScPuzzle puzzle, @NotNull ScScore score) {
        return makeArchiveLink(puzzle, makeScoreFilename(score));
    }

    @Override
    @NotNull
    protected Path makeArchivePath(@NotNull Path puzzlePath, ScScore score) {
        return puzzlePath.resolve(makeScoreFilename(score));
    }

    /** Sorting order of the solutions index */
    private static final Comparator<ScSolution> COMPARATOR =
            Comparator.comparing(ScSolution::getScore, ScCategory.C.getScoreComparator()
                                                                   .thenComparing(ScScore::isBugged)
                                                                   .thenComparing(ScScore::isPrecognitive))
                      .thenComparing(s -> s.getDisplayLink() == null);

    /**
     * @param solutions the list is modified with the updated state
     */
    @Override
    @NotNull
    protected SubmitResult<ScRecord, ScCategory> archiveOne(@NotNull GitRepository.ReadWriteAccess access,
                                                            @NotNull List<ScSolution> solutions,
                                                            @NotNull ScSubmission submission) {
        ScPuzzle puzzle = submission.getPuzzle();
        Path puzzlePath = getPuzzlePath(access, puzzle);

        List<CategoryRecord<ScRecord, ScCategory>> beatenCategoryRecords = new ArrayList<>();
        ScSolution candidate = new ScSolution(submission.getScore(), submission.getAuthor(), submission.getDisplayLink(), false);

        try {
            for (ListIterator<ScSolution> it = solutions.listIterator(); it.hasNext(); ) {
                ScSolution solution = it.next();
                int r = dominanceCompare(candidate.getScore(), solution.getScore());
                if (r > 0) {
                    // TODO actually return all of the beating sols
                    CategoryRecord<ScRecord, ScCategory> categoryRecord =
                            solution.extendToCategoryRecord(puzzle,
                                                            makeArchiveLink(puzzle, solution.getScore()),
                                                            makeArchivePath(puzzlePath, solution.getScore()));
                    return new SubmitResult.NothingBeaten<>(Collections.singletonList(categoryRecord));
                }
                else if (r < 0) {
                    // allow same-score changes if you bring a video or you are the original author and don't regress the video state
                    if (candidate.getScore().equals(solution.getScore()) &&
                        candidate.getDisplayLink() == null &&
                        !(candidate.getAuthor().equals(solution.getAuthor()) && solution.getDisplayLink() == null)) {
                        return new SubmitResult.AlreadyPresent<>();
                    }

                    // remove beaten score and get categories
                    candidate.getCategories().addAll(solution.getCategories());
                    if (!solution.isVideoOnly()) // video-only sols have no data
                        Files.delete(makeArchivePath(puzzlePath, solution.getScore()));
                    beatenCategoryRecords.add(solution.extendToCategoryRecord(puzzle, null, null)); // the beaten record has no data anymore

                    if (candidate.getDisplayLink() == null && solution.getDisplayLink() != null) {
                        // we beat the solution, but we can't replace the video, we keep the solution entry as a video-only
                        it.set(new ScSolution(solution.getScore(), solution.getAuthor(), solution.getDisplayLink(),
                                              true)); // empty categories
                    }
                    else {
                        it.remove();
                    }
                }
            }

            // the new record may have gained categories of records it didn't pareto-beat, do the transfers
            for (ScSolution solution: solutions) {
                EnumSet<ScCategory> lostCategories = EnumSet.noneOf(ScCategory.class);
                for (ScCategory category : solution.getCategories()) {
                    if (category.supportsScore(candidate.getScore()) &&
                        category.getScoreComparator().compare(candidate.getScore(), solution.getScore()) < 0) {
                        lostCategories.add(category);
                    }
                }
                if (!lostCategories.isEmpty()) {
                    // add a CR holding the lost categories, then correct the solutions
                    CategoryRecord<ScRecord, ScCategory> beatenCR = new CategoryRecord<>(
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

    /**
     * If equal, sol1 dominates
     */
    private static int dominanceCompare(@NotNull ScScore s1, @NotNull ScScore s2) {
        int r1 = Integer.compare(s1.getCycles(), s2.getCycles());
        int r2 = Integer.compare(s1.getReactors(), s2.getReactors());
        int r3 = Integer.compare(s1.getSymbols(), s2.getSymbols());
        int r4 = Boolean.compare(s1.isBugged(), s2.isBugged());
        int r5 = Boolean.compare(s1.isPrecognitive(), s2.isPrecognitive());

        if (r1 <= 0 && r2 <= 0 && r3 <= 0 && r4 <= 0 && r5 <= 0) {
            // sol1 dominates
            return -1;
        }
        else if (r1 >= 0 && r2 >= 0 && r3 >= 0 && r4 >= 0 && r5 >= 0) {
            // sol2 dominates
            return 1;
        }
        else {
            // equal is already captured by the 1st check, this is for "not comparable"
            return 0;
        }
    }

}
