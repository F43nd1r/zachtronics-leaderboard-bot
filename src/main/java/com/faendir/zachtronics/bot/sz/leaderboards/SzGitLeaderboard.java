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

package com.faendir.zachtronics.bot.sz.leaderboards;

import com.faendir.zachtronics.bot.git.GitRepository;
import com.faendir.zachtronics.bot.leaderboards.Leaderboard;
import com.faendir.zachtronics.bot.sz.archive.SzArchive;
import com.faendir.zachtronics.bot.sz.model.SzCategory;
import com.faendir.zachtronics.bot.sz.model.SzPuzzle;
import com.faendir.zachtronics.bot.sz.model.SzRecord;
import com.faendir.zachtronics.bot.sz.model.SzSolution;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SzGitLeaderboard implements Leaderboard<SzCategory, SzPuzzle, SzRecord> {
    @Getter
    private final List<SzCategory> supportedCategories = Arrays.asList(SzCategory.values());
    private final SzArchive archive;

    private static final Pattern NAME_REGEX = Pattern
            .compile("top solution (?:cost|power|lines)(?:->(?:cost|power|lines))?(?: - (?<author>.+))?", Pattern.CASE_INSENSITIVE);

    @Nullable
    @Override
    public SzRecord get(@NotNull SzPuzzle puzzle, @NotNull SzCategory category) {
        return archive.getGitRepo().access(a -> readSolutionFile(findPuzzleFile(a, puzzle, category)));
    }

    @NotNull
    @Override
    public Map<SzCategory, SzRecord> getAll(@NotNull SzPuzzle puzzle, @NotNull Collection<? extends SzCategory> categories) {
        return archive.getGitRepo().access(
                a -> categories.stream().collect(Collectors.toMap(category -> category,
                                                                  category -> readSolutionFile(
                                                                          findPuzzleFile(a, puzzle, category)))));
    }

    @NotNull
    private SzRecord readSolutionFile(Path solutionFile) {
        SzSolution solution = new SzSolution(solutionFile);
        Matcher m = NAME_REGEX.matcher(solution.getTitle());
        if (!m.matches())
            throw new IllegalStateException("Name does not match standard format: " + m.replaceFirst(""));
        String author = m.group("author");
        String link = archive.makeArchiveLink(solution.getPuzzle(), solutionFile.getFileName().toString());
        return new SzRecord(solution.getScore(), author, link, solutionFile);
    }

    private static Path findPuzzleFile(@NotNull GitRepository.AccessScope accessScope, @NotNull SzPuzzle puzzle,
                                       @NotNull SzCategory category) {
        Path repo = accessScope.getRepo().toPath();
        Path puzzleFolder = repo.resolve(puzzle.getGroup().getRepoFolder());
        Path puzzleFile = puzzleFolder.resolve(puzzle.getId() + "-" + category.getRepoSuffix() + ".txt");
        if (!Files.exists(puzzleFile)) {
            // we're missing the X02 subcategory, we just have a X01 file
            puzzleFile = puzzleFolder.resolve(puzzle.getId() + "-" + (category.getRepoSuffix() - 1) + ".txt");
        }
        return puzzleFile;
    }
}
