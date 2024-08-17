/*
 * Copyright (c) 2024
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

package com.faendir.zachtronics.bot.exa.repository;

import com.faendir.zachtronics.bot.exa.model.*;
import com.faendir.zachtronics.bot.git.GitRepository;
import com.faendir.zachtronics.bot.model.DisplayContext;
import com.faendir.zachtronics.bot.reddit.RedditService;
import com.faendir.zachtronics.bot.reddit.Subreddit;
import com.faendir.zachtronics.bot.repository.AbstractSolutionRepository;
import com.faendir.zachtronics.bot.utils.Markdown;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;

import static com.faendir.zachtronics.bot.exa.model.ExaCategory.*;

@Component
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public class ExaSolutionRepository extends AbstractSolutionRepository<ExaCategory, ExaPuzzle, ExaScore, ExaSubmission, ExaRecord, ExaSolution> {
    private final ExaCategory[][] wikiCategories = {{CS, CA}, {SC, SA}, {AC, AS}};
    private final RedditService redditService;
    private final Subreddit subreddit = Subreddit.EXAPUNKS;

    @Qualifier("exaRepository")
    private final GitRepository gitRepo;
    private final Class<ExaCategory> categoryClass = ExaCategory.class;
    private final Function<String[], ExaSolution> solUnmarshaller = ExaSolution::unmarshal;
    private final Comparator<ExaSolution> archiveComparator = Comparator.comparing(ExaSolution::getScore, ExaCategory.CS.getScoreComparator());
    private final List<ExaPuzzle> trackedPuzzles = List.of(ExaPuzzle.values());

    @Override
    protected @NotNull String wikiPageName(ExaPuzzle puzzle) {
        return "index";
    }

    @Override
    protected ExaSolution makeCandidateSolution(@NotNull ExaSubmission submission) {
        return new ExaSolution(submission.getScore(), submission.getAuthor(), submission.getDisplayLink());
    }

    @Override
    protected int frontierCompare(@NotNull ExaScore s1, @NotNull ExaScore s2) {
        int r1 = Integer.compare(s1.getCycles(), s2.getCycles());
        int r2 = Integer.compare(s1.getSize(), s2.getSize());
        int r3 = Integer.compare(s1.getActivity(), s2.getActivity());
        int r4 = Boolean.compare(s1.isCheesy(), s2.isCheesy());
        if (r1 <= 0 && r2 <= 0 && r3 <= 0 && r4 <= 0) {
            // s1 dominates
            return -1;
        }
        else if (r1 >= 0 && r2 >= 0 && r3 >= 0 && r4 >= 0) {
            // s2 dominates
            return 1;
        }
        else {
            // equal is already captured by the 1st check, this is for "not comparable"
            return 0;
        }
    }

    /** allow same-score solution changes only if you are the original author */
    @Override
    protected boolean alreadyPresent(@NotNull ExaSolution candidate, @NotNull ExaSolution solution) {
        return candidate.getScore().equals(solution.getScore()) &&
               candidate.getDisplayLink() == null &&
               !(candidate.getAuthor().equals(solution.getAuthor()) && solution.getDisplayLink() == null);
    }

    @Override
    protected void updateRedditLeaderboard(@NotNull List<String> lines, @NotNull ExaPuzzle puzzle,
                                           GitRepository.@NotNull ReadWriteAccess access, @NotNull List<ExaSolution> solutions) {
        updateRedditLeaderboard(lines, puzzle, access, solutions, true);
    }
    
    @Override
    protected List<String> rebuildRedditPage(String page, GitRepository.ReadWriteAccess access) throws IOException {
        List<String> lines = readRedditWiki(page);
        for (ExaPuzzle puzzle : trackedPuzzles) {
            Path puzzlePath = getPuzzlePath(access, puzzle);
            List<ExaSolution> solutions = unmarshalSolutions(puzzlePath);
            updateRedditLeaderboard(lines, puzzle, access, solutions, false);
        }
        rebuildCheeseTable(lines, access);
        return lines;
    }

    protected void updateRedditLeaderboard(@NotNull List<String> lines, @NotNull ExaPuzzle puzzle,
                                           GitRepository.@NotNull ReadWriteAccess access, @NotNull List<ExaSolution> solutions,
                                           boolean doCheese) {
        super.updateRedditLeaderboard(lines, puzzle, access, solutions);

        if (doCheese && solutions.stream().noneMatch(s -> s.getScore().isCheesy())) {
            try {
                rebuildCheeseTable(lines, access);
            }
            catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private void rebuildCheeseTable(@NotNull List<String> lines, GitRepository.ReadWriteAccess access) throws IOException {
        final String anchorPoint = "### Cheesy solutions";
        lines.subList(lines.indexOf(anchorPoint) + 4, lines.size()).clear();
        ListIterator<String> it = lines.listIterator(lines.size());

        ExaCategory[][] cheesyCategories = {{cCS, cCA}, {cSC, cSA}, {cAC, cAS}};
        for (ExaPuzzle puzzle : trackedPuzzles) {
            Path puzzlePath = getPuzzlePath(access, puzzle);
            List<ExaSolution> solutions = unmarshalSolutions(puzzlePath);
            solutions.removeIf(s -> !s.getScore().isCheesy());

            if (solutions.isEmpty())
                continue; // there is no cheesy solve at all

            Map<ExaCategory, ExaRecord> recordMap = new EnumMap<>(ExaCategory.class);
            for (ExaSolution solution : solutions) {
                ExaRecord record = solution.extendToRecord(puzzle,
                                                           makeArchiveLink(puzzle, solution.getScore()),
                                                           makeArchivePath(puzzlePath, solution.getScore()));
                for (ExaCategory category : solution.getCategories()) {
                    recordMap.put(category, record);
                }
            }

            String link = puzzle.getLink() + "?visualizerFilterExa-" + puzzle.name() + ".modifiers.cheesy=true";
            String puzzleHeader = Markdown.link(puzzle.getDisplayName(), link);
            addPuzzleLines(it, puzzle, cheesyCategories, recordMap, puzzleHeader);
            it.add("|");
        }
    }

    @Override
    @NotNull
    protected Path relativePuzzlePath(@NotNull ExaPuzzle puzzle) {
        return Paths.get(puzzle.getGroup().name()).resolve(puzzle.name());
    }

    @NotNull
    static String makeFilename(@NotNull ExaPuzzle puzzle, @NotNull ExaScore score) {
        return puzzle.getPrefix() + "-" + score.toDisplayString(DisplayContext.fileName()) + ".solution";
    }

    @NotNull
    @Override
    protected String makeArchiveLink(@NotNull ExaPuzzle puzzle, @NotNull ExaScore score) {
        return makeArchiveLink(puzzle, makeFilename(puzzle, score));
    }

    @Override
    @NotNull
    protected Path makeArchivePath(@NotNull Path puzzlePath, ExaScore score) {
        ExaPuzzle puzzle = ExaPuzzle.valueOf(puzzlePath.getFileName().toString());
        return puzzlePath.resolve(makeFilename(puzzle, score));
    }
}
