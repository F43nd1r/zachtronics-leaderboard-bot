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

package com.faendir.zachtronics.bot.fc.validation;

import com.faendir.zachtronics.bot.fc.model.FcPuzzle;
import com.faendir.zachtronics.bot.fc.model.FcScore;
import com.faendir.zachtronics.bot.fc.model.FcSubmission;
import com.faendir.zachtronics.bot.validation.ValidationException;
import com.faendir.zachtronics.bot.validation.ValidationResult;
import com.faendir.zachtronics.bot.validation.ValidationUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

/** Wrapper for a foodcourt-sim module installed on the system */
public class FoodCourtSim {

    /**
     * validates a possibly multi FC data file
     * @param data content to check
     * @param author author to override all imports
     */
    @NotNull
    public static Collection<ValidationResult<FcSubmission>> validateMultiExport(@NotNull byte[] data, @NotNull String author) {
        FcSimResult[] results = validate(data);
        if (results.length == 0)
            throw new ValidationException("No valid solution provided");
        return Arrays.stream(results)
                     .map(r -> validationResultFrom(r, author))
                     .collect(Collectors.toCollection(LinkedHashSet::new));    }

    @NotNull
    static ValidationResult<FcSubmission> validationResultFrom(@NotNull FcSimResult result, @NotNull String author)
    throws ValidationException {
        // check the solution was parseable
        if (result.getErrorMessage() != null) {
            return new ValidationResult.Unparseable<>(result.getErrorMessage());
        }
        assert result.getLevelNumber() != null;

        // puzzle
        FcPuzzle puzzle = Arrays.stream(FcPuzzle.values())
                                .filter(p -> p.getNumber() == result.getLevelNumber())
                                .findFirst()
                                .orElseThrow();

        // score
        FcScore score = new FcScore(result.getMaxTime(), result.getCost(), result.getTotalTime(), result.getNumWires());

        // data
        byte[] data = Base64.getDecoder().decode(result.getSolution());

        // build submission
        FcSubmission submission = new FcSubmission(puzzle, score, author, null, data);

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
    static FcSimResult[] validate(@NotNull byte[] data) throws ValidationException {
        String[] command = {"python3", "-m", "foodcourt_sim", "simulate", "--json", "--include-solution", "-"};
        return ValidationUtils.callValidator(FcSimResult[].class, data, command);
    }
}
