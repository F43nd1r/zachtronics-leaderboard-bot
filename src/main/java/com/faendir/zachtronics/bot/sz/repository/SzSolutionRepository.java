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
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import static com.faendir.zachtronics.bot.sz.model.SzCategory.*;

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
    private final Comparator<SzSolution> archiveComparator = Comparator.comparing(SzSolution::getScore, SzCategory.CP.getScoreComparator());
    private final List<SzPuzzle> trackedPuzzles = Arrays.stream(SzPuzzle.values()).filter(p -> p.getType() != SzType.SANDBOX).toList();

    @Override
    protected @NonNull String wikiPageName(SzPuzzle puzzle) {
        return "index";
    }

    @Override
    protected SzSolution makeCandidateSolution(@NonNull SzSubmission submission) {
        return new SzSolution(submission.getScore(), submission.getAuthor(), submission.getDisplayLink());
    }

    @Override
    protected int frontierCompare(@NonNull SzScore s1, @NonNull SzScore s2) {
        int r1 = Integer.compare(s1.getCost(), s2.getCost());
        int r2 = Integer.compare(s1.getPower(), s2.getPower());
        int r3 = Integer.compare(s1.getLines(), s2.getLines());
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

    /** allow same-score solution changes only if you are the original author */
    @Override
    protected boolean allowedSameScoreUpdate(@NonNull SzSolution candidate, @NonNull SzSolution solution) {
        return candidate.getDisplayLink() != null ||
               (candidate.getAuthor().equals(solution.getAuthor()) && solution.getDisplayLink() == null);
    }
    
    @Override
    @NonNull
    protected Path relativePuzzlePath(@NonNull SzPuzzle puzzle) {
        return Paths.get(puzzle.getGroup().name()).resolve(puzzle.getId());
    }

    @NonNull
    static String makeFilename(@NonNull String puzzleId, @NonNull SzScore score) {
        return puzzleId + "-" + score.toDisplayString(DisplayContext.fileName()) + ".txt";
    }

    @NonNull
    @Override
    protected String makeArchiveLink(@NonNull SzPuzzle puzzle, @NonNull SzScore score) {
        return makeArchiveLink(puzzle, makeFilename(puzzle.getId(), score));
    }

    @Override
    @NonNull
    protected Path makeArchivePath(@NonNull Path puzzlePath, SzScore score) {
        return puzzlePath.resolve(makeFilename(puzzlePath.getFileName().toString(), score));
    }
}
