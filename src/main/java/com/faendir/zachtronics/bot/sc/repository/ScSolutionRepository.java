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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.faendir.zachtronics.bot.sc.model.ScCategory.*;

@Component
@RequiredArgsConstructor
public class ScSolutionRepository extends AbstractSolutionRepository<ScCategory, ScPuzzle, ScSubmission, ScRecord> {
    private static final ScCategory[][] CATEGORIES = {{C,  CNB,  CNP},  {S,  SNB,  SNP},
                                                      {RC, RCNB, RCNP}, {RS, RSNB, RSNP}};

    private final RedditService redditService;
    @Getter(AccessLevel.PROTECTED)
    @Qualifier("scArchiveRepository")
    private final GitRepository gitRepo;

    @NotNull
    @Override
    public SubmitResult<ScRecord, ScCategory> submit(@NotNull ScSubmission submission) {
        try (GitRepository.ReadWriteAccess access = getGitRepo().acquireWriteAccess()) {
            // get the map before we change it by archiving
            Map<ScScore, Map.Entry<ScRecord.ScRecordBuilder, Set<ScCategory>>> oldRbcMap =
                    getRbcMap(submission.getPuzzle(), access.getRepo().toPath());
            SubmitResult<ScRecord, ScCategory> archiveResult = performArchive(access, submission);
            access.push();
            if ((archiveResult instanceof SubmitResult.Success ||
                 archiveResult instanceof SubmitResult.AlreadyPresent) && submission.getDisplayLink() != null) {
                // we try the reddit LB only if the record made the archive, as the former is a superset
                ScRecord submissionRecord = submission.extendToRecord(
                        makeArchiveLink(submission.getPuzzle(), submission.getScore()),
                        makeArchivePath(submission.getPuzzle(), submission.getScore()));
                SubmitResult<ScRecord, ScCategory> lbResult = submitToRedditLeaderboard(submissionRecord, oldRbcMap);
                if (archiveResult instanceof SubmitResult.Success<ScRecord, ScCategory> archiveSuccess &&
                    lbResult instanceof SubmitResult.Success<ScRecord, ScCategory> lbSuccess) {
                    // we use the archive's message
                    return new SubmitResult.Success<>(archiveSuccess.getMessage(), lbSuccess.getBeatenRecords());
                }
                else
                    return lbResult;
            }
            else
                return archiveResult;
        }
    }

    @NotNull
    @Override
    public List<SubmitResult<ScRecord, ScCategory>> submitAll(@NotNull Collection<? extends ScSubmission> submissions) {
        try (GitRepository.ReadWriteAccess access = gitRepo.acquireWriteAccess()) {
            List<SubmitResult<ScRecord, ScCategory>> r = submissions.stream().map(s -> performArchive(access, s)).toList();
            access.push();
            return r;
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
    public List<CategoryRecord<ScRecord, ScCategory>> findCategoryHolders(@NotNull ScPuzzle puzzle,
                                                                          boolean includeFrontier) {
        try (GitRepository.ReadAccess access = getGitRepo().acquireReadAccess()) {
            Map<ScScore, Map.Entry<ScRecord.ScRecordBuilder, Set<ScCategory>>> rbcMap =
                    getRbcMap(puzzle, access.getRepo().toPath());

            String[] lines = redditService.getWikiPage(Subreddit.SPACECHEM, puzzle.getGroup().getWikiPage())
                                          .split("\\r?\\n");
            Pattern puzzleRegex = Pattern.compile("^\\s*\\|\\s*" + Pattern.quote(puzzle.getDisplayName()));

            int seenRows = 0;
            for (String line : lines) {
                if (puzzleRegex.matcher(line).find()) {
                    String[] pieces1 = line.trim().split("\\s*\\|\\s*");
                    List<String> tableCols = Arrays.asList(pieces1).subList(2, pieces1.length);

                    List<String> cyclesHalf1 = tableCols.subList(0, tableCols.size() / 2);
                    parseHalfTable(rbcMap, cyclesHalf1, CATEGORIES[2 * seenRows]);

                    List<String> symbolsHalf = tableCols.subList(tableCols.size() / 2, tableCols.size());
                    parseHalfTable(rbcMap, symbolsHalf, CATEGORIES[2 * seenRows + 1]);
                    seenRows++;
                }
                else if (seenRows != 0) {
                    // we've already found the point and now we're past it, we're done
                    break;
                }
            }

            // TODO collapse with reshapeCategoryRecordMap(rbcMap, CategoryRecord::new);
            return rbcMap.values().stream()
                                  .filter(entry -> includeFrontier || !entry.getValue().isEmpty())
                                  .map(entry -> new CategoryRecord<>(entry.getKey().build(), entry.getValue()))
                                  .toList();
        }
    }

    @NotNull
    private SortedMap<ScScore, Map.Entry<ScRecord.ScRecordBuilder, Set<ScCategory>>> getRbcMap(
            @NotNull ScPuzzle puzzle, @NotNull Path repoPath) {
        Path puzzlePath = repoPath.resolve(relativePuzzlePath(puzzle));
        ScSolutionsIndex solutionsIndex;
        try {
            solutionsIndex = makeSolutionIndex(puzzlePath, puzzle);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        // TODO put categories in archive, so we can filter sooner and do the author right
        SortedMap<ScScore, Map.Entry<ScRecord.ScRecordBuilder, Set<ScCategory>>> rbcMap = new TreeMap<>(
                ScSolutionsIndex.COMPARATOR);
        for (ScScore score : solutionsIndex.getScores()) {
            ScRecord.ScRecordBuilder builder = ScRecord.builder().puzzle(puzzle).score(score);
            String filename = makeScoreFilename(score);
            Path path = puzzlePath.resolve(filename);
            if (Files.exists(path)) {
                builder.dataLink(makeArchiveLink(puzzle, filename)).dataPath(path);
            }
            rbcMap.put(score, Map.entry(builder, EnumSet.noneOf(ScCategory.class)));
        }
        return rbcMap;
    }

    @NotNull
    private SubmitResult<ScRecord, ScCategory> submitToRedditLeaderboard(@NotNull ScRecord submissionRecord,
                                                   Map<ScScore, Map.Entry<ScRecord.ScRecordBuilder, Set<ScCategory>>> oldRbcMap) {
        assert submissionRecord.getAuthor() != null;
        assert submissionRecord.getDisplayLink() != null;

        ScPuzzle puzzle = submissionRecord.getPuzzle();
        String[] lines = redditService.getWikiPage(Subreddit.SPACECHEM, puzzle.getGroup().getWikiPage()).split("\\r?\\n");
        Pattern puzzleRegex = Pattern.compile("^\\s*\\|\\s*" + Pattern.quote(puzzle.getDisplayName()));

        int startingRow = -1;
        int seenRows = 0;
        int halfSize = -1;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (puzzleRegex.matcher(line).find()) {
                String[] pieces = line.trim().split("\\s*\\|\\s*");
                List<String> tableCols = Arrays.asList(pieces).subList(2, pieces.length);

                List<String> cyclesHalf = tableCols.subList(0, tableCols.size() / 2);
                parseHalfTable(oldRbcMap, cyclesHalf, CATEGORIES[2 * seenRows]);
                List<String> symbolsHalf = tableCols.subList(tableCols.size() / 2, tableCols.size());
                parseHalfTable(oldRbcMap, symbolsHalf, CATEGORIES[2 * seenRows + 1]);

                if (seenRows == 0) {
                    startingRow = i;
                    halfSize = tableCols.size() / 2;
                }
                seenRows++;
            }
            else if (seenRows != 0) {
                // we've already found the point and now we're past it, we're done
                break;
            }
        }

        Map<ScRecord, Set<ScCategory>> beatenRecords = new HashMap<>();
        Map<ScRecord, Set<ScCategory>> categoryRecords = new HashMap<>();
        // |Puzzle | [(**ccc**/r/ss) author](https://li.nk) | ← | [(ccc/r/**ss**) author](https://li.nk) | ←
        // |Puzzle - 1 Reactor | [(**ccc**/**r**/ss) author](https://li.nk) | ← | [(ccc/**r**/**ss**) author](https://li.nk) | ←
        for (int rowIdx = 0; rowIdx < seenRows; rowIdx++) {
            String[] prevElems = lines[startingRow + rowIdx].trim().split("\\s*\\|\\s*");
            StringBuilder row = new StringBuilder("|");
            int minReactors = Integer.MAX_VALUE;
            if (rowIdx == 1) {
                minReactors = Math.min(submissionRecord.getScore().getReactors(),
                                       oldRbcMap.keySet().stream().mapToInt(ScScore::getReactors).min().orElseThrow());
                row.append(puzzle.getDisplayName()).append(" - ").append(minReactors).append(" Reactor")
                   .append(minReactors == 1 ? "" : "s");
            }
            else {
                row.append(prevElems[1]);
            }

            Map<ScCategory, ScRecord> recordMap = new EnumMap<>(ScCategory.class);
            for (Map.Entry<ScRecord.ScRecordBuilder, Set<ScCategory>> rbc : oldRbcMap.values()) {
                ScRecord record = rbc.getKey().build();
                for (ScCategory category : rbc.getValue()) {
                    recordMap.put(category, record);
                }
            }

            for (int block = 0; block < 2; block++) {
                ScCategory[] blockCategories = CATEGORIES[2 * rowIdx + block];
                ScRecord[] blockRecords = Arrays.stream(blockCategories)
                                                .map(c -> recordMap.getOrDefault(c, ScRecord.IMPOSSIBLE_CATEGORY))
                                                .toArray(ScRecord[]::new);

                for (int i = 0; i < halfSize; i++) {
                    ScCategory thisCategory = blockCategories[i];
                    row.append(" | ");
                    int comparison = thisCategory.getScoreComparator()
                                                 .compare(submissionRecord.getScore(), blockRecords[i].getScore());
                    if (thisCategory.supportsScore(submissionRecord.getScore()) && comparison <= 0) {
                        if (comparison == 0 && submissionRecord.getAuthor().equals(blockRecords[i].getAuthor()) &&
                            submissionRecord.getDisplayLink().equals(blockRecords[i].getDisplayLink()))
                            return new SubmitResult.AlreadyPresent<>();
                        addRC(beatenRecords, blockRecords[i], thisCategory);
                        blockRecords[i] = submissionRecord;

                        row.append(makeLeaderboardCell(blockRecords, i, minReactors, thisCategory));
                    }
                    else {
                        String prevElem = prevElems[block * halfSize + i + 2];
                        if (beatenRecords.containsKey(blockRecords[i]) && prevElem.matches("←+")) {
                            // "dangling" reference to beaten score, we need to write the actual score or change the pointer
                            row.append(makeLeaderboardCell(blockRecords, i, minReactors, thisCategory));
                        }
                        else {
                            if (blockRecords[i] != ScRecord.IMPOSSIBLE_CATEGORY &&
                                thisCategory.supportsScore(submissionRecord.getScore()))
                                addRC(categoryRecords, blockRecords[i], thisCategory);
                            row.append(prevElem);
                        }
                    }
                }
            }
            lines[startingRow + rowIdx] = row.toString();
        }

        if (!beatenRecords.isEmpty()) {
            redditService.updateWikiPage(Subreddit.SPACECHEM, puzzle.getGroup().getWikiPage(), String.join("\n", lines),
                                         puzzle.getDisplayName() + " " + submissionRecord.getScore().toDisplayString() + " by " +
                                         submissionRecord.getAuthor());
            return new SubmitResult.Success<>(null, reshapeCategoryRecordMap(beatenRecords, CategoryRecord::new));
        }
        else {
            return new SubmitResult.NothingBeaten<>(reshapeCategoryRecordMap(categoryRecords, CategoryRecord::new));
        }
    }

    private static void addRC(@NotNull Map<ScRecord, Set<ScCategory>> map, ScRecord record, ScCategory category) {
        map.computeIfAbsent(record, k -> EnumSet.noneOf(ScCategory.class)).add(category);
    }

    @NotNull
    private static String makeLeaderboardCell(@NotNull ScRecord[] blockRecords, int i, int minReactors,
                                              ScCategory thisCategory) {
        ScRecord record = blockRecords[i];
        for (int prev = 0; prev < i; prev++) {
            if (record == blockRecords[prev]) {
                return "←".repeat(i - prev);
            }
        }

        String reactorPrefix = (record.getScore().getReactors() > minReactors) ? "† " : "";
        return record.toDisplayString(new DisplayContext<>(StringFormat.REDDIT, thisCategory), reactorPrefix);
    }

    private static final Pattern REGEX_SCORE_CELL =
            Pattern.compile("\\[\uD83D\uDCC4]\\((?<dataLink>.+\\.txt)\\) " +
                            "(?:† )?" +
                            "\\[\\((?<score>" + ScScore.REGEX_BP_SCORE + ")\\) (?<author>[^]]+)]" +
                            "\\((?<link>[^)]+)\\).*?");

    /**
     * @return set of categories held for updating
     */
    @NotNull
    private static Set<ScCategory> parseLeaderboardRecord(
            Map<ScScore, Map.Entry<ScRecord.ScRecordBuilder, Set<ScCategory>>> rbcMap, String recordCell) {
        Matcher m = REGEX_SCORE_CELL.matcher(recordCell);
        if (m.matches()) {
            ScScore score = ScScore.parseBPScore(m);
            Map.Entry<ScRecord.ScRecordBuilder, Set<ScCategory>> entry = rbcMap.get(score);
            if (entry == null) {
                // we don't have this sol in the archive, which means it has been out-pareto'd but still not submitted
                ScPuzzle puzzle = rbcMap.values().iterator().next().getKey().build().getPuzzle();
                ScRecord.ScRecordBuilder builder = ScRecord.builder().puzzle(puzzle).score(score);
                entry = Map.entry(builder, EnumSet.noneOf(ScCategory.class));
                rbcMap.put(score, entry);
            }
            ScRecord.ScRecordBuilder builder = entry.getKey();
            builder.author(m.group("author")).displayLink(m.group("link")).oldVideoRNG(m.group("oldRNG") != null);
            return entry.getValue();
        }
        throw new IllegalStateException("Leaderboard record unparseable: " + recordCell);
    }

    private static void parseHalfTable(Map<ScScore, Map.Entry<ScRecord.ScRecordBuilder, Set<ScCategory>>> rbcMap,
                                       @NotNull List<String> halfTable, @NotNull ScCategory[] categories) {

        @SuppressWarnings("unchecked")
        Set<ScCategory>[] heldCategories = (Set<ScCategory>[]) new Set[3];
        heldCategories[0] = parseLeaderboardRecord(rbcMap, halfTable.get(0));
        heldCategories[0].add(categories[0]);

        if (halfTable.get(1).startsWith("X"))
            heldCategories[1] = EnumSet.noneOf(ScCategory.class); // impossible category
        else if (halfTable.get(1).equals("←"))
            heldCategories[1] = heldCategories[0];
        else
            heldCategories[1] = parseLeaderboardRecord(rbcMap, halfTable.get(1));
        heldCategories[1].add(categories[1]);

        if (halfTable.size() != 3 || halfTable.get(2).startsWith("X"))
            heldCategories[2] = EnumSet.noneOf(ScCategory.class); // impossible category
        else if (halfTable.get(2).equals("←←"))
            heldCategories[2] = heldCategories[0];
        else if (halfTable.get(2).equals("←"))
            heldCategories[2] = heldCategories[1];
        else
            heldCategories[2] = parseLeaderboardRecord(rbcMap, halfTable.get(2));
        heldCategories[2].add(categories[2]);
    }

    @Override
    protected ScSolutionsIndex makeSolutionIndex(@NotNull Path puzzlePath, @NotNull ScPuzzle puzzle)
            throws IOException {
        return new ScSolutionsIndex(puzzlePath, puzzle);
    }

    @NotNull
    @Override
    protected SubmitResult<ScRecord, ScCategory> performArchive(@NotNull GitRepository.ReadWriteAccess access,
                                                                @NotNull ScSubmission submission) {
        if (!submission.isValid())
            return new SubmitResult.Failure<>(submission.getData());
        return super.performArchive(access, submission);
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

    /**
     *  We use this to (de)serialize the index that is in each level folder and keep track of the export files.<br>
     *  The index is a list sorted in CRS order of BPScores
     */
    static class ScSolutionsIndex implements SolutionsIndex<ScSubmission> {
        private static final Comparator<ScScore> COMPARATOR = ScCategory.C.getScoreComparator()
                                                                          .thenComparing(ScScore::isBugged)
                                                                          .thenComparing(ScScore::isPrecognitive);
        private final ScPuzzle puzzle;
        private final Path puzzlePath;
        @Getter
        private final List<ScScore> scores;

        ScSolutionsIndex(@NotNull Path puzzlePath, @NotNull ScPuzzle puzzle) throws IOException {
            this.puzzlePath = puzzlePath;
            this.puzzle = puzzle;
            try (Stream<String> lines = Files.lines(puzzlePath.resolve("scores.txt"))) {
                scores = lines.map(ScScore::parseBPScore).collect(Collectors.toList());
            }
        }

        @Override
        public boolean add(@NotNull ScSubmission submission) throws IOException {
            ScScore candidate = submission.getScore();

            if (scores.contains(candidate)) {
                Path solutionPath = puzzlePath.resolve(makeScoreFilename(candidate));
                if (Files.exists(solutionPath)) {
                    // we allow file replacement only if the author is the same
                    String diskAuthor = ScSolutionMetadata.fromPath(solutionPath, puzzle).getAuthor();
                    if (submission.getAuthor().equals(diskAuthor)) {
                        Files.delete(solutionPath);
                    }
                    else {
                        // we won't change the files, but return true so that we'll realize we already had it
                        return true;
                    }
                }
            }
            else {
                ListIterator<ScScore> it = scores.listIterator();
                while (it.hasNext()) {
                    ScScore score = it.next();
                    int r = dominanceCompare(candidate, score);
                    if (r > 0)
                        return false;
                    else if (r < 0) {
                        // remove beaten score
                        it.remove();
                        Files.deleteIfExists(puzzlePath.resolve(makeScoreFilename(score)));
                    }
                }

                int index = Collections.binarySearch(scores, candidate, COMPARATOR);
                if (index < 0) {
                    index = -index - 1;
                }
                scores.add(index, candidate);

                Iterable<String> lines = scores.stream().map(ScScore::toDisplayString)::iterator;
                Files.write(puzzlePath.resolve("scores.txt"), lines, StandardOpenOption.TRUNCATE_EXISTING);
            }

            String filename = makeScoreFilename(candidate);
            Path solutionPath = puzzlePath.resolve(filename);
            Files.write(solutionPath, submission.getData().getBytes(), StandardOpenOption.CREATE_NEW);
            return true;
        }

        /**
         * If equal, s1 dominates
         */
        private static int dominanceCompare(@NotNull ScScore s1, @NotNull ScScore s2) {
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
    }
}
