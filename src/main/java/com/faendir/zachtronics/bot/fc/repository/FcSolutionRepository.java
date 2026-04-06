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
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import static com.faendir.zachtronics.bot.fc.model.FcCategory.*;

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
    private final Comparator<FcSolution> archiveComparator = Comparator.comparing(FcSolution::getScore, TCS.getScoreComparator());
    private final List<FcPuzzle> trackedPuzzles = List.of(FcPuzzle.values());

    @Override
    protected @NonNull String wikiPageName(FcPuzzle puzzle) {
        return "foodcourt";
    }

    @Override
    protected FcSolution makeCandidateSolution(@NonNull FcSubmission submission) {
        return new FcSolution(submission.getScore(), submission.getAuthor(), submission.getDisplayLink());
    }

    @Override
    protected int frontierCompare(@NonNull FcScore s1, @NonNull FcScore s2) {
        int r1 = Integer.compare(s1.getCost(), s2.getCost());
        int r2 = Integer.compare(s1.getTime(), s2.getTime());
        int r3 = Integer.compare(s1.getSumTimes(), s2.getSumTimes());
        int r4 = Integer.compare(s1.getWires(), s2.getWires());

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

    @Override
    protected boolean allowedSameScoreUpdate(@NonNull FcSolution candidate, @NonNull FcSolution solution) {
        return candidate.getDisplayLink() != null ||
               (candidate.getAuthor().equals(solution.getAuthor()) && solution.getDisplayLink() == null);
    }

    @Override
    @NonNull
    protected Path relativePuzzlePath(@NonNull FcPuzzle puzzle) {
        return Paths.get(puzzle.getGroup().name()).resolve(puzzle.getId());
    }

    @NonNull
    static String makeFilename(@NonNull String puzzleName, @NonNull FcScore score) {
        return puzzleName + "-" + score.toDisplayString(DisplayContext.fileName()) + ".solution";
    }

    @NonNull
    @Override
    protected String makeArchiveLink(@NonNull FcPuzzle puzzle, @NonNull FcScore score) {
        return makeArchiveLink(puzzle, makeFilename(puzzle.getId(), score));
    }

    @Override
    @NonNull
    protected Path makeArchivePath(@NonNull Path puzzlePath, FcScore score) {
        return puzzlePath.resolve(makeFilename(puzzlePath.getFileName().toString(), score));
    }
}
