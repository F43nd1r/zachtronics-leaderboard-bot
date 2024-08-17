/*
 * Copyright (c) 2024
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

package com.faendir.zachtronics.bot.exa.rest;

import com.faendir.zachtronics.bot.exa.model.ExaCategory;
import com.faendir.zachtronics.bot.exa.model.ExaGroup;
import com.faendir.zachtronics.bot.exa.model.ExaPuzzle;
import com.faendir.zachtronics.bot.exa.model.ExaRecord;
import com.faendir.zachtronics.bot.exa.repository.ExaSolutionRepository;
import com.faendir.zachtronics.bot.exa.rest.dto.ExaCategoryDTO;
import com.faendir.zachtronics.bot.exa.rest.dto.ExaGroupDTO;
import com.faendir.zachtronics.bot.exa.rest.dto.ExaPuzzleDTO;
import com.faendir.zachtronics.bot.exa.rest.dto.ExaRecordDTO;
import com.faendir.zachtronics.bot.repository.CategoryRecord;
import com.faendir.zachtronics.bot.rest.GameRestController;
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
@RequestMapping("/exa")
@RequiredArgsConstructor
public class ExaController implements GameRestController<ExaGroupDTO, ExaPuzzleDTO, ExaCategoryDTO, ExaRecordDTO> {
    
    private final ExaSolutionRepository repository;
    
    @Getter
    private final List<ExaGroupDTO> groups = Arrays.stream(ExaGroup.values()).map(ExaGroupDTO::fromGroup).toList();

    @Getter
    private final List<ExaPuzzleDTO> puzzles = Arrays.stream(ExaPuzzle.values()).map(ExaPuzzleDTO::fromPuzzle).toList();

    @Override
    public ExaPuzzleDTO getPuzzle(@NotNull String puzzleId) {
        return ExaPuzzleDTO.fromPuzzle(findPuzzle(puzzleId));
    }

    @Override
    @NotNull
    public List<ExaPuzzleDTO> listPuzzlesByGroup(@NotNull String groupId) {
        ExaGroup group = findGroup(groupId);
        return Arrays.stream(ExaPuzzle.values()).filter(p -> p.getGroup() == group).map(ExaPuzzleDTO::fromPuzzle).toList();
    }

    @Getter
    private final List<ExaCategoryDTO> categories = Arrays.stream(ExaCategory.values()).map(ExaCategoryDTO::fromCategory).toList();

    @Override
    public ExaCategoryDTO getCategory(@NotNull String categoryId) {
        return ExaCategoryDTO.fromCategory(findCategory(categoryId));
    }

    @Override
    @NotNull
    public List<ExaRecordDTO> listRecords(@NotNull String puzzleId, Boolean includeFrontier) {
        ExaPuzzle puzzle = findPuzzle(puzzleId);
        return repository.findCategoryHolders(puzzle, includeFrontier != null && includeFrontier).stream()
                         .map(ExaRecordDTO::fromCategoryRecord)
                         .toList();
    }

    @Override
    public ExaRecordDTO getRecord(@NotNull String puzzleId, @NotNull String categoryId) {
        ExaPuzzle puzzle = findPuzzle(puzzleId);
        ExaCategory category = findCategory(categoryId);
        ExaRecord record = repository.find(puzzle, category);
        if (record != null)
            return ExaRecordDTO.fromCategoryRecord(new CategoryRecord<>(record, EnumSet.of(category)));
        else
            return null;
    }

    private static ExaPuzzle findPuzzle(String puzzleId) {
        return Arrays.stream(ExaPuzzle.values())
                     .filter(p -> p.name().equalsIgnoreCase(puzzleId))
                     .findFirst()
                     .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Puzzle " + puzzleId + " not found."));
    }

    private static ExaCategory findCategory(String categoryId) {
        return Arrays.stream(ExaCategory.values())
                     .filter(c -> c.name().equalsIgnoreCase(categoryId))
                     .findFirst()
                     .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category " + categoryId + " not found."));
    }
    
    private static ExaGroup findGroup(String groupId) {
        return Arrays.stream(ExaGroup.values())
                     .filter(g -> g.name().equalsIgnoreCase(groupId))
                     .findFirst()
                     .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group " + groupId + " not found."));
    }
}