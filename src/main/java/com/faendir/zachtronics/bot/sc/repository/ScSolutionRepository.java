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
import com.opencsv.*;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.faendir.zachtronics.bot.sc.model.ScCategory.*;

@Component
@RequiredArgsConstructor
public class ScSolutionRepository extends AbstractSolutionRepository<ScCategory, ScPuzzle, ScSubmission, ScRecord> {
    private static final ScCategory[][] CATEGORIES = {{C,  CNB,  CNP,  CNBP},  {S,  SNB,  SNP, SNBP},
                                                      {RC, RCNB, RCNP, RCNBP}, {RS, RSNB, RSNP, RSNBP}};

    private final RedditService redditService;
    @Getter(AccessLevel.PROTECTED)
    @Qualifier("scArchiveRepository")
    private final GitRepository gitRepo;

    @NotNull
    @Override
    public SubmitResult<ScRecord, ScCategory> submit(@NotNull ScSubmission submission) {
        try (GitRepository.ReadWriteAccess access = getGitRepo().acquireWriteAccess()) {
            SubmitResult<ScRecord, ScCategory> r = submitOne(access, submission);
            if (r instanceof SubmitResult.Success<ScRecord, ScCategory>)
                access.push();
            return r;
        }
    }

    @NotNull
    @Override
    public List<SubmitResult<ScRecord, ScCategory>> submitAll(
            @NotNull Collection<? extends ValidationResult<ScSubmission>> validationResults) {
        try (GitRepository.ReadWriteAccess access = gitRepo.acquireWriteAccess()) {
            List<SubmitResult<ScRecord, ScCategory>> l = validationResults.stream().map(r -> {
                if (r instanceof ValidationResult.Valid<ScSubmission>)
                    return submitOne(access, r.getSubmission());
                else
                    return new SubmitResult.Failure<ScRecord, ScCategory>(r.getMessage());
            }).toList();
            if (l.stream().anyMatch(s -> s instanceof SubmitResult.Success<ScRecord, ScCategory>))
                access.push();
            return l;
        }
    }

    @Nullable
    @Override
    public ScRecord find(@NotNull ScPuzzle puzzle, @NotNull ScCategory category) {
        return findCategoryHolders(puzzle, false).stream()
                                                 .filter(cr -> cr.getCategories().contains(category))
                                                 .findFirst()
                                                 .map(CategoryRecord::getRecord)
                                                 .orElse(null);
    }

    @NotNull
    @Override
    public List<CategoryRecord<ScRecord, ScCategory>> findCategoryHolders(@NotNull ScPuzzle puzzle, boolean includeFrontier) {
        try (GitRepository.ReadAccess access = getGitRepo().acquireReadAccess()) {
            Path puzzlePath = access.getRepo().toPath().resolve(relativePuzzlePath(puzzle));

            List<ScSolution> solutions = unmarshalSolutions(puzzlePath);

            List<CategoryRecord<ScRecord, ScCategory>> result = new ArrayList<>();
            for (ScSolution sol : solutions) {
                if (includeFrontier || !sol.getCategories().isEmpty()) {
                    CategoryRecord<ScRecord, ScCategory> categoryRecord =
                            sol.extendToCategoryRecord(puzzle,
                                                       makeArchiveLink(puzzle, sol.getScore()),
                                                       puzzlePath.resolve(makeScoreFilename(sol.getScore())));
                    result.add(categoryRecord);
                }
            }
            return result;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @NotNull
    private SubmitResult<ScRecord, ScCategory> submitOne(@NotNull GitRepository.ReadWriteAccess access,
                                                         @NotNull ScSubmission submission) {
        ScPuzzle puzzle = submission.getPuzzle();
        Path puzzlePath = access.getRepo().toPath().resolve(relativePuzzlePath(puzzle));
        List<ScSolution> solutions;
        try {
            solutions = unmarshalSolutions(puzzlePath);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        SubmitResult<ScRecord, ScCategory> submitResult = archiveOne(access, solutions, submission);

        if (submitResult instanceof SubmitResult.Success<ScRecord, ScCategory>) {
            ScSolution submissionSolution = solutions.stream()
                                                     .filter(s -> s.getScore().equals(submission.getScore()))
                                                     .findFirst()
                                                     .orElseThrow();
            Set<ScCategory> wonCategories = submissionSolution.getCategories();
            if (!wonCategories.isEmpty()) {
                // write the reddit lb, as there are changes to write
                String updateMessage = puzzle.getDisplayName() + " " + submission.getScore().toDisplayString() +
                                       " by " + submission.getAuthor();
                writeToRedditLeaderboard(puzzle, puzzlePath, solutions, updateMessage);
                // post to lb thread
                postAnnouncementToReddit(submission, makeArchiveLink(puzzle, submission.getScore()), wonCategories);
            }
        }

        return submitResult;
    }

    public void rebuildRedditLeaderboard(ScPuzzle puzzle, String updateMessage) {
        try (GitRepository.ReadWriteAccess access = getGitRepo().acquireWriteAccess()) {
            Path puzzlePath = access.getRepo().toPath().resolve(relativePuzzlePath(puzzle));
            List<ScSolution> solutions = unmarshalSolutions(puzzlePath);
            writeToRedditLeaderboard(puzzle, puzzlePath, solutions, updateMessage);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    private void writeToRedditLeaderboard(@NotNull ScPuzzle puzzle, Path puzzlePath, @NotNull List<ScSolution> solutions,
                                         String updateMessage) {

        List<ScRecord> records = solutions.stream()
                                          .map(sol -> sol.extendToRecord(puzzle,
                                                                         makeArchiveLink(puzzle, sol.getScore()),
                                                                         puzzlePath.resolve(makeScoreFilename(sol.getScore()))))
                                          .toList();
        Map<ScCategory, Map.Entry<ScRecord, ScRecord>> recordMap = new EnumMap<>(ScCategory.class);
        for (int i = 0; i < solutions.size(); i++) {
            ScSolution solution = solutions.get(i);
            ScRecord record = records.get(i);
            for (ScCategory category : solution.getCategories()) {
                ScRecord videoRecord;
                if (record.getDisplayLink() != null)
                    videoRecord = record;
                else {
                    videoRecord = records.stream()
                                         .filter(r -> r.getDisplayLink() != null)
                                         .filter(r -> category.supportsScore(r.getScore()))
                                         .min(Comparator.comparing(ScRecord::getScore, category.getScoreComparator()))
                                         .orElseThrow();
                }
                recordMap.put(category, Map.entry(record, videoRecord));
            }
        }

        String[] lines = redditService.getWikiPage(Subreddit.SPACECHEM, puzzle.getGroup().getWikiPage()).split("\\r?\\n");
        Pattern puzzleRegex = Pattern.compile("^\\s*\\|\\s*" + Pattern.quote(puzzle.getDisplayName()));

        int rowIdx = 0;

        // |Puzzle | [(**ccc**/r/ss) author](https://li.nk) | ← | [(ccc/r/**ss**) author](https://li.nk) | ←
        // |Puzzle - 1 Reactor | [(**ccc**/**r**/ss) author](https://li.nk) | ← | [(ccc/**r**/**ss**) author](https://li.nk) | ←
        for (int lineIdx = 0; lineIdx < lines.length; lineIdx++) {
            String line = lines[lineIdx];
            if (puzzleRegex.matcher(line).find()) {
                String[] prevElems = line.trim().split("\\s*\\|\\s*");
                int halfSize = (prevElems.length - 2) / 2;

                StringBuilder row = new StringBuilder("|");
                int minReactors = Integer.MAX_VALUE;
                if (rowIdx == 1) {
                    minReactors = recordMap.values().stream().mapToInt(r -> r.getKey().getScore().getReactors()).min().orElseThrow();
                    row.append(puzzle.getDisplayName()).append(" - ")
                       .append(minReactors).append(" Reactor").append(minReactors == 1 ? "" : "s");
                }
                else {
                    row.append(prevElems[1]);
                }

                Map.Entry<ScRecord, ScRecord> impossible = Map.entry(ScRecord.IMPOSSIBLE_CATEGORY, ScRecord.IMPOSSIBLE_CATEGORY);
                for (int block = 0; block < 2; block++) {
                    ScCategory[] blockCategories = CATEGORIES[2 * rowIdx + block];
                    @SuppressWarnings("unchecked")
                    Map.Entry<ScRecord, ScRecord>[] blockRecords =
                            (Map.Entry<ScRecord, ScRecord>[]) Arrays.stream(blockCategories)
                                                                    .map(c -> recordMap.getOrDefault(c, impossible))
                                                                    .toArray(Map.Entry[]::new);

                    for (int i = 0; i < halfSize; i++) {
                        ScCategory thisCategory = blockCategories[i];
                        row.append(" | ");
                        if (blockRecords[i].getKey() != ScRecord.IMPOSSIBLE_CATEGORY) {
                            row.append(makeLeaderboardCell(blockRecords, i, minReactors, thisCategory));
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
    private static String makeLeaderboardCell(@NotNull Map.Entry<ScRecord, ScRecord>[] blockRecords, int i, int minReactors,
                                              ScCategory thisCategory) {
        Map.Entry<ScRecord, ScRecord> recordPair = blockRecords[i];
        for (int prev = 0; prev < i; prev++) {
            if (recordPair.equals(blockRecords[prev])) {
                return "←".repeat(i - prev);
            }
        }

        ScRecord record = recordPair.getKey();
        String reactorPrefix = (record.getScore().getReactors() > minReactors) ? "† " : "";
        String cell = record.toDisplayString(new DisplayContext<>(StringFormat.REDDIT, thisCategory), reactorPrefix);

        if (record.getDisplayLink() == null) { // show the best video we have
            cell += ". Best video: " + recordPair.getValue().toDisplayString(new DisplayContext<>(StringFormat.REDDIT, thisCategory));
        }
        return cell;
    }

    private void postAnnouncementToReddit(@NotNull ScSubmission submission, String dataLink, Collection<ScCategory> categories) {
        // see: https://www.reddit.com/r/spacechem/comments/mmcuzb
        DisplayContext<ScCategory> context = new DisplayContext<>(StringFormat.REDDIT, categories);
        redditService.postInSubmission("mmcuzb", "Added " + Markdown.fileLinkOrEmpty(dataLink) +
                                                 "[" + submission.getPuzzle().getDisplayName() +
                                                 " (" + submission.getScore().toDisplayString(context) +
                                                 ")](" + submission.getDisplayLink() +
                                                 ") by " + submission.getAuthor());
    }

    @Override
    @NotNull
    protected Path relativePuzzlePath(@NotNull ScPuzzle puzzle) {
        return Paths.get(puzzle.getGroup().name(), puzzle.name());
    }

    @NotNull
    private static String makeScoreFilename(@NotNull ScScore score) {
        return score.toDisplayString(DisplayContext.fileName()) + ".txt";
    }

    @NotNull
    Path makeArchivePath(@NotNull ScPuzzle puzzle, @NotNull ScScore score) {
        return makeArchivePath(puzzle, makeScoreFilename(score));
    }

    String makeArchiveLink(@NotNull ScPuzzle puzzle, @NotNull ScScore score) {
        return makeArchiveLink(puzzle, makeScoreFilename(score));
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
    @NotNull
    protected SubmitResult<ScRecord, ScCategory> archiveOne(@NotNull GitRepository.ReadWriteAccess access,
                                                            @NotNull List<ScSolution> solutions,
                                                            @NotNull ScSubmission submission) {
        ScPuzzle puzzle = submission.getPuzzle();
        Path puzzlePath = access.getRepo().toPath().resolve(relativePuzzlePath(puzzle));

        List<CategoryRecord<ScRecord, ScCategory>> beatenCategoryRecords = new ArrayList<>();
        ScSolution candidate = new ScSolution(submission.getScore(), submission.getAuthor(), submission.getDisplayLink(), false);

        try {
            for (ListIterator<ScSolution> it = solutions.listIterator(); it.hasNext(); ) {
                ScSolution solution = it.next();
                int r = dominanceCompare(candidate, solution);
                if (r > 0) {
                    // TODO actually return all of the beating sols
                    CategoryRecord<ScRecord, ScCategory> categoryRecord =
                            solution.extendToCategoryRecord(puzzle,
                                                            makeArchiveLink(puzzle, solution.getScore()),
                                                            puzzlePath.resolve(makeScoreFilename(solution.getScore())));
                    return new SubmitResult.NothingBeaten<>(Collections.singletonList(categoryRecord));
                }
                else if (r < 0) {
                    // allow same-score author/video changes if you are the original author or you bring a video
                    if (candidate.getScore().equals(solution.getScore()) &&
                        !candidate.getAuthor().equals(solution.getAuthor()) &&
                        candidate.getDisplayLink() == null) {
                        return new SubmitResult.AlreadyPresent<>();
                    }
                    // remove beaten score and get categories
                    it.remove();
                    Files.delete(puzzlePath.resolve(makeScoreFilename(solution.getScore())));
                    candidate.getCategories().addAll(solution.getCategories());
                    beatenCategoryRecords.add(solution.extendToCategoryRecord(puzzle, null, null)); // the beaten record has no data anymore
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
                                                    puzzlePath.resolve(makeScoreFilename(solution.getScore()))),
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

        return new SubmitResult.Success<>(result, beatenCategoryRecords);
    }

    /**
     * @return a mutable list
     */
    private static List<ScSolution> unmarshalSolutions(@NotNull Path puzzlePath) throws IOException {
        Path indexPath = puzzlePath.resolve("solutions.psv");
        try (BufferedReader reader = Files.newBufferedReader(indexPath)) {

            CSVParser parser = new CSVParserBuilder().withSeparator('|').withFieldAsNull(CSVReaderNullFieldIndicator.BOTH).build();
            CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(parser).build();
            return StreamSupport.stream(csvReader.spliterator(), false)
                                .map(ScSolution::unmarshal)
                                .collect(Collectors.toList());
        }
        catch (NoSuchFileException e) {
            Files.createDirectories(puzzlePath);
            Files.createFile(indexPath);
            return new ArrayList<>();
        }
    }

    private static void marshalSolutions(@NotNull List<ScSolution> solutions, @NotNull Path puzzlePath) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(puzzlePath.resolve("solutions.psv"),
                                                             StandardOpenOption.TRUNCATE_EXISTING)) {
            ICSVWriter csvWriter = new CSVWriterBuilder(writer).withSeparator('|').build();
            csvWriter.writeAll(solutions.stream().map(ScSolution::marshal)::iterator, false);
        }
    }

    /**
     * If equal, sol1 dominates
     */
    private static int dominanceCompare(@NotNull ScSolution sol1, @NotNull ScSolution sol2) {
        ScScore s1 = sol1.getScore();
        ScScore s2 = sol2.getScore();
        int r1 = Integer.compare(s1.getCycles(), s2.getCycles());
        int r2 = Integer.compare(s1.getReactors(), s2.getReactors());
        int r3 = Integer.compare(s1.getSymbols(), s2.getSymbols());
        int r4 = Boolean.compare(s1.isBugged(), s2.isBugged());
        int r5 = Boolean.compare(s1.isPrecognitive(), s2.isPrecognitive());
        int r6 = Boolean.compare(sol1.getDisplayLink() == null, sol2.getDisplayLink() == null);

        if (r1 <= 0 && r2 <= 0 && r3 <= 0 && r4 <= 0 && r5 <= 0 && r6 <= 0) {
            // sol1 dominates
            return -1;
        }
        else if (r1 >= 0 && r2 >= 0 && r3 >= 0 && r4 >= 0 && r5 >= 0 && r6 >= 0) {
            // sol2 dominates
            return 1;
        }
        else {
            // equal is already captured by the 1st check, this is for "not comparable"
            return 0;
        }
    }

}
