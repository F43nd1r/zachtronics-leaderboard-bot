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

package com.faendir.zachtronics.bot.tis.rest;

import com.faendir.zachtronics.bot.repository.CategoryRecord;
import com.faendir.zachtronics.bot.rest.GameRestController;
import com.faendir.zachtronics.bot.tis.model.TISCategory;
import com.faendir.zachtronics.bot.tis.model.TISGroup;
import com.faendir.zachtronics.bot.tis.model.TISPuzzle;
import com.faendir.zachtronics.bot.tis.model.TISRecord;
import com.faendir.zachtronics.bot.tis.repository.TISSolutionRepository;
import com.faendir.zachtronics.bot.tis.rest.dto.TISCategoryDTO;
import com.faendir.zachtronics.bot.tis.rest.dto.TISGroupDTO;
import com.faendir.zachtronics.bot.tis.rest.dto.TISPuzzleDTO;
import com.faendir.zachtronics.bot.tis.rest.dto.TISRecordDTO;
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
@RequestMapping("/tis")
@RequiredArgsConstructor
public class TISController implements GameRestController<TISGroupDTO, TISPuzzleDTO, TISCategoryDTO, TISRecordDTO> {
    
    private final TISSolutionRepository repository;
    
    @Getter
    private final List<TISGroupDTO> groups = Arrays.stream(TISGroup.values()).map(TISGroupDTO::fromGroup).toList();

    @Getter
    private final List<TISPuzzleDTO> puzzles = Arrays.stream(TISPuzzle.values()).map(TISPuzzleDTO::fromPuzzle).toList();

    @Override
    public TISPuzzleDTO getPuzzle(@NotNull String puzzleId) {
        return TISPuzzleDTO.fromPuzzle(findPuzzle(puzzleId));
    }

    @Override
    @NotNull
    public List<TISPuzzleDTO> listPuzzlesByGroup(@NotNull String groupId) {
        TISGroup group = findGroup(groupId);
        return Arrays.stream(TISPuzzle.values()).filter(p -> p.getGroup() == group).map(TISPuzzleDTO::fromPuzzle).toList();
    }

    @Getter
    private final List<TISCategoryDTO> categories = Arrays.stream(TISCategory.values()).map(TISCategoryDTO::fromCategory).toList();

    @Override
    public TISCategoryDTO getCategory(@NotNull String categoryId) {
        return TISCategoryDTO.fromCategory(findCategory(categoryId));
    }

    @Override
    @NotNull
    public List<TISRecordDTO> listRecords(@NotNull String puzzleId, Boolean includeFrontier) {
        TISPuzzle puzzle = findPuzzle(puzzleId);
        return repository.findCategoryHolders(puzzle, includeFrontier != null && includeFrontier).stream()
                         .map(TISRecordDTO::fromCategoryRecord)
                         .toList();
    }

    @Override
    public TISRecordDTO getRecord(@NotNull String puzzleId, @NotNull String categoryId) {
        TISPuzzle puzzle = findPuzzle(puzzleId);
        TISCategory category = findCategory(categoryId);
        TISRecord record = repository.find(puzzle, category);
        if (record != null)
            return TISRecordDTO.fromCategoryRecord(new CategoryRecord<>(record, EnumSet.of(category)));
        else
            return null;
    }

    private static TISPuzzle findPuzzle(String puzzleId) {
        return Arrays.stream(TISPuzzle.values())
                     .filter(p -> p.getId().equalsIgnoreCase(puzzleId))
                     .findFirst()
                     .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Puzzle " + puzzleId + " not found."));
    }

    private static TISCategory findCategory(String categoryId) {
        return Arrays.stream(TISCategory.values())
                     .filter(c -> c.name().equalsIgnoreCase(categoryId))
                     .findFirst()
                     .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category " + categoryId + " not found."));
    }
    
    private static TISGroup findGroup(String groupId) {
        return Arrays.stream(TISGroup.values())
                     .filter(g -> g.name().equalsIgnoreCase(groupId))
                     .findFirst()
                     .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group " + groupId + " not found."));
    }
}