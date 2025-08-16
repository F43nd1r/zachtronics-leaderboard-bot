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

package com.faendir.zachtronics.bot.cw.rest;

import com.faendir.zachtronics.bot.cw.model.CwCategory;
import com.faendir.zachtronics.bot.cw.model.CwGroup;
import com.faendir.zachtronics.bot.cw.model.CwPuzzle;
import com.faendir.zachtronics.bot.cw.model.CwRecord;
import com.faendir.zachtronics.bot.cw.repository.CwSolutionRepository;
import com.faendir.zachtronics.bot.cw.rest.dto.CwPuzzleDTO;
import com.faendir.zachtronics.bot.cw.rest.dto.CwRecordDTO;
import com.faendir.zachtronics.bot.repository.CategoryRecord;
import com.faendir.zachtronics.bot.rest.GameRestController;
import com.faendir.zachtronics.bot.rest.dto.CategoryDTO;
import com.faendir.zachtronics.bot.rest.dto.GroupDTO;
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
@RequestMapping("/cw")
@RequiredArgsConstructor
public class CwController implements GameRestController<GroupDTO, CwPuzzleDTO, CategoryDTO, CwRecordDTO> {
    
    private final CwSolutionRepository repository;
    
    @Getter
    private final List<GroupDTO> groups = Arrays.stream(CwGroup.values()).map(GroupDTO::fromGroup).toList();

    @Getter
    private final List<CwPuzzleDTO> puzzles = Arrays.stream(CwPuzzle.values()).map(CwPuzzleDTO::fromPuzzle).toList();

    @Override
    public CwPuzzleDTO getPuzzle(@NotNull String puzzleId) {
        return CwPuzzleDTO.fromPuzzle(findPuzzle(puzzleId));
    }

    @Override
    @NotNull
    public List<CwPuzzleDTO> listPuzzlesByGroup(@NotNull String groupId) {
        CwGroup group = findGroup(groupId);
        return Arrays.stream(CwPuzzle.values()).filter(p -> p.getGroup() == group).map(CwPuzzleDTO::fromPuzzle).toList();
    }

    @Getter
    private final List<CategoryDTO> categories = Arrays.stream(CwCategory.values()).map(CategoryDTO::fromCategory).toList();

    @Override
    public CategoryDTO getCategory(@NotNull String categoryId) {
        return CategoryDTO.fromCategory(findCategory(categoryId));
    }

    @Override
    @NotNull
    public List<CwRecordDTO> listRecords(@NotNull String puzzleId, Boolean includeFrontier) {
        CwPuzzle puzzle = findPuzzle(puzzleId);
        return repository.findCategoryHolders(puzzle, includeFrontier != null && includeFrontier).stream()
                         .map(CwRecordDTO::fromCategoryRecord)
                         .toList();
    }

    @Override
    public CwRecordDTO getRecord(@NotNull String puzzleId, @NotNull String categoryId) {
        CwPuzzle puzzle = findPuzzle(puzzleId);
        CwCategory category = findCategory(categoryId);
        CwRecord record = repository.find(puzzle, category);
        if (record != null)
            return CwRecordDTO.fromCategoryRecord(new CategoryRecord<>(record, EnumSet.of(category)));
        else
            return null;
    }

    private static CwPuzzle findPuzzle(String puzzleId) {
        return Arrays.stream(CwPuzzle.values())
                     .filter(p -> p.name().equalsIgnoreCase(puzzleId))
                     .findFirst()
                     .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Puzzle " + puzzleId + " not found."));
    }

    private static CwCategory findCategory(String categoryId) {
        return Arrays.stream(CwCategory.values())
                     .filter(c -> c.name().equalsIgnoreCase(categoryId))
                     .findFirst()
                     .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category " + categoryId + " not found."));
    }
    
    private static CwGroup findGroup(String groupId) {
        return Arrays.stream(CwGroup.values())
                     .filter(g -> g.name().equalsIgnoreCase(groupId))
                     .findFirst()
                     .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group " + groupId + " not found."));
    }
}