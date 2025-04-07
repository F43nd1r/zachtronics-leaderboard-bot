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

package com.faendir.zachtronics.bot.inf.repository;

import com.faendir.zachtronics.bot.git.GitRepository;
import com.faendir.zachtronics.bot.inf.model.*;
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

import static com.faendir.zachtronics.bot.inf.model.IfCategory.*;


@Component
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public class IfSolutionRepository extends AbstractSolutionRepository<IfCategory, IfPuzzle, IfScore, IfSubmission, IfRecord, IfSolution> {
    private final IfCategory[][] wikiCategories = {{CF, CB}, {CFNG, CBNG},
                                                   {FC, FB, FIC, FIB},
                                                   {BC, BF, BNC, BNF}};
    private final RedditService redditService;
    private final Subreddit subreddit = Subreddit.INFINIFACTORY;

    @Qualifier("ifRepository")
    private final GitRepository gitRepo;
    private final Class<IfCategory> categoryClass = IfCategory.class;
    private final Function<String[], IfSolution> solUnmarshaller = IfSolution::unmarshal;
    private final Comparator<IfSolution> archiveComparator =
        Comparator.comparing(IfSolution::getScore, Comparator.comparing(IfScore::getCycles)
                                                             .thenComparing(IfScore::getFootprint)
                                                             .thenComparing(IfScore::getBlocks)
                                                             .thenComparing(IfScore::isOutOfBounds)
                                                             .thenComparing(IfScore::usesGRA));
    private final List<IfPuzzle> trackedPuzzles = List.of(IfPuzzle.values());

    @Override
    protected @NotNull String wikiPageName(IfPuzzle puzzle) {
        return "index";
    }

    @Override
    protected IfSolution makeCandidateSolution(@NotNull IfSubmission submission) {
        return new IfSolution(submission.getScore(), submission.getAuthor(), submission.getDisplayLinks());
    }

    @Override
    protected int frontierCompare(@NotNull IfScore s1, @NotNull IfScore s2) {
        int r1 = Integer.compare(s1.getCycles(), s2.getCycles());
        int r2 = Integer.compare(s1.getFootprint(), s2.getFootprint());
        int r3 = Integer.compare(s1.getBlocks(), s2.getBlocks());
        int r4 = Boolean.compare(s1.isOutOfBounds(), s2.isOutOfBounds());
        int r5 = Boolean.compare(s1.usesGRA(), s2.usesGRA());
        int r6 = Boolean.compare(s1.isFinite(), s2.isFinite());
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
    protected boolean allowedSameScoreUpdate(@NotNull IfSolution candidate, @NotNull IfSolution solution) {
        return !candidate.getDisplayLinks().isEmpty() ||
               (candidate.getAuthor().equals(solution.getAuthor()) && solution.getDisplayLinks().isEmpty());
    }

    @Override
    @NotNull
    protected Path relativePuzzlePath(@NotNull IfPuzzle puzzle) {
        return Paths.get(puzzle.getGroup().name(), puzzle.getId());
    }

    @NotNull
    static String makeScoreFilename(@NotNull IfScore score) {
        return score.toDisplayString(DisplayContext.fileName()) + ".txt";
    }

    @NotNull
    @Override
    protected String makeArchiveLink(@NotNull IfPuzzle puzzle, @NotNull IfScore score) {
        return makeArchiveLink(puzzle, makeScoreFilename(score));
    }

    @Override
    @NotNull
    protected Path makeArchivePath(@NotNull Path puzzlePath, IfScore score) {
        return puzzlePath.resolve(makeScoreFilename(score));
    }
}
