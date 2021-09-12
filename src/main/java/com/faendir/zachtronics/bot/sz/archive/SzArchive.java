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

package com.faendir.zachtronics.bot.sz.archive;

import com.faendir.zachtronics.bot.archive.AbstractArchive;
import com.faendir.zachtronics.bot.archive.SolutionsIndex;
import com.faendir.zachtronics.bot.git.GitRepository;
import com.faendir.zachtronics.bot.sz.model.SzPuzzle;
import com.faendir.zachtronics.bot.sz.model.SzSolution;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@RequiredArgsConstructor
public class SzArchive extends AbstractArchive<SzSolution> {
    @Getter
    @Qualifier("szRepository")
    private final GitRepository gitRepo;

    @Override
    protected Path relativePuzzlePath(@NotNull SzSolution solution) {
        return Paths.get(solution.getPuzzle().getGroup().getRepoFolder());
    }

    @Override
    protected SolutionsIndex<SzSolution> makeSolutionIndex(@NotNull Path puzzlePath,
                                                           @NotNull SzSolution solution) throws IOException {
        return new SzSolutionsIndex(puzzlePath, solution.getPuzzle());
    }

    public String makeArchiveLink(@NotNull SzPuzzle puzzle, @NotNull String fileName) {
        return String.format("%s/%s/%s", getGitRepo().getRawFilesUrl(), puzzle.getGroup().getRepoFolder(), fileName);
    }
}
