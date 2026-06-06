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

package com.faendir.zachtronics.bot.kz.repository;

import com.faendir.zachtronics.bot.git.GitRepository;
import com.faendir.zachtronics.bot.kz.model.*;
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

import static com.faendir.zachtronics.bot.kz.model.KzCategory.*;
import static com.faendir.zachtronics.bot.kz.model.KzMetric.*;

@Component
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public class KzSolutionRepository extends AbstractSolutionRepository<KzCategory, KzPuzzle, KzScore, KzSubmission, KzRecord, KzSolution> {
    private final KzCategory[][] wikiCategories = {{TC, TA}, {CT, CA}, {AT, AC}};
    private final RedditService redditService;
    private final Subreddit subreddit = Subreddit.KAIZEN;

    @Qualifier("kzRepository")
    private final GitRepository gitRepo;
    private final Class<KzCategory> categoryClass = KzCategory.class;
    private final Function<String[], KzSolution> solUnmarshaller = KzSolution::unmarshal;
    private final List<Comparator<KzScore>> frontierComparators = List.of(TIME, COST, AREA);
    private final List<KzPuzzle> trackedPuzzles = List.of(KzPuzzle.values());

    @Override
    protected String wikiPageName(@Nullable KzPuzzle puzzle) {
        return "index";
    }

    @Override
    protected KzSolution makeCandidateSolution(KzSubmission submission) {
        return new KzSolution(submission.getScore(), submission.getAuthor(), submission.getDisplayLink());
    }

    @Override
    protected Path relativePuzzlePath(KzPuzzle puzzle) {
        return Path.of(puzzle.getGroup().name()).resolve(puzzle.name());
    }

    static String makeFilename(KzPuzzle puzzle, KzScore score) {
        return puzzle.getPrefix() + "-" + score.toDisplayString(DisplayContext.fileName()) + ".solution";
    }

    @Override
    protected String makeArchiveLink(KzPuzzle puzzle, KzScore score) {
        return makeArchiveLink(puzzle, makeFilename(puzzle, score));
    }

    @Override
    protected Path makeArchivePath(Path puzzlePath, KzScore score) {
        KzPuzzle puzzle = KzPuzzle.valueOf(puzzlePath.getFileName().toString());
        return puzzlePath.resolve(makeFilename(puzzle, score));
    }
}
