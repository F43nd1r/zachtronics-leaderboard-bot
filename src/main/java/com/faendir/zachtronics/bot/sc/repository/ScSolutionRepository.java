/*
 * Copyright (c) 2023
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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Pattern;

import static com.faendir.zachtronics.bot.sc.model.ScCategory.*;

@Component
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public class ScSolutionRepository extends AbstractSolutionRepository<ScCategory, ScPuzzle, ScScore, ScSubmission, ScRecord, ScSolution> {
    private final ScCategory[][] wikiCategories = {{ C,  CNB,  CNP,  CNBP}, { S,  SNB,  SNP,  SNBP},
                                                   {RC, RCNB, RCNP, RCNBP}, {RS, RSNB, RSNP, RSNBP}};
    private final RedditService redditService;
    private final Subreddit subreddit = Subreddit.SPACECHEM;

    @Qualifier("scArchiveRepository")
    private final GitRepository gitRepo;
    private final Class<ScCategory> categoryClass = ScCategory.class;
    private final Function<String[], ScSolution> solUnmarshaller = ScSolution::unmarshal;
    private final Comparator<ScSolution> archiveComparator =
            Comparator.comparing(ScSolution::getScore, ScCategory.C.getScoreComparator()
                                                                   .thenComparing(ScScore::isBugged)
                                                                   .thenComparing(ScScore::isPrecognitive));
    private final List<ScPuzzle> trackedPuzzles = Arrays.stream(ScPuzzle.values()).filter(p -> p.getType() != ScType.BOSS_RANDOM).toList();

    @NotNull
    @Override
    public SubmitResult<ScRecord, ScCategory> submit(@NotNull ScSubmission submission) {
        try (GitRepository.ReadWriteAccess access = gitRepo.acquireWriteAccess()) {
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

    @Override
    protected @NotNull String wikiPageName(ScPuzzle puzzle) {
        return puzzle.getGroup().getWikiPage();
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
    protected void updateRedditLeaderboard(@NotNull List<String> lines, @NotNull ScPuzzle puzzle, Path puzzlePath,
                                           @NotNull List<ScSolution> solutions) {

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

        Pattern puzzleRegex = Pattern.compile("^\\| \\[" + Pattern.quote(puzzle.getDisplayName()) + "(?: - |])");

        int rowIdx = 0;

        // | [Puzzle](https://zlbb) | [(**ccc**/r/ss) author](https://li.nk) | ← | [(ccc/r/**ss**) author](https://li.nk) | ←
        // | [Puzzle - 1 Reactor](https://zlbb) | [(**ccc**/**r**/ss) author](https://li.nk) | ← | [(ccc/**r**/**ss**) author](https://li.nk) | ←
        for (int lineIdx = 0; lineIdx < lines.size(); lineIdx++) {
            String line = lines.get(lineIdx);
            if (puzzleRegex.matcher(line).find()) {
                String[] prevElems = line.trim().split("\\s*\\|\\s*", -1);
                int halfSize = (prevElems.length - 2) / 2;

                StringBuilder row = new StringBuilder("| ");
                int minReactors;
                String text;
                String link;
                if (rowIdx == 0) {
                    minReactors = Integer.MAX_VALUE;
                    text = puzzle.getDisplayName();
                    link = puzzle.getLink();
                }
                else {
                    minReactors = recordMap.get(ScCategory.RC).getScore().getReactors();
                    text = puzzle.getDisplayName() + " - " + minReactors + " Reactor" + (minReactors == 1 ? "" : "s");

                    int maxReactorsShown = recordMap.get(ScCategory.RCNB).getScore().getReactors();
                    String filter = String.format("visualizerFilterSc-%s.range.r.max=%d", puzzle.name(), maxReactorsShown);
                    link = puzzle.getLink() + "?visualizerConfigSc.mode=2D&" + filter;
                }
                row.append(Markdown.link(text, link));

                for (int block = 0; block < 2; block++) {
                    ScCategory[] blockCategories = wikiCategories[2 * rowIdx + block];
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
                lines.set(lineIdx, row.toString());

                rowIdx++;
            }
            else if (rowIdx != 0) {
                // we've already found the point and now we're past it, we're done
                break;
            }
        }
    }

    @NotNull
    private static String makeLeaderboardCell(ScRecord @NotNull [] blockRecords, int i, int minReactors,
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
    protected ScSolution makeCandidateSolution(@NotNull ScSubmission submission) {
        return new ScSolution(submission.getScore(), submission.getAuthor(), submission.getDisplayLink(), false);
    }

    @Override
    protected int frontierCompare(@NotNull ScScore s1, @NotNull ScScore s2) {
        int r1 = Integer.compare(s1.getCycles(), s2.getCycles());
        int r2 = Integer.compare(s1.getReactors(), s2.getReactors());
        int r3 = Integer.compare(s1.getSymbols(), s2.getSymbols());
        int r4 = Boolean.compare(s1.isBugged(), s2.isBugged());
        int r5 = Boolean.compare(s1.isPrecognitive(), s2.isPrecognitive());

        if (r1 <= 0 && r2 <= 0 && r3 <= 0 && r4 <= 0 && r5 <= 0) {
            // s1 dominates
            return -1;
        }
        else if (r1 >= 0 && r2 >= 0 && r3 >= 0 && r4 >= 0 && r5 >= 0) {
            // s2 dominates
            return 1;
        }
        else {
            // equal is already captured by the 1st check, this is for "not comparable"
            return 0;
        }
    }

    @Override
    protected boolean alreadyPresent(@NotNull ScSolution candidate, @NotNull ScSolution solution) {
        return candidate.getScore().equals(solution.getScore()) &&
               candidate.getDisplayLink() == null &&
               !(candidate.getAuthor().equals(solution.getAuthor()) && solution.getDisplayLink() == null);
    }

    @Override
    protected void removeOrReplaceFromIndex(@NotNull ScSolution candidate, @NotNull ScSolution solution,
                                            @NotNull ListIterator<ScSolution> it) {
        if (candidate.getDisplayLink() == null && solution.getDisplayLink() != null) {
            // we beat the solution, but we can't replace the video, we keep the solution entry as a video-only
            it.set(solution.withVideoOnly(true)); // empty categories
        }
        else {
            it.remove();
        }
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
}
