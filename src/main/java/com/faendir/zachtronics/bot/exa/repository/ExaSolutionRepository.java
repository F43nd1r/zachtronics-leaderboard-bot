/*
 * Copyright (c) 2026
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
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

import static com.faendir.zachtronics.bot.exa.model.ExaCategory.*;
import static com.faendir.zachtronics.bot.exa.model.ExaMetric.*;

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
    private final List<Comparator<ExaScore>> frontierComparators = List.of(CYCLES, SIZE, ACTIVITY, CHEESY);
    private final List<ExaPuzzle> trackedPuzzles = Arrays.stream(ExaPuzzle.values()).filter(p -> p.getType() != ExaType.SANDBOX).toList();

    @Override
    protected String wikiPageName(@Nullable ExaPuzzle puzzle) {
        return "index";
    }

    @Override
    protected ExaSolution makeCandidateSolution(ExaSubmission submission) {
        return new ExaSolution(submission.getScore(), submission.getAuthor(), submission.getDisplayLink());
    }

    @Override
    protected void updateRedditLeaderboard(List<String> lines, ExaPuzzle puzzle,
                                           GitRepository.ReadWriteAccess access, List<ExaSolution> solutions) {
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

    protected void updateRedditLeaderboard(List<String> lines, ExaPuzzle puzzle,
                                           GitRepository.ReadWriteAccess access, List<ExaSolution> solutions,
                                           boolean doCheese) {
        super.updateRedditLeaderboard(lines, puzzle, access, solutions);

        if (doCheese && solutions.stream().anyMatch(s -> s.getScore().isCheesy())) {
            try {
                rebuildCheeseTable(lines, access);
            }
            catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private void rebuildCheeseTable(List<String> lines, GitRepository.ReadWriteAccess access) throws IOException {
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
    protected Path relativePuzzlePath(ExaPuzzle puzzle) {
        return Path.of(puzzle.getGroup().name()).resolve(puzzle.name());
    }

    static String makeFilename(ExaPuzzle puzzle, ExaScore score) {
        return puzzle.getPrefix() + "-" + score.toDisplayString(DisplayContext.fileName()) + ".solution";
    }

    @Override
    protected String makeArchiveLink(ExaPuzzle puzzle, ExaScore score) {
        return makeArchiveLink(puzzle, makeFilename(puzzle, score));
    }

    @Override
    protected Path makeArchivePath(Path puzzlePath, ExaScore score) {
        ExaPuzzle puzzle = ExaPuzzle.valueOf(puzzlePath.getFileName().toString());
        return puzzlePath.resolve(makeFilename(puzzle, score));
    }
}
