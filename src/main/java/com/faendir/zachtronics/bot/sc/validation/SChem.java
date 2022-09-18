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

package com.faendir.zachtronics.bot.sc.validation;

import com.faendir.zachtronics.bot.sc.model.*;
import com.faendir.zachtronics.bot.validation.ValidationException;
import com.faendir.zachtronics.bot.validation.ValidationResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/** Wrapper for a schem package installed on the system */
public class SChem {

    private static final ObjectMapper objectMapper = new ObjectMapper().configure(
            DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

    /**
     * validates a possibly multi SpaceChem export
     * @param export multiExport to check
     * @param bypassValidation only check it imports
     * @param author author to override all imports
     */
    @NotNull
    public static Collection<ValidationResult<ScSubmission>> validateMultiExport(@NotNull String export, boolean bypassValidation,
                                                                                 @Nullable String author) {
        int solutionsNumber = StringUtils.countMatches(export, "SOLUTION:");
        if (solutionsNumber > 50 && !bypassValidation) {
            throw new IllegalArgumentException(
                    "You can archive a maximum of 50 solutions at a time, you tried " + solutionsNumber);
        }

        return Arrays.stream(validate(export, bypassValidation))
                     .map(r -> validationResultFrom(r, bypassValidation, author))
                     .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @NotNull
    static ValidationResult<ScSubmission> validationResultFrom(@NotNull SChemResult result, boolean bypassValidation, String author)
    throws ValidationException {

        if (result.getLevelName() == null || result.getAuthor() == null) {
            assert result.getError() != null;
            return new ValidationResult.Unparseable<>(result.getError());
        }
        if (result.getCycles() == null) {
            if (result.getError() != null)
                return new ValidationResult.Unparseable<>(result.getError());
            else // there is no associated error on the schem side
                return new ValidationResult.Unparseable<>("Missing expected cycles for \"" + result.getSolutionName() + "\"");
        }
        assert result.getExport() != null;

        // puzzle
        ScPuzzle puzzle;
        List<ScPuzzle> possiblePuzzles = ScPuzzle.findMatchingPuzzles(result.getLevelName());
        switch (possiblePuzzles.size()) {
            case 1 -> puzzle = possiblePuzzles.get(0);
            case 2 -> {  // resnet levels with the same name
                assert result.getResnetId() != null;
                String longName = result.getLevelName() + Arrays.stream(result.getResnetId())
                                                                .mapToObj(Integer::toString)
                                                                .collect(Collectors.joining("-", " (", ")"));
                puzzle = ScPuzzle.findUniqueMatchingPuzzle(longName);
            }
            default -> throw new IllegalStateException("I did not recognize the puzzle \"" + result.getLevelName() + "\".");
        }

        // author
        if (author == null)
            author = result.getAuthor();

        // score
        // we pull flags from the input
        boolean declaresBugged = false;
        boolean declaresPrecog = false;
        if (result.getSolutionName() != null) {
            Matcher m = ScSolutionMetadata.SOLUTION_NAME_REGEX.matcher(result.getSolutionName());
            if (!m.matches()) {
                return new ValidationResult.Unparseable<>(
                        "Invalid solution name: \"" + result.getSolutionName() + "\"");
            }
            declaresBugged = m.group("Bflag") != null;
            declaresPrecog = m.group("Pflag") != null;
        }

        ScScore score = new ScScore(result.getCycles(), result.getReactors(), result.getSymbols(), declaresBugged,
                                    declaresPrecog);

        // data
        String export = result.getExport().replaceFirst("\\s*$", "\n"); // ensure there is one and only one newline at the end

        // build submission
        ScSubmission submission = new ScSolutionMetadata(puzzle, author, score, result.getSolutionName())
                                     .extendToSubmission(null, export);

        if (!bypassValidation) {
            if (result.getError() != null)
                return new ValidationResult.Invalid<>(submission, result.getError());

            // check if the user is lying part 1, we know the score isn't bugged because SChem ran it
            if (declaresBugged) {
                return new ValidationResult.Invalid<>(submission,
                                                      "Submission was declared bugged, but SChem ran it successfully");
            }

            // check if the user is lying part 2, we can check SChem's precog opinion
            if (result.getPrecog() != null && declaresPrecog != result.getPrecog()) {
                return new ValidationResult.Invalid<>(submission, "Incoherent precognition flag, given " +
                                                                  "\"" + score.sepFlags("/") + "\"" +
                                                                  " but SChem wanted \"" +
                                                                  ScScore.sepFlags("/", false, result.getPrecog()) +
                                                                  "\"\n" + result.getPrecogExplanation());
            }
        }
        else {
            if (puzzle.getType() == ScType.BOSS_RANDOM) {
                return new ValidationResult.Invalid<>(submission,
                                                      "Boss levels with true randomness are not supported");
            }

            if (declaresPrecog && puzzle.isDeterministic()) {
                return new ValidationResult.Invalid<>(submission,
                                                      "Submission was declared precognitive, but the level is not random");
            }
        }

        return new ValidationResult.Valid<>(submission);
    }

    /**
     *
     * @param export the (possibly multi) export string
     * @param onlyImport if <tt>true</tt> we only check the solution(s) imports, not that they runs
     * @return results, arrays of size 1 are correctly generated
     * @throws ValidationException if there is a communication error, solution errors are handled in the onject
     */
    @NotNull
    static SChemResult[] validate(@NotNull String export, boolean onlyImport) throws ValidationException {
        ProcessBuilder builder = new ProcessBuilder();
        String runFlag = onlyImport ? "--no-run" : "--check-precog";
        builder.command("python3", "-m", "schem", "--json", "--export", runFlag);

        try {
            Process process = builder.start();
            process.getOutputStream().write(export.getBytes());
            process.getOutputStream().close();

            byte[] result = process.getInputStream().readAllBytes();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new ValidationException(new String(process.getErrorStream().readAllBytes()));
            }
            return objectMapper.readValue(result, SChemResult[].class);

        } catch (JsonProcessingException e) {
            throw new ValidationException("Error in reading back results", e);
        } catch (IOException e) {
            throw new ValidationException("Error in communicating with the SChem executable", e);
        } catch (InterruptedException e) {
            throw new ValidationException("Thread was killed while waiting for SChem", e);
        }
    }
}
