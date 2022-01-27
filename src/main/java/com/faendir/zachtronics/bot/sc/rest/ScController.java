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

package com.faendir.zachtronics.bot.sc.rest;

import com.faendir.zachtronics.bot.repository.CategoryRecord;
import com.faendir.zachtronics.bot.sc.model.ScCategory;
import com.faendir.zachtronics.bot.sc.model.ScGroup;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.ScRecord;
import com.faendir.zachtronics.bot.sc.repository.ScSolutionRepository;
import com.faendir.zachtronics.bot.sc.rest.dto.ScCategoryDTO;
import com.faendir.zachtronics.bot.sc.rest.dto.ScGroupDTO;
import com.faendir.zachtronics.bot.sc.rest.dto.ScPuzzleDTO;
import com.faendir.zachtronics.bot.sc.rest.dto.ScRecordDTO;
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
@RequestMapping("/sc")
@RequiredArgsConstructor
public class ScController {
    
    private final ScSolutionRepository repository;
    
    @Getter(onMethod_ = {@GetMapping(path = "/groups", produces = MediaType.APPLICATION_JSON_VALUE)})
    private final List<ScGroupDTO> groups = Arrays.stream(ScGroup.values()).map(ScGroupDTO::fromGroup).toList();

    @Getter(onMethod_ = {@GetMapping(path = "/puzzles", produces = MediaType.APPLICATION_JSON_VALUE)})
    private final List<ScPuzzleDTO> puzzles = Arrays.stream(ScPuzzle.values()).map(ScPuzzleDTO::fromPuzzle).toList();

    @GetMapping(path = "/puzzle/{puzzleId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ScPuzzleDTO getPuzzle(@PathVariable String puzzleId) { return ScPuzzleDTO.fromPuzzle(findPuzzle(puzzleId)); }

    @GetMapping(path = "/group/{groupId}/puzzles", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ScPuzzleDTO> listPuzzlesByGroup(@PathVariable String groupId) {
        ScGroup group = findGroup(groupId);
        return Arrays.stream(ScPuzzle.values()).filter(p -> p.getGroup() == group).map(ScPuzzleDTO::fromPuzzle).toList();
    }

    @Getter(onMethod_ = {@GetMapping(path = "/categories", produces = MediaType.APPLICATION_JSON_VALUE)})
    private final List<ScCategoryDTO> categories = Arrays.stream(ScCategory.values()).map(ScCategoryDTO::fromCategory).toList();

    @GetMapping(path = "/category/{categoryId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ScCategoryDTO getCategory(@PathVariable String categoryId) {
        return ScCategoryDTO.fromCategory(findCategory(categoryId));
    }

    @GetMapping(path = "/puzzle/{puzzleId}/records", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ScRecordDTO> listRecords(@PathVariable String puzzleId, @RequestParam(required = false) Boolean includeFrontier) {
        ScPuzzle puzzle = findPuzzle(puzzleId);
        return repository.findCategoryHolders(puzzle, includeFrontier != null && includeFrontier).stream()
                         .map(ScRecordDTO::fromCategoryRecord)
                         .toList();
    }

    @GetMapping(path = "/puzzle/{puzzleId}/category/{categoryId}/record", produces = MediaType.APPLICATION_JSON_VALUE)
    public ScRecordDTO getRecord(@PathVariable String puzzleId, @PathVariable String categoryId) {
        ScPuzzle puzzle = findPuzzle(puzzleId);
        ScCategory category = findCategory(categoryId);
        ScRecord record = repository.find(puzzle, category);
        if (record != null)
            return ScRecordDTO.fromCategoryRecord(new CategoryRecord<>(record, EnumSet.of(category)));
        else
            return null;
    }

    private static ScPuzzle findPuzzle(String puzzleId) {
        return Arrays.stream(ScPuzzle.values())
                     .filter(p -> p.name().equalsIgnoreCase(puzzleId))
                     .findFirst()
                     .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Puzzle " + puzzleId + " not found."));
    }

    private static ScCategory findCategory(String categoryId) {
        return Arrays.stream(ScCategory.values())
                     .filter(c -> c.name().equalsIgnoreCase(categoryId))
                     .findFirst()
                     .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category " + categoryId + " not found."));
    }
    
    private static ScGroup findGroup(String groupId) {
        return Arrays.stream(ScGroup.values())
                     .filter(g -> g.name().equalsIgnoreCase(groupId))
                     .findFirst()
                     .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group " + groupId + " not found."));
    }
}