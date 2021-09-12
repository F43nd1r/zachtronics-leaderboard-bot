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

package com.faendir.zachtronics.bot.sc.archive;

import com.faendir.zachtronics.bot.archive.AbstractArchive;
import com.faendir.zachtronics.bot.archive.SolutionsIndex;
import com.faendir.zachtronics.bot.git.GitRepository;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.ScScore;
import com.faendir.zachtronics.bot.sc.model.ScSolution;
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
public class ScArchive extends AbstractArchive<ScSolution> {
    @Getter
    @Qualifier("scArchiveRepository")
    private final GitRepository gitRepo;

    @Override
    protected Path relativePuzzlePath(@NotNull ScSolution solution) {
        return Paths.get(solution.getPuzzle().getGroup().name(), solution.getPuzzle().name());
    }

    @Override
    protected SolutionsIndex<ScSolution> makeSolutionIndex(@NotNull Path puzzlePath,
                                                           @NotNull ScSolution solution) throws IOException {
        return new ScSolutionsIndex(puzzlePath);
    }

    public String makeArchiveLink(@NotNull ScPuzzle puzzle, @NotNull ScScore score) {
        return String.format("%s/%s/%s/%s.txt", getGitRepo().getRawFilesUrl(), puzzle.getGroup().name(), puzzle.name(),
                score.toDisplayString().replace('/', '-'));
    }

    public String makeArchiveLink(@NotNull ScSolution solution) {
        return makeArchiveLink(solution.getPuzzle(), solution.getScore());
    }
}
