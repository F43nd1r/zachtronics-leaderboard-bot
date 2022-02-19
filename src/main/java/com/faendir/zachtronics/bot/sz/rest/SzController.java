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

package com.faendir.zachtronics.bot.sz.rest;

import com.faendir.zachtronics.bot.repository.CategoryRecord;
import com.faendir.zachtronics.bot.rest.GameRestController;
import com.faendir.zachtronics.bot.sz.model.SzCategory;
import com.faendir.zachtronics.bot.sz.model.SzGroup;
import com.faendir.zachtronics.bot.sz.model.SzPuzzle;
import com.faendir.zachtronics.bot.sz.model.SzRecord;
import com.faendir.zachtronics.bot.sz.repository.SzSolutionRepository;
import com.faendir.zachtronics.bot.sz.rest.dto.SzCategoryDTO;
import com.faendir.zachtronics.bot.sz.rest.dto.SzGroupDTO;
import com.faendir.zachtronics.bot.sz.rest.dto.SzPuzzleDTO;
import com.faendir.zachtronics.bot.sz.rest.dto.SzRecordDTO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

@RestController
@RequestMapping("/sz")
@RequiredArgsConstructor
public class SzController implements GameRestController<SzGroupDTO, SzPuzzleDTO, SzCategoryDTO, SzRecordDTO> {
    
    private final SzSolutionRepository repository;
    
    @Getter
    private final List<SzGroupDTO> groups = Arrays.stream(SzGroup.values()).map(SzGroupDTO::fromGroup).toList();

    @Getter
    private final List<SzPuzzleDTO> puzzles = Arrays.stream(SzPuzzle.values()).map(SzPuzzleDTO::fromPuzzle).toList();

    @Override
    public SzPuzzleDTO getPuzzle(@NotNull String puzzleId) {
        return SzPuzzleDTO.fromPuzzle(findPuzzle(puzzleId));
    }

    @Override
    @NotNull
    public List<SzPuzzleDTO> listPuzzlesByGroup(@NotNull String groupId) {
        SzGroup group = findGroup(groupId);
        return Arrays.stream(SzPuzzle.values()).filter(p -> p.getGroup() == group).map(SzPuzzleDTO::fromPuzzle).toList();
    }

    @Getter
    private final List<SzCategoryDTO> categories = Arrays.stream(SzCategory.values()).map(SzCategoryDTO::fromCategory).toList();

    @Override
    public SzCategoryDTO getCategory(@NotNull String categoryId) {
        return SzCategoryDTO.fromCategory(findCategory(categoryId));
    }

    @Override
    @NotNull
    public List<SzRecordDTO> listRecords(@NotNull String puzzleId, Boolean includeFrontier) {
        SzPuzzle puzzle = findPuzzle(puzzleId);
        return repository.findCategoryHolders(puzzle, includeFrontier != null && includeFrontier).stream()
                         .map(SzRecordDTO::fromCategoryRecord)
                         .toList();
    }

    @Override
    public SzRecordDTO getRecord(@NotNull String puzzleId, @NotNull String categoryId) {
        SzPuzzle puzzle = findPuzzle(puzzleId);
        SzCategory category = findCategory(categoryId);
        SzRecord record = repository.find(puzzle, category);
        if (record != null)
            return SzRecordDTO.fromCategoryRecord(new CategoryRecord<>(record, EnumSet.of(category)));
        else
            return null;
    }

    private static SzPuzzle findPuzzle(String puzzleId) {
        return Arrays.stream(SzPuzzle.values())
                     .filter(p -> p.getId().equalsIgnoreCase(puzzleId))
                     .findFirst()
                     .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Puzzle " + puzzleId + " not found."));
    }

    private static SzCategory findCategory(String categoryId) {
        return Arrays.stream(SzCategory.values())
                     .filter(c -> c.name().equalsIgnoreCase(categoryId))
                     .findFirst()
                     .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category " + categoryId + " not found."));
    }
    
    private static SzGroup findGroup(String groupId) {
        return Arrays.stream(SzGroup.values())
                     .filter(g -> g.name().equalsIgnoreCase(groupId))
                     .findFirst()
                     .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group " + groupId + " not found."));
    }
}