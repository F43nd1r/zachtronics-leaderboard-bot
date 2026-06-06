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

package com.faendir.zachtronics.bot.fc.repository;

import com.faendir.zachtronics.bot.fc.model.*;
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

import static com.faendir.zachtronics.bot.fc.model.FcCategory.*;
import static com.faendir.zachtronics.bot.fc.model.FcMetric.*;

@Component
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public class FcSolutionRepository extends AbstractSolutionRepository<FcCategory, FcPuzzle, FcScore, FcSubmission, FcRecord, FcSolution> {
    private final FcCategory[][] wikiCategories = {{TCS, TSW, TWC},
                                                   {CTS, CSW, CWT},
                                                   {STC, SCW, SWT},
                                                   {WTC, WCS, WST}};
    private final RedditService redditService;
    private final Subreddit subreddit = Subreddit.LASTCALLBBS;

    @Qualifier("fcRepository")
    private final GitRepository gitRepo;
    private final Class<FcCategory> categoryClass = FcCategory.class;
    private final Function<String[], FcSolution> solUnmarshaller = FcSolution::unmarshal;
    private final List<Comparator<FcScore>> frontierComparators = List.of(TIME, COST, SUM_TIMES, WIRES);
    private final List<FcPuzzle> trackedPuzzles = List.of(FcPuzzle.values());

    @Override
    protected String wikiPageName(@Nullable FcPuzzle puzzle) {
        return "foodcourt";
    }

    @Override
    protected FcSolution makeCandidateSolution(FcSubmission submission) {
        return new FcSolution(submission.getScore(), submission.getAuthor(), submission.getDisplayLink());
    }

    @Override
    protected Path relativePuzzlePath(FcPuzzle puzzle) {
        return Path.of(puzzle.getGroup().name()).resolve(puzzle.getId());
    }

    static String makeFilename(String puzzleName, FcScore score) {
        return puzzleName + "-" + score.toDisplayString(DisplayContext.fileName()) + ".solution";
    }

    @Override
    protected String makeArchiveLink(FcPuzzle puzzle, FcScore score) {
        return makeArchiveLink(puzzle, makeFilename(puzzle.getId(), score));
    }

    @Override
    protected Path makeArchivePath(Path puzzlePath, FcScore score) {
        return puzzlePath.resolve(makeFilename(puzzlePath.getFileName().toString(), score));
    }
}
