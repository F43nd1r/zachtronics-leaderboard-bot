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

package com.faendir.zachtronics.bot.sz.repository;

import com.faendir.zachtronics.bot.git.GitRepository;
import com.faendir.zachtronics.bot.repository.AbstractSolutionRepository;
import com.faendir.zachtronics.bot.repository.CategoryRecord;
import com.faendir.zachtronics.bot.repository.SubmitResult;
import com.faendir.zachtronics.bot.sz.model.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Component
@RequiredArgsConstructor
public class SzSolutionRepository extends AbstractSolutionRepository<SzCategory, SzPuzzle, SzSubmission, SzRecord> {
    private static final Pattern NAME_REGEX = Pattern.compile(
            "top solution (?:cost|power|lines)(?:->(?:cost|power|lines))?(?: - (?<author>.+))?",
            Pattern.CASE_INSENSITIVE);

    //TODO Reddit
    @Getter(AccessLevel.PROTECTED)
    @Qualifier("szRepository")
    private final GitRepository gitRepo;

    @NotNull
    @Override
    public SubmitResult<SzRecord, SzCategory> submit(@NotNull SzSubmission submission) {
        try (GitRepository.ReadWriteAccess access = getGitRepo().acquireWriteAccess()) {
            SubmitResult<SzRecord, SzCategory> r = performArchive(access, submission);
            access.push();
            return r;
        }
    }

    @Nullable
    @Override
    public SzRecord find(@NotNull SzPuzzle puzzle, @NotNull SzCategory category) {
        try (GitRepository.ReadAccess access = gitRepo.acquireReadAccess()) {
            Path filePath = findPuzzleFile(access.getRepo().toPath(), puzzle, category);
            return readSolutionFile(filePath);
        }
    }

    @NotNull
    @Override
    public List<CategoryRecord<SzRecord, SzCategory>> findCategoryHolders(@NotNull SzPuzzle puzzle,
                                                                          boolean includeFrontier) { // FIXME
        try (GitRepository.ReadAccess access = gitRepo.acquireReadAccess()) {
            Path repoPath = access.getRepo().toPath();
            // TODO
            Map<SzRecord, Set<SzCategory>> categoryRecords = Arrays.stream(SzCategory.values()).collect(
                    Collectors.groupingBy(c -> readSolutionFile(findPuzzleFile(repoPath, puzzle, c)),
                                          Collectors.mapping(c -> c, Collectors.toSet())));
            return reshapeCategoryRecordMap(categoryRecords, CategoryRecord::new);
        }
    }

    @NotNull
    private SzRecord readSolutionFile(Path solutionFile) {
        SzSubmission submission = new SzSubmission(solutionFile); // FIXME
        Matcher m = NAME_REGEX.matcher(submission.getTitle());
        if (!m.matches())
            throw new IllegalStateException("Name does not match standard format: " + m.replaceFirst(""));
        String author = m.group("author");
        String link = makeArchiveLink(submission.getPuzzle(), solutionFile.getFileName().toString());
        return new SzRecord(submission.getPuzzle(), submission.getScore(), author, link, solutionFile);
    }

    private Path findPuzzleFile(@NotNull Path repoPath, @NotNull SzPuzzle puzzle, @NotNull SzCategory category) {
        Path puzzleFolder = repoPath.resolve(relativePuzzlePath(puzzle));
        Path puzzleFile = puzzleFolder.resolve(puzzle.getId() + "-" + category.getRepoSuffix() + ".txt");
        if (!Files.exists(puzzleFile)) {
            // we're missing the X02 subcategory, we just have a X01 file
            puzzleFile = puzzleFolder.resolve(puzzle.getId() + "-" + (category.getRepoSuffix() - 1) + ".txt");
        }
        return puzzleFile;
    }

    @Override
    @NotNull
    protected Path relativePuzzlePath(@NotNull SzPuzzle puzzle) {
        return Paths.get(puzzle.getGroup().getRepoFolder());
    }

    @Override
    protected SolutionsIndex<SzSubmission> makeSolutionIndex(@NotNull Path puzzlePath,
                                                             @NotNull SzPuzzle puzzle)
            throws IOException {
        return new SzSolutionsIndex(puzzlePath, puzzle);
    }

    @Override
    public String makeArchiveLink(@NotNull SzPuzzle puzzle, @NotNull String filename) {
        return String.format("%s/%s/%s", gitRepo.getRawFilesUrl(), puzzle.getGroup().getRepoFolder(), filename);
    }

    /**
     *  We use this to keep track of the solutions to a given level in the repo.
     */
    static class SzSolutionsIndex implements SolutionsIndex<SzSubmission> {

        private final Path folderPath;
        @Getter
        private final SzPuzzle puzzle;
        /** <tt>{1: [sols C], 2: [sols P], 3: [sols L]}</tt> */
        private final Map<Integer, List<SzSubmission>> diskSolutions;
        private static final Map<Integer, Comparator<SzSubmission>> COMPARATOR_MAP = Map
                .of(1, makeComparator(SzCategory.CP),
                    2, makeComparator(SzCategory.PC),
                    3, makeComparator(SzCategory.LC));

        private static Comparator<SzSubmission> makeComparator(@NotNull SzCategory category) {
            return Comparator.comparing(SzSubmission::getScore, category.getScoreComparator());
        }

        SzSolutionsIndex(Path folderPath, @NotNull SzPuzzle puzzle) throws IOException {
            this.folderPath = folderPath;
            this.puzzle = puzzle;
            diskSolutions = Files.list(folderPath)
                                 .filter(p -> p.getFileName().toString().startsWith(this.puzzle.getId()))
                                 .collect(groupingBy(p -> Character.getNumericValue(
                                                             p.getFileName().toString()
                                                              .charAt(this.puzzle.getId().length() + 1)),
                                                     Collectors.mapping(SzSubmission::new, toList())));

        }

        @Override
        public boolean add(@NotNull SzSubmission solution) throws IOException {
            boolean updated = false;
            SzScore candidate = solution.getScore();
            categoryLoop:
            for (Map.Entry<Integer, List<SzSubmission>> entry : diskSolutions.entrySet()) {
                List<SzSubmission> categorySolutions = entry.getValue();
                ListIterator<SzSubmission> it = categorySolutions.listIterator();
                while (it.hasNext()) {
                    SzSubmission solutionDisk = it.next();
                    SzScore score = solutionDisk.getScore();
                    int r = dominanceCompare(candidate, score);
                    if (r > 0)
                        continue categoryLoop;
                    else if (r < 0) {
                        // remove beaten score
                        it.remove();
                        assert solutionDisk.getPath() != null;
                        Files.delete(solutionDisk.getPath());
                    }
                }

                Integer category = entry.getKey();
                int index = Collections.binarySearch(categorySolutions, solution, COMPARATOR_MAP.get(category));
                if (index < 0) {
                    index = -index - 1;
                }
                categorySolutions.add(index, solution);

                for (int i = 0; i < categorySolutions.size(); i++) {
                    Path newPath = folderPath.resolve(String.format("%s-%d%02d.txt", puzzle.getId(), category, i + 1));
                    Path oldPath = categorySolutions.get(i).getPath();
                    if (!newPath.equals(oldPath)) {
                        if (oldPath != null) { // an old sol
                            Files.move(oldPath, newPath);
                            categorySolutions.get(i).setPath(newPath);
                        }
                        else { // it's our new sol
                            Files.write(newPath, solution.getData().getBytes(), StandardOpenOption.CREATE_NEW);
                        }
                    }
                }
                updated = true;
            }

            return updated;
        }

        @Deprecated
        public List<SzRecord> findAll() {
            return diskSolutions.values()
                                .stream()
                                .flatMap(List::stream)
                                .map(SzSubmission::toRecord)
                                .toList();
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
}
