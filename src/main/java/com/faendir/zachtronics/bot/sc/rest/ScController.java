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

package com.faendir.zachtronics.bot.sc.rest;

import com.faendir.zachtronics.bot.repository.CategoryRecord;
import com.faendir.zachtronics.bot.rest.GameRestController;
import com.faendir.zachtronics.bot.rest.dto.SubmitResultTypeKt;
import com.faendir.zachtronics.bot.sc.model.*;
import com.faendir.zachtronics.bot.sc.repository.ScSolutionRepository;
import com.faendir.zachtronics.bot.sc.rest.dto.*;
import com.faendir.zachtronics.bot.utils.UtilsKt;
import com.faendir.zachtronics.bot.validation.ValidationResult;
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
import java.util.*;

@RestController
@RequestMapping("/sc")
@RequiredArgsConstructor
public class ScController implements GameRestController<ScGroupDTO, ScPuzzleDTO, ScCategoryDTO, ScRecordDTO> {
    
    private final ScSolutionRepository repository;
    
    @Getter
    private final List<ScGroupDTO> groups = Arrays.stream(ScGroup.values()).map(ScGroupDTO::fromGroup).toList();

    @Getter
    private final List<ScPuzzleDTO> puzzles = Arrays.stream(ScPuzzle.values()).map(ScPuzzleDTO::fromPuzzle).toList();

    @Override
    public ScPuzzleDTO getPuzzle(@NotNull String puzzleId) {
        return ScPuzzleDTO.fromPuzzle(findPuzzle(puzzleId));
    }

    @Override
    @NotNull
    public List<ScPuzzleDTO> listPuzzlesByGroup(@NotNull String groupId) {
        ScGroup group = findGroup(groupId);
        return Arrays.stream(ScPuzzle.values()).filter(p -> p.getGroup() == group).map(ScPuzzleDTO::fromPuzzle).toList();
    }

    @Getter
    private final List<ScCategoryDTO> categories = Arrays.stream(ScCategory.values()).map(ScCategoryDTO::fromCategory).toList();

    @Override
    public ScCategoryDTO getCategory(@NotNull String categoryId) {
        return ScCategoryDTO.fromCategory(findCategory(categoryId));
    }

    @Override
    @NotNull
    public List<ScRecordDTO> listRecords(@NotNull String puzzleId, Boolean includeFrontier) {
        ScPuzzle puzzle = findPuzzle(puzzleId);
        return repository.findCategoryHolders(puzzle, includeFrontier != null && includeFrontier).stream()
                         .filter(cr -> cr.getRecord().getDataLink() != null)
                         .map(ScRecordDTO::fromCategoryRecord)
                         .toList();
    }

    @Override
    public ScRecordDTO getRecord(@NotNull String puzzleId, @NotNull String categoryId) {
        ScPuzzle puzzle = findPuzzle(puzzleId);
        ScCategory category = findCategory(categoryId);
        ScRecord record = repository.find(puzzle, category);
        if (record != null)
            return ScRecordDTO.fromCategoryRecord(new CategoryRecord<>(record, EnumSet.of(category)));
        else
            return null;
    }

    @PostMapping(path = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> submit(@NotNull @ModelAttribute ScSubmissionDTO submissionDTO) throws IOException {
        if (submissionDTO.getVideo() != null && !UtilsKt.isValidLink(submissionDTO.getVideo()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid video link");
        String export = new String(submissionDTO.getExport().getBytes());
        Collection<ValidationResult<ScSubmission>> submissions = ScSubmission.fromData(export, false, submissionDTO.getAuthor());

        return repository.submitAll(submissions).stream()
                         .map(r -> Map.of("result", SubmitResultTypeKt.toType(r), "data", r))
                         .toList();
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