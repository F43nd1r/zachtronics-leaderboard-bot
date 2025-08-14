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
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import static com.faendir.zachtronics.bot.kz.model.KzCategory.*;

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
    private final Comparator<KzSolution> archiveComparator = Comparator.comparing(KzSolution::getScore, TC.getScoreComparator());
    private final List<KzPuzzle> trackedPuzzles = List.of(KzPuzzle.values());

    @Override
    protected @NotNull String wikiPageName(KzPuzzle puzzle) {
        return "index";
    }

    @Override
    protected KzSolution makeCandidateSolution(@NotNull KzSubmission submission) {
        return new KzSolution(submission.getScore(), submission.getAuthor(), submission.getDisplayLink());
    }

    @Override
    protected int frontierCompare(@NotNull KzScore s1, @NotNull KzScore s2) {
        int r1 = Integer.compare(s1.getTime(), s2.getTime());
        int r2 = Integer.compare(s1.getCost(), s2.getCost());
        int r3 = Integer.compare(s1.getArea(), s2.getArea());

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
    protected boolean allowedSameScoreUpdate(@NotNull KzSolution candidate, @NotNull KzSolution solution) {
        return candidate.getDisplayLink() != null ||
               (candidate.getAuthor().equals(solution.getAuthor()) && solution.getDisplayLink() == null);
    }

    @Override
    @NotNull
    protected Path relativePuzzlePath(@NotNull KzPuzzle puzzle) {
        return Paths.get(puzzle.getGroup().name()).resolve(puzzle.name());
    }

    @NotNull
    static String makeFilename(@NotNull KzPuzzle puzzle, @NotNull KzScore score) {
        return puzzle.getPrefix() + "-" + score.toDisplayString(DisplayContext.fileName()) + ".solution";
    }

    @NotNull
    @Override
    protected String makeArchiveLink(@NotNull KzPuzzle puzzle, @NotNull KzScore score) {
        return makeArchiveLink(puzzle, makeFilename(puzzle, score));
    }

    @Override
    @NotNull
    protected Path makeArchivePath(@NotNull Path puzzlePath, KzScore score) {
        KzPuzzle puzzle = KzPuzzle.valueOf(puzzlePath.getFileName().toString());
        return puzzlePath.resolve(makeFilename(puzzle, score));
    }
}
