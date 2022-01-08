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
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

public abstract class AbstractSolutionRepository<C extends Category, P extends Puzzle<C>, S extends Submission<C, P>, R extends Record<C>>
        implements SolutionRepository<C, P, S, R> {

    protected abstract GitRepository getGitRepo();
    protected abstract Path relativePuzzlePath(@NotNull P puzzle);

    protected <T> List<T> reshapeCategoryRecordMap(@NotNull Map<R, Set<C>> crMap, BiFunction<R, Set<C>, T> entryCreator) {
        return crMap.entrySet().stream()
                               .map(p -> entryCreator.apply(p.getKey(), p.getValue()))
                               .toList();
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
}
