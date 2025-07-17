/*
 * Copyright (c) 2025
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

package com.faendir.zachtronics.bot.tis.repository;

import com.faendir.zachtronics.bot.git.GitRepository;
import com.faendir.zachtronics.bot.model.DisplayContext;
import com.faendir.zachtronics.bot.reddit.RedditService;
import com.faendir.zachtronics.bot.reddit.Subreddit;
import com.faendir.zachtronics.bot.repository.AbstractSolutionRepository;
import com.faendir.zachtronics.bot.tis.model.*;
import com.faendir.zachtronics.bot.utils.Markdown;
import com.google.errorprone.annotations.CheckReturnValue;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.faendir.zachtronics.bot.tis.model.TISCategory.*;
import static java.util.stream.Collectors.*;

@Component
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public class TISSolutionRepository extends AbstractSolutionRepository<TISCategory, TISPuzzle, TISScore, TISSubmission, TISRecord, TISSolution> {
    private final TISCategory[][] wikiCategories = {{CN, CI, CX}, {NC, NI, NX}, {IC, IN, IX}};
    private final RedditService redditService;
    private final Subreddit subreddit = Subreddit.TIS100;

    @Qualifier("tisRepository")
    private final GitRepository gitRepo;
    private final Class<TISCategory> categoryClass = TISCategory.class;
    private final Function<String[], TISSolution> solUnmarshaller = TISSolution::unmarshal;
    private final Comparator<TISSolution> archiveComparator =
        Comparator.comparing(TISSolution::getScore, CN.getScoreComparator()
                                                      .thenComparing(s -> !s.isAchievement())
                                                      .thenComparing(TISScore::isCheating));
    @Getter(AccessLevel.PUBLIC) @VisibleForTesting
    private final List<TISPuzzle> trackedPuzzles = Arrays.stream(TISPuzzle.values()).filter(p -> p.getType() != TISType.SANDBOX).toList();

    @Override
    protected @NotNull String wikiPageName(TISPuzzle puzzle) {
        return "index";
    }

    @Override
    protected TISSolution makeCandidateSolution(@NotNull TISSubmission submission) {
        return new TISSolution(submission.getScore(), submission.getAuthor(), submission.getDisplayLink());
    }

    @Override
    protected int frontierCompare(@NotNull TISScore s1, @NotNull TISScore s2) {
        int r1 = Integer.compare(s1.getCycles(), s2.getCycles());
        int r2 = Integer.compare(s1.getNodes(), s2.getNodes());
        int r3 = Integer.compare(s1.getInstructions(), s2.getInstructions());
        int r4 = Boolean.compare(!s1.isAchievement(), !s2.isAchievement());
        int r5 = Boolean.compare(s1.isCheating(), s2.isCheating());
        int r6 = Boolean.compare(s1.isHardcoded(), s2.isHardcoded());

        if (r1 <= 0 && r2 <= 0 && r3 <= 0 && r4 <= 0 && r5 <= 0 && r6 <= 0) {
            // s1 dominates
            return -1;
        }
        else if (r1 >= 0 && r2 >= 0 && r3 >= 0 && r4 >= 0 && r5 >= 0 && r6 >= 0) {
            // s2 dominates
            return 1;
        }
        else {
            // equal is already captured by the 1st check, this is for "not comparable"
            return 0;
        }
    }

    @Override
    protected boolean allowedSameScoreUpdate(@NotNull TISSolution candidate, @NotNull TISSolution solution) {
        return candidate.getDisplayLink() != null ||
               (candidate.getAuthor().equals(solution.getAuthor()) && solution.getDisplayLink() == null);
    }

    @Override
    protected void updateRedditLeaderboard(@NotNull List<String> lines, @NotNull TISPuzzle puzzle,
                                           GitRepository.@NotNull ReadWriteAccess access, @NotNull List<TISSolution> solutions) {
        // we need to update data that relies on the whole solution list (like totals), don't even try a single update
        lines.clear();
        try {
            lines.addAll(rebuildRedditPage(wikiPageName(null), access));
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    protected @NotNull List<String> rebuildRedditPage(@NotNull String page, GitRepository.ReadWriteAccess access) throws IOException {
        final String anchorPoint = "# TIS-100 SEGMENT MAP";
        List<String> lines = readRedditWiki(page).stream()
                                                 .takeWhile(l -> !l.equals(anchorPoint))
                                                 .collect(toList());
        ListIterator<String> it = lines.listIterator(lines.size());

        Consumer<String> addPuzzleTableHeader =
            head -> {
                it.add(String.format("# %s", head));
                it.add("");
                it.add("| Puzzle | Cycles | Nodes | Instructions");
                it.add("| --- | --- | --- | --- | ---");
            };

        List<TISSolution> allSolutions = new ArrayList<>();
        Map<TISPuzzle, Map<TISCategory, TISRecord>> data = new EnumMap<>(TISPuzzle.class);
        for (TISPuzzle puzzle : trackedPuzzles) {
            Path puzzlePath = getPuzzlePath(access, puzzle);
            List<TISSolution> solutions = unmarshalSolutions(puzzlePath);
            allSolutions.addAll(solutions);

            Map<TISCategory, TISRecord> recordMap = new EnumMap<>(TISCategory.class);
            for (TISSolution solution : solutions) {
                TISRecord record = solution.extendToRecord(puzzle,
                                                   makeArchiveLink(puzzle, solution.getScore()),
                                                   makeArchivePath(puzzlePath, solution.getScore()));
                for (TISCategory category : solution.getCategories()) {
                    recordMap.put(category, record);
                }
            }
            data.put(puzzle, recordMap);
        }

        for (TISGroup group : TISGroup.values()) {
            int totalCycles = 0;
            int totalNodes = 0;
            int totalInstructions = 0;
            addPuzzleTableHeader.accept(group.getDisplayName());
            Iterable<TISPuzzle> puzzles = trackedPuzzles.stream().filter(p -> p.getGroup() == group)::iterator;
            for (TISPuzzle puzzle : puzzles) {
                Map<TISCategory, TISRecord> recordMap = data.get(puzzle);

                addPuzzleLines(it, puzzle, wikiCategories, recordMap, Markdown.link(puzzle.getDisplayName(), puzzle.getLink()));
                it.add("|");

                if (recordMap.containsKey(wikiCategories[0][0])) {
                    totalCycles += recordMap.get(wikiCategories[0][0]).getScore().getCycles();
                    totalNodes += recordMap.get(wikiCategories[1][0]).getScore().getNodes();
                    totalInstructions += recordMap.get(wikiCategories[2][0]).getScore().getInstructions();
                }
            }
            it.add(String.format("| **Totals** | **%d** | **%d** | **%d**", totalCycles, totalNodes, totalInstructions));
            it.add("");
        }

        {
            int totalCycles = 0;
            int totalNodes = 0;
            int totalInstructions = 0;
            addPuzzleTableHeader.accept("Achievement Solutions");
            Iterable<TISPuzzle> achievPuzzles = trackedPuzzles.stream().filter(p -> p.getAchievement() != null)::iterator;
            TISCategory[][] achievCategories = {{aCN, aCI, aCX, acCN, acCI, acCX},
                                                {aNC, aNI, aNX, acNC, acNI, acNX},
                                                {aIC, aIN, aIX, acIC, acIN, acIX}};
            for (TISPuzzle puzzle : achievPuzzles) {
                Map<TISCategory, TISRecord> recordMap = data.get(puzzle);

                String link = puzzle.getLink() + "?visualizerFilterTIS-" + puzzle.getId().replace('.', '-') + ".modifiers.achievement=true";
                String puzzleHeader = Markdown.link(puzzle.getDisplayName(), link) + " (" + puzzle.getAchievement() + ")";
                addPuzzleLines(it, puzzle, achievCategories, recordMap, puzzleHeader);
                it.add("|");

                if (recordMap.containsKey(achievCategories[0][0])) {
                    totalCycles += recordMap.get(achievCategories[0][0]).getScore().getCycles();
                    totalNodes += recordMap.get(achievCategories[1][0]).getScore().getNodes();
                    totalInstructions += recordMap.get(achievCategories[2][0]).getScore().getInstructions();
                }
            }
            it.add(String.format("| **Totals** | **%d** | **%d** | **%d**", totalCycles, totalNodes, totalInstructions));
            it.add("");
        }

        {
            int totalCycles = 0;
            int totalNodes = 0;
            int totalInstructions = 0;
            addPuzzleTableHeader.accept("Cheating Solutions");
            TISCategory[][] cheatCategories = {{hCN, hCI, hCX, cCN, cCI, cCX},
                                               {hNC, hNI, hNX, cNC, cNI, cNX},
                                               {hIC, hIN, hIX, cIC, cIN, cIX}};
            for (TISPuzzle puzzle : trackedPuzzles) {
                // copy to edit
                Map<TISCategory, TISRecord> recordMap = new EnumMap<>(data.get(puzzle));
                recordMap.values().removeIf(r -> !r.getScore().isCheating() || r.getScore().isAchievement());
                if (recordMap.isEmpty())
                    continue; // there is no cheating solve at all

                String link = puzzle.getLink() + "?visualizerFilterTIS-" + puzzle.getId().replace('.', '-') + ".modifiers.cheating=true";
                String puzzleHeader = Markdown.link(puzzle.getDisplayName(), link);
                addPuzzleLines(it, puzzle, cheatCategories, recordMap, puzzleHeader);
                it.add("|");

                if (recordMap.containsKey(cheatCategories[0][0]))
                    totalCycles += recordMap.get(cheatCategories[0][0]).getScore().getCycles();
                if (recordMap.containsKey(cheatCategories[1][0]))
                    totalNodes += recordMap.get(cheatCategories[1][0]).getScore().getNodes();
                if (recordMap.containsKey(cheatCategories[2][0]))
                    totalInstructions += recordMap.get(cheatCategories[2][0]).getScore().getInstructions();
            }
            it.add(String.format("| **Totals** | **%d** | **%d** | **%d**", totalCycles, totalNodes, totalInstructions));
            it.add("");
        }

        Consumer<String> addLbTableHeader =
            head -> {
                it.add(String.format("# %s", head));
                it.add("");
                it.add("| Solutions | Name(s)");
                it.add("| --- | --- ");
            };

        addLbTableHeader.accept("Most record solutions");
        metaLeaderboardStream(allSolutions, s -> !s.getCategories().isEmpty()).forEach(it::add);
        it.add("");
        addLbTableHeader.accept("Most frontier solutions");
        metaLeaderboardStream(allSolutions, s -> true).forEach(it::add);

        return lines;
    }

    private static final DecimalFormat format = new DecimalFormat("0.##", new DecimalFormatSymbols(Locale.ENGLISH));
    @CheckReturnValue
    private static Stream<String> metaLeaderboardStream(@NotNull List<TISSolution> allSolutions, Predicate<TISSolution> filterSol) {
        Map<String, Double> authorToAmount = new HashMap<>();
        for (TISSolution solution: allSolutions) {
            if (filterSol.test(solution)) {
                String[] authors;
                if (solution.getAuthor().contains("/")) {
                    authors = solution.getAuthor().split("/");
                }
                else {
                    authors = new String[]{solution.getAuthor()};
                }
                double part = 1.0 / authors.length;
                for (String author : authors)
                    authorToAmount.merge(author, part, Double::sum);
            }
        }
        return authorToAmount.entrySet()
                             .stream()
                             .collect(groupingBy(Map.Entry::getValue, mapping(Map.Entry::getKey, toList())))
                             .entrySet()
                             .stream()
                             .sorted(Collections.reverseOrder(Map.Entry.comparingByKey()))
                             .map(e -> "| " + format.format(e.getKey()) + " | " + e.getValue()
                                                                                   .stream()
                                                                                   .sorted(String.CASE_INSENSITIVE_ORDER)
                                                                                   .map(Markdown::escape)
                                                                                   .collect(joining(", ")));
    }

    @Override
    @NotNull
    protected Path relativePuzzlePath(@NotNull TISPuzzle puzzle) {
        return Paths.get(puzzle.getGroup().name()).resolve(puzzle.getId());
    }

    @NotNull
    static String makeFilename(@NotNull String puzzleId, @NotNull TISScore score) {
        return puzzleId + "." + score.toDisplayString(DisplayContext.fileName()) + ".txt";
    }

    @NotNull
    @Override
    protected String makeArchiveLink(@NotNull TISPuzzle puzzle, @NotNull TISScore score) {
        return makeArchiveLink(puzzle, makeFilename(puzzle.getId(), score));
    }

    @Override
    @NotNull
    protected Path makeArchivePath(@NotNull Path puzzlePath, TISScore score) {
        return puzzlePath.resolve(makeFilename(puzzlePath.getFileName().toString(), score));
    }
}
