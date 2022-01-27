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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

@RestController
@RequestMapping("/sz")
@RequiredArgsConstructor
public class SzController {
    
    private final SzSolutionRepository repository;
    
    @Getter(onMethod_ = {@GetMapping(path = "/groups", produces = MediaType.APPLICATION_JSON_VALUE)})
    private final List<SzGroupDTO> groups = Arrays.stream(SzGroup.values()).map(SzGroupDTO::fromGroup).toList();

    @Getter(onMethod_ = {@GetMapping(path = "/puzzles", produces = MediaType.APPLICATION_JSON_VALUE)})
    private final List<SzPuzzleDTO> puzzles = Arrays.stream(SzPuzzle.values()).map(SzPuzzleDTO::fromPuzzle).toList();

    @GetMapping(path = "/puzzle/{puzzleId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public SzPuzzleDTO getPuzzle(@PathVariable String puzzleId) { return SzPuzzleDTO.fromPuzzle(findPuzzle(puzzleId)); }

    @GetMapping(path = "/group/{groupId}/puzzles", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<SzPuzzleDTO> listPuzzlesByGroup(@PathVariable String groupId) {
        SzGroup group = findGroup(groupId);
        return Arrays.stream(SzPuzzle.values()).filter(p -> p.getGroup() == group).map(SzPuzzleDTO::fromPuzzle).toList();
    }

    @Getter(onMethod_ = {@GetMapping(path = "/categories", produces = MediaType.APPLICATION_JSON_VALUE)})
    private final List<SzCategoryDTO> categories = Arrays.stream(SzCategory.values()).map(SzCategoryDTO::fromCategory).toList();

    @GetMapping(path = "/category/{categoryId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public SzCategoryDTO getCategory(@PathVariable String categoryId) {
        return SzCategoryDTO.fromCategory(findCategory(categoryId));
    }

    @GetMapping(path = "/puzzle/{puzzleId}/records", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<SzRecordDTO> listRecords(@PathVariable String puzzleId, @RequestParam(required = false) Boolean includeFrontier) {
        SzPuzzle puzzle = findPuzzle(puzzleId);
        return repository.findCategoryHolders(puzzle, includeFrontier != null && includeFrontier).stream()
                         .map(SzRecordDTO::fromCategoryRecord)
                         .toList();
    }

    @GetMapping(path = "/puzzle/{puzzleId}/category/{categoryId}/record", produces = MediaType.APPLICATION_JSON_VALUE)
    public SzRecordDTO getRecord(@PathVariable String puzzleId, @PathVariable String categoryId) {
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