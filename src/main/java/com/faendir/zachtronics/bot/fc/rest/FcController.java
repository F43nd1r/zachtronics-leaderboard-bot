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

package com.faendir.zachtronics.bot.fc.rest;

import com.faendir.zachtronics.bot.fc.model.FcCategory;
import com.faendir.zachtronics.bot.fc.model.FcGroup;
import com.faendir.zachtronics.bot.fc.model.FcPuzzle;
import com.faendir.zachtronics.bot.fc.model.FcRecord;
import com.faendir.zachtronics.bot.fc.repository.FcSolutionRepository;
import com.faendir.zachtronics.bot.fc.rest.dto.FcCategoryDTO;
import com.faendir.zachtronics.bot.fc.rest.dto.FcGroupDTO;
import com.faendir.zachtronics.bot.fc.rest.dto.FcPuzzleDTO;
import com.faendir.zachtronics.bot.fc.rest.dto.FcRecordDTO;
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
@RequestMapping("/fc")
@RequiredArgsConstructor
public class FcController implements GameRestController<FcGroupDTO, FcPuzzleDTO, FcCategoryDTO, FcRecordDTO> {
    
    private final FcSolutionRepository repository;
    
    @Getter
    private final List<FcGroupDTO> groups = Arrays.stream(FcGroup.values()).map(FcGroupDTO::fromGroup).toList();

    @Getter
    private final List<FcPuzzleDTO> puzzles = Arrays.stream(FcPuzzle.values()).map(FcPuzzleDTO::fromPuzzle).toList();

    @Override
    public FcPuzzleDTO getPuzzle(@NotNull String puzzleId) {
        return FcPuzzleDTO.fromPuzzle(findPuzzle(puzzleId));
    }

    @Override
    @NotNull
    public List<FcPuzzleDTO> listPuzzlesByGroup(@NotNull String groupId) {
        FcGroup group = findGroup(groupId);
        return Arrays.stream(FcPuzzle.values()).filter(p -> p.getGroup() == group).map(FcPuzzleDTO::fromPuzzle).toList();
    }

    @Getter
    private final List<FcCategoryDTO> categories = Arrays.stream(FcCategory.values()).map(FcCategoryDTO::fromCategory).toList();

    @Override
    public FcCategoryDTO getCategory(@NotNull String categoryId) {
        return FcCategoryDTO.fromCategory(findCategory(categoryId));
    }

    @Override
    @NotNull
    public List<FcRecordDTO> listRecords(@NotNull String puzzleId, Boolean includeFrontier) {
        FcPuzzle puzzle = findPuzzle(puzzleId);
        return repository.findCategoryHolders(puzzle, includeFrontier != null && includeFrontier).stream()
                         .map(FcRecordDTO::fromCategoryRecord)
                         .toList();
    }

    @Override
    public FcRecordDTO getRecord(@NotNull String puzzleId, @NotNull String categoryId) {
        FcPuzzle puzzle = findPuzzle(puzzleId);
        FcCategory category = findCategory(categoryId);
        FcRecord record = repository.find(puzzle, category);
        if (record != null)
            return FcRecordDTO.fromCategoryRecord(new CategoryRecord<>(record, EnumSet.of(category)));
        else
            return null;
    }

    private static FcPuzzle findPuzzle(String puzzleId) {
        return Arrays.stream(FcPuzzle.values())
                     .filter(p -> p.name().equalsIgnoreCase(puzzleId))
                     .findFirst()
                     .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Puzzle " + puzzleId + " not found."));
    }

    private static FcCategory findCategory(String categoryId) {
        return Arrays.stream(FcCategory.values())
                     .filter(c -> c.name().equalsIgnoreCase(categoryId))
                     .findFirst()
                     .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category " + categoryId + " not found."));
    }
    
    private static FcGroup findGroup(String groupId) {
        return Arrays.stream(FcGroup.values())
                     .filter(g -> g.name().equalsIgnoreCase(groupId))
                     .findFirst()
                     .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group " + groupId + " not found."));
    }
}