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

package com.faendir.zachtronics.bot.archive;

import com.faendir.zachtronics.bot.git.GitRepository;
import com.faendir.zachtronics.bot.model.Solution;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public abstract class AbstractArchive<S extends Solution> implements Archive<S> {
    protected abstract GitRepository getGitRepo();

    @NotNull
    @Override
    public ArchiveResult archive(@NotNull S solution) {
        return getGitRepo().access(a -> {
            ArchiveResult r = performArchive(a, solution);
            a.push();
            return r;
        });
    }

    @NotNull
    @Override
    public List<ArchiveResult> archiveAll(@NotNull Collection<? extends S> solution) {
        return getGitRepo().access(a -> {
            List<ArchiveResult> r = solution.stream()
                                            .map(s -> performArchive(a, s))
                                            .toList();
            a.push();
            return r;
        });
    }

    protected abstract Path relativePuzzlePath(@NotNull S solution);

    protected abstract SolutionsIndex<S> makeSolutionIndex(@NotNull Path puzzlePath,
                                                           @NotNull S solution) throws IOException;

    @NotNull
    private ArchiveResult performArchive(@NotNull GitRepository.AccessScope accessScope, @NotNull S solution) {
        Path repoPath = accessScope.getRepo().toPath();
        Path puzzlePath = repoPath.resolve(relativePuzzlePath(solution));
        boolean newOrEqual;
        try {
            SolutionsIndex<S> index = makeSolutionIndex(puzzlePath, solution);
            newOrEqual = index.add(solution);
        } catch (IOException e) {
            // failures could happen after we dirtied the repo, so we call reset&clean on the puzzle dir
            accessScope.resetAndClean(puzzlePath.toFile());
            log.warn("Recoverable error during archive: ", e);
            return new ArchiveResult.Failure();
        }

        if (!newOrEqual) {
            return new ArchiveResult.Failure();
        }

        if (!accessScope.status().isClean()) {
            accessScope.addAll(puzzlePath.toFile());
            String result = Stream.concat(accessScope.status().getChanged().stream(),
                                          accessScope.status().getAdded().stream())
                                  .map(f -> "[" + f.replaceFirst(".+/", "") + "]" +
                                            "(" + getGitRepo().getRawFilesUrl() + f + ")")
                                  .collect(Collectors.joining(", "));
            RevCommit rev = accessScope.commit(
                    "Added " + solution.getScore().toDisplayString() + " for " + solution.getPuzzle().getDisplayName());
            result += "\n[commit " + rev.name().substring(0, 7) + "]" +
                      "(" + getGitRepo().getUrl().replaceFirst(".git$", "") + "/commit/" + rev.name() + ")";
            return new ArchiveResult.Success(result);
        }
        else {
            // the same exact sol was already archived,
            return new ArchiveResult.AlreadyArchived();
        }
    }
}
