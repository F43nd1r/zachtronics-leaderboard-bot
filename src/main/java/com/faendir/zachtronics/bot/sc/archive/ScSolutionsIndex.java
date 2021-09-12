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

package com.faendir.zachtronics.bot.sc.archive;

import com.faendir.zachtronics.bot.archive.SolutionsIndex;
import com.faendir.zachtronics.bot.sc.model.ScCategory;
import com.faendir.zachtronics.bot.sc.model.ScScore;
import com.faendir.zachtronics.bot.sc.model.ScSolution;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *  We use this to (de)serialize the index that is in each level folder and keep track of the export files.<br>
 *  The index is a list sorted in CRS order of BPScores
 */
class ScSolutionsIndex implements SolutionsIndex<ScSolution> {
    private static final Comparator<ScScore> COMPARATOR = ScCategory.C.getScoreComparator()
                                                                      .thenComparing(ScScore::isBugged)
                                                                      .thenComparing(ScScore::isPrecognitive);
    private final Path puzzlePath;
    private final List<ScScore> scores;

    ScSolutionsIndex(@NotNull Path puzzlePath) throws IOException {
        this.puzzlePath = puzzlePath;
        try (Stream<String> lines = Files.lines(puzzlePath.resolve("scores.txt"))) {
            scores = lines.map(ScScore::parseBPScore).collect(Collectors.toList());
        }
    }

    @Override
    public boolean add(@NotNull ScSolution solution) throws IOException {
        ScScore candidate = solution.getScore();

        if (scores.contains(candidate)) {
            Path solutionPath = puzzlePath.resolve(makeScoreFilename(candidate));
            if (Files.exists(solutionPath)) {
                // we allow file replacement only if the author is the same
                String diskHeader = Files.newBufferedReader(solutionPath).readLine();
                String diskAuthor = ScSolution.authorFromSolutionHeader(diskHeader);
                String candidateAuthor = ScSolution.authorFromSolutionHeader(solution.getContent());
                if (diskAuthor.equals(candidateAuthor)) {
                    Files.delete(solutionPath);
                }
                else {
                    return false;
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
        Files.write(solutionPath, solution.getContent().getBytes(), StandardOpenOption.CREATE_NEW);
        return true;
    }

    @NotNull
    private static String makeScoreFilename(@NotNull ScScore score) {
        return score.toDisplayString().replace('/', '-') + ".txt";
    }

    /** If equal, s1 dominates */
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
