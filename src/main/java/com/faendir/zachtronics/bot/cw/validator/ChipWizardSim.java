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

package com.faendir.zachtronics.bot.cw.validator;

import com.faendir.zachtronics.bot.cw.model.CwPuzzle;
import com.faendir.zachtronics.bot.cw.model.CwScore;
import com.faendir.zachtronics.bot.cw.model.CwSubmission;
import com.faendir.zachtronics.bot.validation.ValidationResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

/** Wrapper for a chipwizard-sim module installed on the system */
public class ChipWizardSim {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * validates a possibly multi FP data file
     * @param data content to check
     * @param author author to override all imports
     */
    @NotNull
    public static Collection<ValidationResult<CwSubmission>> validateMultiExport(@NotNull String data, @NotNull String author) {
        SimResult[] results = validate(data);
        if (results.length == 0)
            throw new ChipWizardSimException("No valid solution provided");
        return Arrays.stream(results)
                     .map(r -> validationResultFrom(r, author))
                     .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @NotNull
    static ValidationResult<CwSubmission> validationResultFrom(@NotNull SimResult result, @NotNull String author) throws
                                                                                                                  ChipWizardSimException {
        // puzzle
        CwPuzzle puzzle = Arrays.stream(CwPuzzle.values())
                                .filter(p -> p.getId() == result.getLevelId())
                                .findFirst()
                                .orElseThrow();

        // score
        CwScore score = new CwScore(result.getSiliconWidth(), result.getSiliconHeight(), result.getFootprint());

        // build submission
        CwSubmission submission = new CwSubmission(puzzle, score, author, null, result.getSolution());

        // check correctness
        if (!result.isCorrect())
            return new ValidationResult.Invalid<>(submission, "Solution is not correct");

        return new ValidationResult.Valid<>(submission);
    }

    /**
     *
     * @param data the (possibly multi) saves string
     * @return results, arrays of size 1 are correctly generated
     * @throws ChipWizardSimException if there is a communication error, solution errors are handled in the onject
     */
    @NotNull
    static SimResult[] validate(@NotNull String data) throws ChipWizardSimException {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("python3", "-m", "chipwizard_sim", "validate_all", "--json", "--include-solution", "-");

        try {
            Process process = builder.start();
            process.getOutputStream().write(data.getBytes());
            process.getOutputStream().close();

            byte[] result = process.getInputStream().readAllBytes();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new ChipWizardSimException(new String(process.getErrorStream().readAllBytes()));
            }
            return objectMapper.readValue(result, SimResult[].class);

        } catch (JsonProcessingException e) {
            throw new ChipWizardSimException("Error in reading back results", e);
        } catch (IOException e) {
            throw new ChipWizardSimException("Error in communicating with the chipwizard_sim executable", e);
        } catch (InterruptedException e) {
            throw new ChipWizardSimException("Thread was killed while waiting for chipwizard_sim", e);
        }
    }
}
