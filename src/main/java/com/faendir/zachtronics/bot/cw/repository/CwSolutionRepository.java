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

package com.faendir.zachtronics.bot.cw.repository;

import com.faendir.zachtronics.bot.cw.model.*;
import com.faendir.zachtronics.bot.git.GitRepository;
import com.faendir.zachtronics.bot.model.DisplayContext;
import com.faendir.zachtronics.bot.reddit.RedditService;
import com.faendir.zachtronics.bot.reddit.Subreddit;
import com.faendir.zachtronics.bot.repository.AbstractSolutionRepository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import static com.faendir.zachtronics.bot.cw.model.CwCategory.FOOTPRINT;
import static com.faendir.zachtronics.bot.cw.model.CwCategory.SIZE;

@Component
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public class CwSolutionRepository extends AbstractSolutionRepository<CwCategory, CwPuzzle, CwScore, CwSubmission, CwRecord, CwSolution> {
    private final CwCategory[][] wikiCategories = {{SIZE}, {FOOTPRINT}};
    private final RedditService redditService;
    private final Subreddit subreddit = Subreddit.LASTCALLBBS;

    @Qualifier("cwRepository")
    private final GitRepository gitRepo;
    private final Class<CwCategory> categoryClass = CwCategory.class;
    private final Function<String[], CwSolution> solUnmarshaller = CwSolution::unmarshal;
    private final Comparator<CwSolution> archiveComparator = Comparator.comparing(CwSolution::getScore, SIZE.getScoreComparator());
    private final List<CwPuzzle> trackedPuzzles = List.of(CwPuzzle.values());

    @Override
    @NotNull
    protected String wikiPageName(CwPuzzle puzzle) {
        return "chipwizard";
    }

    @Override
    protected CwSolution makeCandidateSolution(@NotNull CwSubmission submission) {
        return new CwSolution(submission.getScore(), submission.getAuthor(), submission.getDisplayLink());
    }

    @Override
    protected int frontierCompare(@NotNull CwScore s1, @NotNull CwScore s2) {
        int r1 = Integer.compare(s1.getWidth(), s2.getWidth());
        int r2 = Integer.compare(s1.getHeight(), s2.getHeight());
        int r3 = Integer.compare(s1.getFootprint(), s2.getFootprint());

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

    @Override
    protected boolean allowedSameScoreUpdate(@NotNull CwSolution candidate, @NotNull CwSolution solution) {
        return candidate.getDisplayLink() != null ||
               (candidate.getAuthor().equals(solution.getAuthor()) && solution.getDisplayLink() == null);
    }

    @Override
    @NotNull
    protected Path relativePuzzlePath(@NotNull CwPuzzle puzzle) {
        return Paths.get(puzzle.getGroup().name()).resolve(puzzle.name());
    }

    @NotNull
    static String makeScoreFilename(@NotNull CwScore score) {
        return score.toDisplayString(DisplayContext.fileName()) + ".txt";
    }

    @NotNull
    @Override
    protected String makeArchiveLink(@NotNull CwPuzzle puzzle, @NotNull CwScore score) {
        return makeArchiveLink(puzzle, makeScoreFilename(score));
    }

    @Override
    @NotNull
    protected Path makeArchivePath(@NotNull Path puzzlePath, CwScore score) {
        return puzzlePath.resolve(makeScoreFilename(score));
    }
}
