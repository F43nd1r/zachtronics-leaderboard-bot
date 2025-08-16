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

package com.faendir.zachtronics.bot.tis.rest;

import com.faendir.zachtronics.bot.repository.CategoryRecord;
import com.faendir.zachtronics.bot.repository.SubmitResult;
import com.faendir.zachtronics.bot.rest.GameRestController;
import com.faendir.zachtronics.bot.rest.dto.CategoryDTO;
import com.faendir.zachtronics.bot.rest.dto.GroupDTO;
import com.faendir.zachtronics.bot.rest.dto.SubmitResultTypeKt;
import com.faendir.zachtronics.bot.tis.model.*;
import com.faendir.zachtronics.bot.tis.repository.TISSolutionRepository;
import com.faendir.zachtronics.bot.tis.rest.dto.TISPuzzleDTO;
import com.faendir.zachtronics.bot.tis.rest.dto.TISRecordDTO;
import com.faendir.zachtronics.bot.tis.rest.dto.TISSubmissionDTO;
import com.faendir.zachtronics.bot.utils.UtilsKt;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tis")
@RequiredArgsConstructor
public class TISController implements GameRestController<GroupDTO, TISPuzzleDTO, CategoryDTO, TISRecordDTO> {
    
    private final TISSolutionRepository repository;
    
    @Getter
    private final List<GroupDTO> groups = Arrays.stream(TISGroup.values()).map(GroupDTO::fromGroup).toList();

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
    private final List<CategoryDTO> categories = Arrays.stream(TISCategory.values()).map(CategoryDTO::fromCategory).toList();

    @Override
    public CategoryDTO getCategory(@NotNull String categoryId) {
        return CategoryDTO.fromCategory(findCategory(categoryId));
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

    @PostMapping(path = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Object submit(@NotNull @ModelAttribute TISSubmissionDTO submissionDTO) throws IOException {
        if (submissionDTO.getImage() != null && !UtilsKt.isValidLink(submissionDTO.getImage()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid image link");
        TISPuzzle puzzle = findPuzzle(submissionDTO.getPuzzleId());
        String data = new String(submissionDTO.getData().getBytes());
        TISSubmission submission = TISSubmission.fromData(data, puzzle, submissionDTO.getAuthor(), submissionDTO.getImage());

        SubmitResult<TISRecord, TISCategory> result = repository.submit(submission);
        return Map.of("result", SubmitResultTypeKt.toType(result), "data", result);
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