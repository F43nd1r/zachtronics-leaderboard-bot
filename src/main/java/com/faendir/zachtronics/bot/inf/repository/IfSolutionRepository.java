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
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import static com.faendir.zachtronics.bot.inf.model.IfCategory.*;
import static com.faendir.zachtronics.bot.inf.model.IfMetric.*;


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
    private final List<Comparator<IfScore>> frontierComparators = List.of(CYCLES, FOOTPRINT, BLOCKS,
                                                                          INBOUNDS, NO_GRA, INFINITE);
    private final List<IfPuzzle> trackedPuzzles = List.of(IfPuzzle.values());

    @Override
    protected String wikiPageName(@Nullable IfPuzzle puzzle) {
        return "index";
    }

    @Override
    protected IfSolution makeCandidateSolution(IfSubmission submission) {
        return new IfSolution(submission.getScore(), submission.getAuthor(), submission.getDisplayLinks());
    }

    @Override
    protected Path relativePuzzlePath(IfPuzzle puzzle) {
        return Path.of(puzzle.getGroup().name(), puzzle.getId());
    }

    static String makeScoreFilename(IfScore score) {
        return score.toDisplayString(DisplayContext.fileName()) + ".txt";
    }

    @Override
    protected String makeArchiveLink(IfPuzzle puzzle, IfScore score) {
        return makeArchiveLink(puzzle, makeScoreFilename(score));
    }

    @Override
    protected Path makeArchivePath(Path puzzlePath, IfScore score) {
        return puzzlePath.resolve(makeScoreFilename(score));
    }
}
