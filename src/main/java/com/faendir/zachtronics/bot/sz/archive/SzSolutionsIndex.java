package com.faendir.zachtronics.bot.sz.archive;

import com.faendir.zachtronics.bot.generic.archive.SolutionsIndex;
import com.faendir.zachtronics.bot.sz.model.SzCategory;
import com.faendir.zachtronics.bot.sz.model.SzPuzzle;
import com.faendir.zachtronics.bot.sz.model.SzScore;
import com.faendir.zachtronics.bot.sz.model.SzSolution;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 *  We use this to keep track of the solutions to a given level in the repo.
 */
class SzSolutionsIndex implements SolutionsIndex<SzSolution> {

    private final Path folderPath;
    private final String puzzleName;
    /** <tt>{1: [sols C], 2: [sols P], 3: [sols L]}</tt> */
    private final Map<Integer, List<SzSolution>> diskSolutions;
    private static final Map<Integer, Comparator<SzSolution>> COMPARATOR_MAP = Map
            .of(1, makeComparator(SzCategory.CP),
                2, makeComparator(SzCategory.PC),
                3, makeComparator(SzCategory.LC));

    private static Comparator<SzSolution> makeComparator(@NotNull SzCategory category) {
        return Comparator.comparing(SzSolution::getScore, category.getScoreComparator());
    }

    SzSolutionsIndex(Path folderPath, @NotNull SzPuzzle puzzle) throws IOException {
        this.folderPath = folderPath;
        this.puzzleName = puzzle.name();
        diskSolutions = Files.list(folderPath)
                             .filter(p -> p.getFileName().toString().startsWith(puzzle.name()))
                             .collect(groupingBy(p -> Integer
                                                         .valueOf(p.getFileName().toString().replace(puzzle.name(), "").charAt(1)),
                                                 Collectors.mapping(SzSolution::new, toList())));

    }

    /**
     * @return list of displaced scores
     */
    public List<String> add(@NotNull SzSolution solution) throws IOException {
        List<String> displacedScores = new ArrayList<>();
        SzScore candidate = solution.getScore();
        categoryLoop:
        for (Map.Entry<Integer, List<SzSolution>> entry : diskSolutions.entrySet()) {
            List<SzSolution> categorySolutions = entry.getValue();
            ListIterator<SzSolution> it = categorySolutions.listIterator();
            while (it.hasNext()) {
                SzSolution solutionDisk = it.next();
                SzScore score = solutionDisk.getScore();
                int r = dominanceCompare(candidate, score);
                if (r > 0)
                    break categoryLoop;
                else if (r < 0) {
                    // remove beaten score
                    displacedScores.add(score.toDisplayString());
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
                Path newPath = folderPath.resolve(String.format("%s-%d%02d.txt", puzzleName, category, i + 1));
                Path oldPath = categorySolutions.get(i).getPath();
                if (!newPath.equals(oldPath)) {
                    if (oldPath != null) { // an old sol
                        Files.move(oldPath, newPath);
                    }
                    else { // it's our new sol
                        assert solution.getContent() != null;
                        Files.write(newPath, solution.getContent().getBytes(), StandardOpenOption.CREATE_NEW);
                    }
                    categorySolutions.get(i).setPath(newPath);
                }
            }

        }

        if (displacedScores.isEmpty()) // solution is in the frontier but doesn't outright beat any, we have to fill a placeholder
            displacedScores.add("-");
        return displacedScores;
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
