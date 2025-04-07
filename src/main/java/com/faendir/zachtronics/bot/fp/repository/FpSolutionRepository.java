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

package com.faendir.zachtronics.bot.fp.repository;

import com.faendir.zachtronics.bot.fp.model.*;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import static com.faendir.zachtronics.bot.fp.model.FpCategory.*;

@Component
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public class FpSolutionRepository extends AbstractSolutionRepository<FpCategory, FpPuzzle, FpScore, FpSubmission, FpRecord, FpSolution> {
    private final FpCategory[][] wikiCategories = {{RCF, RFC}, {CRF, CFR}, {FRC, FCR}, {wRCF, wFRC}};
    private final RedditService redditService;
    private final Subreddit subreddit = Subreddit.LASTCALLBBS;

    @Qualifier("fpRepository")
    private final GitRepository gitRepo;
    private final Class<FpCategory> categoryClass = FpCategory.class;
    private final Function<String[], FpSolution> solUnmarshaller = FpSolution::unmarshal;
    private final Comparator<FpSolution> archiveComparator = Comparator.comparing(FpSolution::getScore, RCF.getScoreComparator());
    private final List<FpPuzzle> trackedPuzzles = Arrays.stream(FpPuzzle.values()).filter(p -> p.getType() != FpType.EDITOR).toList();

    @Override
    protected @NotNull String wikiPageName(FpPuzzle puzzle) {
        return "forbidden-path";
    }

    @Override
    protected FpSolution makeCandidateSolution(@NotNull FpSubmission submission) {
        return new FpSolution(submission.getScore(), submission.getAuthor(), submission.getDisplayLink());
    }

    @Override
    protected int frontierCompare(@NotNull FpScore s1, @NotNull FpScore s2) {
        int r1 = Integer.compare(s1.getRules(), s2.getRules());
        int r2 = Integer.compare(s1.getConditionalRules(), s2.getConditionalRules());
        int r3 = Integer.compare(s1.getFrames(), s2.getFrames());
        // waste is a special boy, the metric has no direction, so different waste = uncomparable
        int r4 = Integer.compare(s1.getWaste(), s2.getWaste());

        if (r1 <= 0 && r2 <= 0 && r3 <= 0 && r4 == 0) {
            // s1 dominates
            return -1;
        }
        else if (r1 >= 0 && r2 >= 0 && r3 >= 0 && r4 == 0) {
            // s2 dominates
            return 1;
        }
        else {
            // equal is already captured by the 1st check, this is for "not comparable"
            return 0;
        }
    }

    @Override
    protected boolean allowedSameScoreUpdate(@NotNull FpSolution candidate, @NotNull FpSolution solution) {
        return candidate.getDisplayLink() != null ||
               (candidate.getAuthor().equals(solution.getAuthor()) && solution.getDisplayLink() == null);
    }

    @Override
    @NotNull
    protected Path relativePuzzlePath(@NotNull FpPuzzle puzzle) {
        return Paths.get(puzzle.getGroup().name()).resolve(puzzle.name());
    }

    @NotNull
    static String makeScoreFilename(@NotNull FpScore score) {
        return score.toDisplayString(DisplayContext.fileName()) + ".txt";
    }

    @NotNull
    @Override
    protected String makeArchiveLink(@NotNull FpPuzzle puzzle, @NotNull FpScore score) {
        return makeArchiveLink(puzzle, makeScoreFilename(score));
    }

    @Override
    @NotNull
    protected Path makeArchivePath(@NotNull Path puzzlePath, FpScore score) {
        return puzzlePath.resolve(makeScoreFilename(score));
    }
}
