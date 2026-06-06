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
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
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
    private final List<Comparator<CwScore>> frontierComparators = List.of(CwMetric.SIZE, CwMetric.FOOTPRINT, CwMetric.WIDTH);
    private final List<CwPuzzle> trackedPuzzles = List.of(CwPuzzle.values());

    @Override
    protected String wikiPageName(@Nullable CwPuzzle puzzle) {
        return "chipwizard";
    }

    @Override
    protected CwSolution makeCandidateSolution(CwSubmission submission) {
        return new CwSolution(submission.getScore(), submission.getAuthor(), submission.getDisplayLink());
    }

    @Override
    protected Path relativePuzzlePath(CwPuzzle puzzle) {
        return Path.of(puzzle.getGroup().name()).resolve(puzzle.name());
    }

    static String makeScoreFilename(CwScore score) {
        return score.toDisplayString(DisplayContext.fileName()) + ".txt";
    }

    @Override
    protected String makeArchiveLink(CwPuzzle puzzle, CwScore score) {
        return makeArchiveLink(puzzle, makeScoreFilename(score));
    }

    @Override
    protected Path makeArchivePath(Path puzzlePath, CwScore score) {
        return puzzlePath.resolve(makeScoreFilename(score));
    }
}
