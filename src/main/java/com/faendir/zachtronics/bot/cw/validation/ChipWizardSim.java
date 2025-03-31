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

package com.faendir.zachtronics.bot.cw.validation;

import com.faendir.zachtronics.bot.cw.model.CwPuzzle;
import com.faendir.zachtronics.bot.cw.model.CwScore;
import com.faendir.zachtronics.bot.cw.model.CwSubmission;
import com.faendir.zachtronics.bot.validation.ValidationException;
import com.faendir.zachtronics.bot.validation.ValidationResult;
import com.faendir.zachtronics.bot.validation.ValidationUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

/** Wrapper for a chipwizard-sim module installed on the system */
public class ChipWizardSim {

    /**
     * validates a possibly multi FP data file
     * @param data content to check
     * @param author author to override all imports
     */
    @NotNull
    public static Collection<ValidationResult<CwSubmission>> validateMultiExport(@NotNull String data, @NotNull String author) {
        CwSimResult[] results = validate(data);
        if (results.length == 0)
            throw new ValidationException("No valid solution provided");
        return Arrays.stream(results)
                     .map(r -> validationResultFrom(r, author))
                     .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @NotNull
    static ValidationResult<CwSubmission> validationResultFrom(@NotNull CwSimResult result, @NotNull String author) throws
                                                                                                                  ValidationException {
        // puzzle
        CwPuzzle puzzle = Arrays.stream(CwPuzzle.values())
                                .filter(p -> p.getId() == result.getLevelId())
                                .findFirst()
                                .orElseThrow();

        // score
        CwScore score = new CwScore(result.getSiliconWidth(), result.getSiliconHeight(), result.getFootprint());

        // data
        String data = result.getSolution().replaceFirst("\\s*$", "\n"); // ensure there is one and only one newline at the end

        // build submission
        CwSubmission submission = new CwSubmission(puzzle, score, author, null, data);

        // check correctness
        if (!result.isCorrect())
            return new ValidationResult.Invalid<>(submission, "Solution is not correct");

        return new ValidationResult.Valid<>(submission);
    }

    /**
     *
     * @param data the (possibly multi) saves string
     * @return results, arrays of size 1 are correctly generated
     * @throws ValidationException if there is a communication error, solution errors are handled in the onject
     */
    @NotNull
    static CwSimResult[] validate(@NotNull String data) throws ValidationException {
        List<String> command = List.of("python3", "-m", "chipwizard_sim", "validate_all", "--json", "--include-solution", "-");
        return ValidationUtils.callValidator(CwSimResult[].class, data.getBytes(), command);
    }
}
