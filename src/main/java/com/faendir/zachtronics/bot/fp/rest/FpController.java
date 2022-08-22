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

package com.faendir.zachtronics.bot.fp.rest;

import com.faendir.zachtronics.bot.fp.model.FpCategory;
import com.faendir.zachtronics.bot.fp.model.FpGroup;
import com.faendir.zachtronics.bot.fp.model.FpPuzzle;
import com.faendir.zachtronics.bot.fp.model.FpRecord;
import com.faendir.zachtronics.bot.fp.repository.FpSolutionRepository;
import com.faendir.zachtronics.bot.fp.rest.dto.FpCategoryDTO;
import com.faendir.zachtronics.bot.fp.rest.dto.FpGroupDTO;
import com.faendir.zachtronics.bot.fp.rest.dto.FpPuzzleDTO;
import com.faendir.zachtronics.bot.fp.rest.dto.FpRecordDTO;
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
@RequestMapping("/fp")
@RequiredArgsConstructor
public class FpController implements GameRestController<FpGroupDTO, FpPuzzleDTO, FpCategoryDTO, FpRecordDTO> {
    
    private final FpSolutionRepository repository;
    
    @Getter
    private final List<FpGroupDTO> groups = Arrays.stream(FpGroup.values()).map(FpGroupDTO::fromGroup).toList();

    @Getter
    private final List<FpPuzzleDTO> puzzles = Arrays.stream(FpPuzzle.values()).map(FpPuzzleDTO::fromPuzzle).toList();

    @Override
    public FpPuzzleDTO getPuzzle(@NotNull String puzzleId) {
        return FpPuzzleDTO.fromPuzzle(findPuzzle(puzzleId));
    }

    @Override
    @NotNull
    public List<FpPuzzleDTO> listPuzzlesByGroup(@NotNull String groupId) {
        FpGroup group = findGroup(groupId);
        return Arrays.stream(FpPuzzle.values()).filter(p -> p.getGroup() == group).map(FpPuzzleDTO::fromPuzzle).toList();
    }

    @Getter
    private final List<FpCategoryDTO> categories = Arrays.stream(FpCategory.values()).map(FpCategoryDTO::fromCategory).toList();

    @Override
    public FpCategoryDTO getCategory(@NotNull String categoryId) {
        return FpCategoryDTO.fromCategory(findCategory(categoryId));
    }

    @Override
    @NotNull
    public List<FpRecordDTO> listRecords(@NotNull String puzzleId, Boolean includeFrontier) {
        FpPuzzle puzzle = findPuzzle(puzzleId);
        return repository.findCategoryHolders(puzzle, includeFrontier != null && includeFrontier).stream()
                         .map(FpRecordDTO::fromCategoryRecord)
                         .toList();
    }

    @Override
    public FpRecordDTO getRecord(@NotNull String puzzleId, @NotNull String categoryId) {
        FpPuzzle puzzle = findPuzzle(puzzleId);
        FpCategory category = findCategory(categoryId);
        FpRecord record = repository.find(puzzle, category);
        if (record != null)
            return FpRecordDTO.fromCategoryRecord(new CategoryRecord<>(record, EnumSet.of(category)));
        else
            return null;
    }

    private static FpPuzzle findPuzzle(String puzzleId) {
        return Arrays.stream(FpPuzzle.values())
                     .filter(p -> p.name().equalsIgnoreCase(puzzleId))
                     .findFirst()
                     .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Puzzle " + puzzleId + " not found."));
    }

    private static FpCategory findCategory(String categoryId) {
        return Arrays.stream(FpCategory.values())
                     .filter(c -> c.name().equalsIgnoreCase(categoryId))
                     .findFirst()
                     .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category " + categoryId + " not found."));
    }
    
    private static FpGroup findGroup(String groupId) {
        return Arrays.stream(FpGroup.values())
                     .filter(g -> g.name().equalsIgnoreCase(groupId))
                     .findFirst()
                     .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group " + groupId + " not found."));
    }
}