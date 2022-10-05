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

package com.faendir.zachtronics.bot.inf.repository;

import com.faendir.zachtronics.bot.git.GitRepository;
import com.faendir.zachtronics.bot.inf.model.*;
import com.faendir.zachtronics.bot.model.DisplayContext;
import com.faendir.zachtronics.bot.reddit.RedditService;
import com.faendir.zachtronics.bot.reddit.Subreddit;
import com.faendir.zachtronics.bot.repository.AbstractSolutionRepository;
import com.faendir.zachtronics.bot.repository.SubmitResult;
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

import static com.faendir.zachtronics.bot.inf.model.IfCategory.*;


@Component
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public class IfSolutionRepository extends AbstractSolutionRepository<IfCategory, IfPuzzle, IfScore, IfSubmission, IfRecord, IfSolution> {
    private final IfCategory[][] wikiCategories = {{CF, CB}, {CFNG, CBNG}, {FC, FB}, {BC, BF}};
    private final RedditService redditService;
    private final Subreddit subreddit = Subreddit.INFINIFACTORY;
    private final String wikiPageName = "index";

    @Qualifier("ifRepository")
    private final GitRepository gitRepo;
    private final Class<IfCategory> categoryClass = IfCategory.class;
    final Function<String[], IfSolution> solUnmarshaller = IfSolution::unmarshal;
    private final Comparator<IfSolution> archiveComparator = Comparator.comparing(IfSolution::getScore, CF.getScoreComparator());

    @NotNull
    @Override
    public SubmitResult<IfRecord, IfCategory> submit(@NotNull IfSubmission submission) {
        try (GitRepository.ReadWriteAccess access = gitRepo.acquireWriteAccess()) {
            return submitOne(access, submission, (s, c) -> access.push());
        }
    }

    @Override
    protected IfSolution makeCandidateSolution(@NotNull IfSubmission submission) {
        return new IfSolution(submission.getScore(), submission.getAuthor(), submission.getDisplayLinks());
    }

    @Override
    protected int dominanceCompare(@NotNull IfScore s1, @NotNull IfScore s2) {
        int r1 = Integer.compare(s1.getCycles(), s2.getCycles());
        int r2 = Integer.compare(s1.getFootprint(), s2.getFootprint());
        int r3 = Integer.compare(s1.getBlocks(), s2.getBlocks());
        int r4 = Boolean.compare(s1.usesGRA(), s2.usesGRA());
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
    protected boolean alreadyPresent(@NotNull IfSolution candidate, @NotNull IfSolution solution) {
        return candidate.getScore().equals(solution.getScore()) &&
               candidate.getDisplayLinks().isEmpty() &&
               !(candidate.getAuthor().equals(solution.getAuthor()) && solution.getDisplayLinks().isEmpty());
    }

    @Override
    @NotNull
    protected Path relativePuzzlePath(@NotNull IfPuzzle puzzle) {
        return Paths.get(puzzle.getGroup().name(), puzzle.getId());
    }

    @NotNull
    static String makeScoreFilename(@NotNull IfScore score) {
        return score.toDisplayString(DisplayContext.fileName()) + ".txt";
    }

    @NotNull
    @Override
    protected String makeArchiveLink(@NotNull IfPuzzle puzzle, @NotNull IfScore score) {
        return makeArchiveLink(puzzle, makeScoreFilename(score));
    }

    @Override
    @NotNull
    protected Path makeArchivePath(@NotNull Path puzzlePath, IfScore score) {
        return puzzlePath.resolve(makeScoreFilename(score));
    }
}
