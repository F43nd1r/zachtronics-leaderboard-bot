/*
 * Copyright (c) 2021
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

package com.faendir.zachtronics.bot.repository;

import com.faendir.zachtronics.bot.git.GitRepository;
import com.faendir.zachtronics.bot.model.Category;
import com.faendir.zachtronics.bot.model.Puzzle;
import com.faendir.zachtronics.bot.model.Record;
import com.faendir.zachtronics.bot.model.Submission;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractSolutionRepository<C extends Category, P extends Puzzle<C>, S extends Submission<C, P>, R extends Record<C>>
        implements SolutionRepository<C, P, S, R> {

    protected abstract GitRepository getGitRepo();
    protected abstract Path relativePuzzlePath(@NotNull P puzzle);

    protected abstract SolutionsIndex<S> makeSolutionIndex(@NotNull Path puzzlePath,
                                                           @NotNull P puzzle) throws IOException;

    protected <T> List<T> reshapeCategoryRecordMap(@NotNull Map<R, Set<C>> crMap, BiFunction<R, Set<C>, T> entryCreator) {
        return crMap.entrySet().stream()
                               .map(p -> entryCreator.apply(p.getKey(), p.getValue()))
                               .toList();
    }

    @NotNull
    protected SubmitResult<R, C> performArchive(@NotNull GitRepository.ReadWriteAccess access, @NotNull S submission) {
        P puzzle = submission.getPuzzle();
        Path puzzlePath = access.getRepo().toPath().resolve(relativePuzzlePath(puzzle));

        SolutionsIndex<S> index;
        boolean newOrEqual;
        try {
            index = makeSolutionIndex(puzzlePath, puzzle);
            newOrEqual = index.add(submission);
        } catch (IOException e) {
            // failures could happen after we dirtied the repo, so we call reset&clean on the puzzle dir
            access.resetAndClean(puzzlePath.toFile());
            return new SubmitResult.Failure<>(e.toString());
        }

        if (!newOrEqual) {
            return new SubmitResult.NothingBeaten<>(Collections.emptyList());
        }

        if (access.status().isClean()) {
            // the same exact sol was already archived,
            return new SubmitResult.AlreadyPresent<>();
        }

        access.addAll(puzzlePath.toFile());
        String result = Stream.concat(access.status().getChanged().stream(),
                                      access.status().getAdded().stream())
                              .map(f -> "[" + f.replaceFirst(".+/", "") + "]" +
                                        "(" + getGitRepo().getRawFilesUrl() + "/" + f + ")")
                              .collect(Collectors.joining(", "));
        RevCommit rev = access.commit("Added " + submission.getScore().toDisplayString() +
                                      " for " + submission.getPuzzle().getDisplayName() +
                                      (submission.getAuthor() != null ? " by " + submission.getAuthor() : ""));
        result += "\n[commit " + rev.name().substring(0, 7) + "]" +
                  "(" + getGitRepo().getUrl().replaceFirst(".git$", "") + "/commit/" + rev.name() + ")";
        return new SubmitResult.Success<>(result, Collections.emptyList());
    }

    @NotNull
    protected Path makeArchivePath(@NotNull P puzzle, @NotNull String filename) {
        try (GitRepository.ReadAccess access = getGitRepo().acquireReadAccess()) {
            return access.getRepo().toPath().resolve(relativePuzzlePath(puzzle)).resolve(filename);
        }
    }

    protected String makeArchiveLink(@NotNull P puzzle, @NotNull String filename) {
        return String.format("%s/%s/%s", getGitRepo().getRawFilesUrl(), relativePuzzlePath(puzzle),
                             filename);
    }


    /** Interface for classes that handle archival data for a level in the respective repository */
    protected interface SolutionsIndex<S> {

        /**
         * @return whether the new solution sits on the frontier
         */
        boolean add(S submission) throws IOException;
    }
}
