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

package com.faendir.zachtronics.bot.sz.repository;

import com.faendir.zachtronics.bot.git.GitRepository;
import com.faendir.zachtronics.bot.model.DisplayContext;
import com.faendir.zachtronics.bot.reddit.RedditService;
import com.faendir.zachtronics.bot.reddit.Subreddit;
import com.faendir.zachtronics.bot.repository.AbstractSolutionRepository;
import com.faendir.zachtronics.bot.sz.model.*;
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

import static com.faendir.zachtronics.bot.sz.model.SzCategory.*;
import static com.faendir.zachtronics.bot.sz.model.SzMetric.*;

@Component
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public class SzSolutionRepository extends AbstractSolutionRepository<SzCategory, SzPuzzle, SzScore, SzSubmission, SzRecord, SzSolution> {
    private final SzCategory[][] wikiCategories = {{CP, CL}, {PC, PL}, {LC, LP}};
    private final RedditService redditService;
    private final Subreddit subreddit = Subreddit.SHENZHEN_IO;

    @Qualifier("szRepository")
    private final GitRepository gitRepo;
    private final Class<SzCategory> categoryClass = SzCategory.class;
    private final Function<String[], SzSolution> solUnmarshaller = SzSolution::unmarshal;
    private final List<Comparator<SzScore>> frontierComparators = List.of(COST, POWER, LINES);
    private final List<SzPuzzle> trackedPuzzles = Arrays.stream(SzPuzzle.values()).filter(p -> p.getType() != SzType.SANDBOX).toList();

    @Override
    protected String wikiPageName(@Nullable SzPuzzle puzzle) {
        return "index";
    }

    @Override
    protected SzSolution makeCandidateSolution(SzSubmission submission) {
        return new SzSolution(submission.getScore(), submission.getAuthor(), submission.getDisplayLink());
    }

    @Override
    protected Path relativePuzzlePath(SzPuzzle puzzle) {
        return Path.of(puzzle.getGroup().name()).resolve(puzzle.getId());
    }

    static String makeFilename(String puzzleId, SzScore score) {
        return puzzleId + "-" + score.toDisplayString(DisplayContext.fileName()) + ".txt";
    }

    @Override
    protected String makeArchiveLink(SzPuzzle puzzle, SzScore score) {
        return makeArchiveLink(puzzle, makeFilename(puzzle.getId(), score));
    }

    @Override
    protected Path makeArchivePath(Path puzzlePath, SzScore score) {
        return puzzlePath.resolve(makeFilename(puzzlePath.getFileName().toString(), score));
    }
}
