/*
 * Copyright (c) 2022
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
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.function.Function;

import static com.faendir.zachtronics.bot.sz.model.SzCategory.*;

@Component
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public class SzSolutionRepository extends AbstractSolutionRepository<SzCategory, SzPuzzle, SzScore, SzSubmission, SzRecord, SzSolution> {
    private final SzCategory[][] wikiCategories = {{CP, CL}, {PC, PL}, {LC, LP}};
    private final RedditService redditService;
    private final Subreddit subreddit = Subreddit.SHENZHEN_IO;
    private final String wikiPageName = "index";

    @Qualifier("szRepository")
    private final GitRepository gitRepo;
    private final Class<SzCategory> categoryClass = SzCategory.class;
    final Function<String[], SzSolution> solUnmarshaller = SzSolution::unmarshal;
    private final Comparator<SzSolution> archiveComparator = Comparator.comparing(SzSolution::getScore, SzCategory.CP.getScoreComparator());

    @Override
    protected SzSolution makeCandidateSolution(@NotNull SzSubmission submission) {
        return new SzSolution(submission.getScore(), submission.getAuthor(), submission.getDisplayLink());
    }

    @Override
    protected int dominanceCompare(@NotNull SzScore s1, @NotNull SzScore s2) {
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
    protected boolean alreadyPresent(@NotNull SzSolution candidate, @NotNull SzSolution solution) {
        return candidate.getScore().equals(solution.getScore()) &&
               candidate.getDisplayLink() == null &&
               !(candidate.getAuthor().equals(solution.getAuthor()) && solution.getDisplayLink() == null);
    }
    
    @Override
    @NotNull
    protected Path relativePuzzlePath(@NotNull SzPuzzle puzzle) {
        return Paths.get(puzzle.getGroup().getRepoFolder()).resolve(puzzle.getId());
    }

    @NotNull
    static String makeFilename(@NotNull String puzzleId, @NotNull SzScore score) {
        return puzzleId + "-" + score.toDisplayString(DisplayContext.fileName()) + ".txt";
    }

    @NotNull
    @Override
    protected String makeArchiveLink(@NotNull SzPuzzle puzzle, @NotNull SzScore score) {
        return makeArchiveLink(puzzle, makeFilename(puzzle.getId(), score));
    }

    @Override
    @NotNull
    protected Path makeArchivePath(@NotNull Path puzzlePath, SzScore score) {
        return puzzlePath.resolve(makeFilename(puzzlePath.getFileName().toString(), score));
    }
}
