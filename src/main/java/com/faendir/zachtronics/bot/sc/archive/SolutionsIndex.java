package com.faendir.zachtronics.bot.sc.archive;

import com.faendir.zachtronics.bot.sc.model.ScCategory;
import com.faendir.zachtronics.bot.sc.model.ScScore;
import com.faendir.zachtronics.bot.sc.model.ScSolution;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *  We use this to (de)serialize the index that is in each level folder and keep track of the export files.<br>
 *  The index is a list sorted in CRS order of BPScores
 */
class SolutionsIndex {
    private static final Comparator<ScScore> COMPARATOR = ScCategory.C.getScoreComparator()
                                                                      .thenComparing(ScScore::isBugged)
                                                                      .thenComparing(ScScore::isPrecognitive);
    private final Path puzzlePath;
    private final List<ScScore> scores;

    SolutionsIndex(Path puzzlePath) throws IOException {
        this.puzzlePath = puzzlePath;
        try (Stream<String> lines = Files.lines(puzzlePath.resolve("solutions.txt"))) {
            scores = lines.map(ScScore::parseBPScore).collect(Collectors.toList());
        }
    }

    /**
     * @return list of displaced scores
     */
    List<String> add(ScSolution solution) throws IOException {
        List<String> displacedScores = new ArrayList<>();
        ScScore candidate = solution.getScore();
        ListIterator<ScScore> it = scores.listIterator();
        while (it.hasNext()) {
            ScScore score = it.next();
            int r = dominanceCompare(candidate, score);
            if (r > 0)
                return Collections.emptyList();
            else if (r < 0) {
                // remove beaten score
                displacedScores.add(score.toDisplayString());
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
        Files.write(puzzlePath.resolve("solutions.txt"), lines, StandardOpenOption.TRUNCATE_EXISTING);

        if (solution.getContent() != null) {
            Path solutionPath = puzzlePath.resolve(makeScoreFilename(candidate));
            Files.write(solutionPath, solution.getContent().getBytes(), StandardOpenOption.CREATE_NEW);
        }

        if (displacedScores.isEmpty()) // solution is in the frontier but doesn't outright beat any, we have to fill a placeholder
            displacedScores.add("-");
        return displacedScores;
    }

    @NotNull
    private static String makeScoreFilename(ScScore score) {
        return score.toDisplayString().replace('/', '-') + ".txt";
    }

    /** If equal, s1 dominates */
    private static int dominanceCompare(ScScore s1, ScScore s2) {
        int r1 = Integer.compare(s1.getCycles(), s2.getCycles());
        int r2 = Integer.compare(s1.getReactors(), s2.getReactors());
        int r3 = Integer.compare(s1.getSymbols(), s2.getReactors());
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
