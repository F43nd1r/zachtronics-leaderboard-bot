/*
 * Copyright (c) 2021
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

package com.faendir.zachtronics.bot.sc.validator;

import com.faendir.discord4j.command.parse.SingleParseResult;
import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.ScScore;
import com.faendir.zachtronics.bot.sc.model.ScSolutionMetadata;
import com.faendir.zachtronics.bot.sc.model.ScSubmission;
import com.faendir.zachtronics.bot.validation.ValidationResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/** Wrapper for a schem package installed on the system */
public class SChem {

    private static final ObjectMapper objectMapper = new ObjectMapper().configure(
            DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

    /**
     * validates a possibly multi SpaceChem export
     *
     * @param export multiExport to check
     * @param bypassValidation only check it imports
     */
    @NotNull
    public static Collection<ValidationResult<ScSubmission>> validateMultiExport(@NotNull String export,
                                                                                 boolean bypassValidation) {
        int solutionsNumber = StringUtils.countMatches(export, "SOLUTION:");
        if (solutionsNumber > 50 && !bypassValidation) {
            throw new IllegalArgumentException(
                    "You can archive a maximum of 50 solutions at a time, you tried " + solutionsNumber);
        }

        return Arrays.stream(validate(export, bypassValidation))
                     .map(r -> validationResultFrom(r, bypassValidation))
                     .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @NotNull
    static ValidationResult<ScSubmission> validationResultFrom(@NotNull SChemResult result, boolean bypassValidation)
            throws SChemException {

        if (result.getLevelName() == null || result.getAuthor() == null || result.getCycles() == null)
            return new ValidationResult.Unparseable<>(result.getError());

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

        SingleParseResult<ScPuzzle> puzzleParseResult = ScPuzzle.parsePuzzle(result.getLevelName());
        if (puzzleParseResult instanceof SingleParseResult.Ambiguous && result.getResnetId() != null) {
            puzzleParseResult = ScPuzzle.parsePuzzle(result.getLevelName() +
                                                     Arrays.stream(result.getResnetId()).mapToObj(Integer::toString)
                                                           .collect(Collectors.joining("-", " (", ")")));
        }
        ScPuzzle puzzle = puzzleParseResult.orElseThrow();

        ScSubmission submission = new ScSolutionMetadata(puzzle, result.getAuthor(), score, result.getSolutionName())
                                     .extendToSubmission(null, result.getExport());

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
        return new ValidationResult.Valid<>(submission);
    }

    /**
     *
     * @param export the (possibly multi) export string
     * @param onlyImport if <tt>true</tt> we only check the solution(s) imports, not that they runs
     * @return results, arrays of size 1 are correctly generated
     * @throws SChemException if there is a communication error, solution errors are handled in the onject
     */
    @NotNull
    static SChemResult[] validate(@NotNull String export, boolean onlyImport) throws SChemException {
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
                throw new SChemException(new String(process.getErrorStream().readAllBytes()));
            }
            return objectMapper.readValue(result, SChemResult[].class);

        } catch (JsonProcessingException e) {
            throw new SChemException("Error in reading back results", e);
        } catch (IOException e) {
            throw new SChemException("Error in communicating with the SChem executable", e);
        } catch (InterruptedException e) {
            throw new SChemException("Thread was killed while waiting for SChem", e);
        }
    }
}
