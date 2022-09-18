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

package com.faendir.zachtronics.bot.fp.validation;

import com.faendir.zachtronics.bot.fp.model.FpPuzzle;
import com.faendir.zachtronics.bot.fp.model.FpScore;
import com.faendir.zachtronics.bot.fp.model.FpSubmission;
import com.faendir.zachtronics.bot.fp.model.FpType;
import com.faendir.zachtronics.bot.validation.ValidationException;
import com.faendir.zachtronics.bot.validation.ValidationResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

/** Wrapper for a xbpgh-sim module installed on the system */
public class XBPGHSim {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * validates a possibly multi FP data file
     * @param data content to check
     * @param author author to override all imports
     */
    @NotNull
    public static Collection<ValidationResult<FpSubmission>> validateMultiExport(@NotNull String data, @NotNull String author) {
        FpSimResult[] results = validate(data);
        if (results.length == 0)
            throw new ValidationException("No valid solution provided");
        return Arrays.stream(results)
                     .map(r -> validationResultFrom(r, author))
                     .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @NotNull
    static ValidationResult<FpSubmission> validationResultFrom(@NotNull FpSimResult result, @NotNull String author) throws ValidationException {
        // puzzle
        FpPuzzle puzzle = Arrays.stream(FpPuzzle.values())
                                .filter(p -> p.getId() == result.getLevelId())
                                .findFirst()
                                .orElseThrow();

        // score
        int frames = result.isStable() ? result.getNumFrames() : 13;
        FpScore score = new FpScore(result.getNumRules(), result.getNumRulesConditional(), frames, result.getNumWaste());

        // data
        String data = result.getSolution().replaceFirst("\\s*$", "\n"); // ensure there is one and only one newline at the end

        // build submission
        FpSubmission submission = new FpSubmission(puzzle, score, author, null, data);

        // ensure level is tracked
        if (puzzle.getType() == FpType.EDITOR)
            return new ValidationResult.Invalid<>(submission, "Editor levels are not supported");

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
    static FpSimResult[] validate(@NotNull String data) throws ValidationException {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("python3", "-m", "xbpgh_sim", "validate_all", "--json", "--include-solution", "-");

        try {
            Process process = builder.start();
            process.getOutputStream().write(data.getBytes());
            process.getOutputStream().close();

            byte[] result = process.getInputStream().readAllBytes();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new ValidationException(new String(process.getErrorStream().readAllBytes()));
            }
            return objectMapper.readValue(result, FpSimResult[].class);

        } catch (JsonProcessingException e) {
            throw new ValidationException("Error in reading back results", e);
        } catch (IOException e) {
            throw new ValidationException("Error in communicating with the xbpgh_sim executable", e);
        } catch (InterruptedException e) {
            throw new ValidationException("Thread was killed while waiting for xbpgh_sim", e);
        }
    }
}
