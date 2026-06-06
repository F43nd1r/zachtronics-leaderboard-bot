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
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import static com.faendir.zachtronics.bot.fp.model.FpCategory.*;
import static com.faendir.zachtronics.bot.fp.model.FpMetric.*;

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
    // waste is a special boy, the metric has no direction, so different waste = uncomparable
    private final List<Comparator<FpScore>> frontierComparators = List.of(RULES, CONDITIONAL_RULES, FRAMES, WASTE, MOST_WASTE);
    private final List<FpPuzzle> trackedPuzzles = Arrays.stream(FpPuzzle.values()).filter(p -> p.getType() != FpType.EDITOR).toList();

    @Override
    protected String wikiPageName(@Nullable FpPuzzle puzzle) {
        return "forbidden-path";
    }

    @Override
    protected FpSolution makeCandidateSolution(FpSubmission submission) {
        return new FpSolution(submission.getScore(), submission.getAuthor(), submission.getDisplayLink());
    }

    @Override
    protected Path relativePuzzlePath(FpPuzzle puzzle) {
        return Path.of(puzzle.getGroup().name()).resolve(puzzle.name());
    }

    static String makeScoreFilename(FpScore score) {
        return score.toDisplayString(DisplayContext.fileName()) + ".txt";
    }

    @Override
    protected String makeArchiveLink(FpPuzzle puzzle, FpScore score) {
        return makeArchiveLink(puzzle, makeScoreFilename(score));
    }

    @Override
    protected Path makeArchivePath(Path puzzlePath, FpScore score) {
        return puzzlePath.resolve(makeScoreFilename(score));
    }
}
