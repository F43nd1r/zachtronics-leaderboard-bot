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

package com.faendir.zachtronics.bot.kz.rest;

import com.faendir.zachtronics.bot.kz.model.KzCategory;
import com.faendir.zachtronics.bot.kz.model.KzGroup;
import com.faendir.zachtronics.bot.kz.model.KzPuzzle;
import com.faendir.zachtronics.bot.kz.model.KzRecord;
import com.faendir.zachtronics.bot.kz.repository.KzSolutionRepository;
import com.faendir.zachtronics.bot.kz.rest.dto.KzPuzzleDTO;
import com.faendir.zachtronics.bot.kz.rest.dto.KzRecordDTO;
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
@RequestMapping("/kz")
@RequiredArgsConstructor
public class KzController implements GameRestController<GroupDTO, KzPuzzleDTO, CategoryDTO, KzRecordDTO> {
    
    private final KzSolutionRepository repository;
    
    @Getter
    private final List<GroupDTO> groups = Arrays.stream(KzGroup.values()).map(GroupDTO::fromGroup).toList();

    @Getter
    private final List<KzPuzzleDTO> puzzles = Arrays.stream(KzPuzzle.values()).map(KzPuzzleDTO::fromPuzzle).toList();

    @Override
    public KzPuzzleDTO getPuzzle(@NotNull String puzzleId) {
        return KzPuzzleDTO.fromPuzzle(findPuzzle(puzzleId));
    }

    @Override
    @NotNull
    public List<KzPuzzleDTO> listPuzzlesByGroup(@NotNull String groupId) {
        KzGroup group = findGroup(groupId);
        return Arrays.stream(KzPuzzle.values()).filter(p -> p.getGroup() == group).map(KzPuzzleDTO::fromPuzzle).toList();
    }

    @Getter
    private final List<CategoryDTO> categories = Arrays.stream(KzCategory.values()).map(CategoryDTO::fromCategory).toList();

    @Override
    public CategoryDTO getCategory(@NotNull String categoryId) {
        return CategoryDTO.fromCategory(findCategory(categoryId));
    }

    @Override
    @NotNull
    public List<KzRecordDTO> listRecords(@NotNull String puzzleId, Boolean includeFrontier) {
        KzPuzzle puzzle = findPuzzle(puzzleId);
        return repository.findCategoryHolders(puzzle, includeFrontier != null && includeFrontier).stream()
                         .map(KzRecordDTO::fromCategoryRecord)
                         .toList();
    }

    @Override
    public KzRecordDTO getRecord(@NotNull String puzzleId, @NotNull String categoryId) {
        KzPuzzle puzzle = findPuzzle(puzzleId);
        KzCategory category = findCategory(categoryId);
        KzRecord record = repository.find(puzzle, category);
        if (record != null)
            return KzRecordDTO.fromCategoryRecord(new CategoryRecord<>(record, EnumSet.of(category)));
        else
            return null;
    }

    private static KzPuzzle findPuzzle(String puzzleId) {
        return Arrays.stream(KzPuzzle.values())
                     .filter(p -> p.name().equals(puzzleId))
                     .findFirst()
                     .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Puzzle " + puzzleId + " not found."));
    }

    private static KzCategory findCategory(String categoryId) {
        return Arrays.stream(KzCategory.values())
                     .filter(c -> c.name().equalsIgnoreCase(categoryId))
                     .findFirst()
                     .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category " + categoryId + " not found."));
    }
    
    private static KzGroup findGroup(String groupId) {
        return Arrays.stream(KzGroup.values())
                     .filter(g -> g.name().equalsIgnoreCase(groupId))
                     .findFirst()
                     .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group " + groupId + " not found."));
    }
}