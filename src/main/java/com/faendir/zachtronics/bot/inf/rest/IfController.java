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

package com.faendir.zachtronics.bot.inf.rest;

import com.faendir.zachtronics.bot.inf.model.IfCategory;
import com.faendir.zachtronics.bot.inf.model.IfGroup;
import com.faendir.zachtronics.bot.inf.model.IfPuzzle;
import com.faendir.zachtronics.bot.inf.model.IfRecord;
import com.faendir.zachtronics.bot.inf.repository.IfSolutionRepository;
import com.faendir.zachtronics.bot.inf.rest.dto.IfPuzzleDTO;
import com.faendir.zachtronics.bot.inf.rest.dto.IfRecordDTO;
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
@RequestMapping("/if")
@RequiredArgsConstructor
public class IfController implements GameRestController<GroupDTO, IfPuzzleDTO, CategoryDTO, IfRecordDTO> {
    
    private final IfSolutionRepository repository;
    
    @Getter
    private final List<GroupDTO> groups = Arrays.stream(IfGroup.values()).map(GroupDTO::fromGroup).toList();

    @Getter
    private final List<IfPuzzleDTO> puzzles = Arrays.stream(IfPuzzle.values()).map(IfPuzzleDTO::fromPuzzle).toList();

    @Override
    public IfPuzzleDTO getPuzzle(@NotNull String puzzleId) {
        return IfPuzzleDTO.fromPuzzle(findPuzzle(puzzleId));
    }

    @Override
    @NotNull
    public List<IfPuzzleDTO> listPuzzlesByGroup(@NotNull String groupId) {
        IfGroup group = findGroup(groupId);
        return Arrays.stream(IfPuzzle.values()).filter(p -> p.getGroup() == group).map(IfPuzzleDTO::fromPuzzle).toList();
    }

    @Getter
    private final List<CategoryDTO> categories = Arrays.stream(IfCategory.values()).map(CategoryDTO::fromCategory).toList();

    @Override
    public CategoryDTO getCategory(@NotNull String categoryId) {
        return CategoryDTO.fromCategory(findCategory(categoryId));
    }

    @Override
    @NotNull
    public List<IfRecordDTO> listRecords(@NotNull String puzzleId, Boolean includeFrontier) {
        IfPuzzle puzzle = findPuzzle(puzzleId);
        return repository.findCategoryHolders(puzzle, includeFrontier != null && includeFrontier).stream()
                         .map(IfRecordDTO::fromCategoryRecord)
                         .toList();
    }

    @Override
    public IfRecordDTO getRecord(@NotNull String puzzleId, @NotNull String categoryId) {
        IfPuzzle puzzle = findPuzzle(puzzleId);
        IfCategory category = findCategory(categoryId);
        IfRecord record = repository.find(puzzle, category);
        if (record != null)
            return IfRecordDTO.fromCategoryRecord(new CategoryRecord<>(record, EnumSet.of(category)));
        else
            return null;
    }

    private static IfPuzzle findPuzzle(String puzzleId) {
        return Arrays.stream(IfPuzzle.values())
                     .filter(p -> p.getId().equalsIgnoreCase(puzzleId))
                     .findFirst()
                     .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Puzzle " + puzzleId + " not found."));
    }

    private static IfCategory findCategory(String categoryId) {
        return Arrays.stream(IfCategory.values())
                     .filter(c -> c.name().equalsIgnoreCase(categoryId))
                     .findFirst()
                     .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category " + categoryId + " not found."));
    }
    
    private static IfGroup findGroup(String groupId) {
        return Arrays.stream(IfGroup.values())
                     .filter(g -> g.name().equalsIgnoreCase(groupId))
                     .findFirst()
                     .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group " + groupId + " not found."));
    }
}