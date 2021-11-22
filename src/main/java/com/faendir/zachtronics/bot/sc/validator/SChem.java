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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/** Wrapper for a schem package installed on the system */
public class SChem {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * validates a possibly multi SpaceChem export
     *
     * @param export multiExport to check
     * @param puzzle to aid in puzzle resolution
     * @param bypassValidation skip validation and checks entirely, just parse everything
     */
    @NotNull
    public static Collection<ValidationResult<ScSubmission>> validateMultiExport(@NotNull String export,
                                                                                 ScPuzzle puzzle,
                                                                                 boolean bypassValidation) {
        String[] solutions = export.trim().split("(?=SOLUTION:)");
        if (solutions.length > 50 && !bypassValidation) {
            throw new IllegalArgumentException(
                    "You can archive a maximum of 50 solutions at a time, you tried " + solutions.length);
        }
        LinkedHashSet<ValidationResult<ScSubmission>> results = new LinkedHashSet<>();
        for (String data : solutions) {
            data = data.replaceFirst("\\s*$", "\n"); // ensure there is one and only one newline at the end
            ValidationResult<ScSubmission> result;
            if (bypassValidation) {
                // TODO pass them under SChem import-only
                try {
                    result = new ValidationResult.Valid<>(ScSubmission.fromDataNoValidation(data, puzzle));
                } catch (IllegalArgumentException e) {
                    result = new ValidationResult.Unparseable<>(e.getMessage());
                }
            }
            else {
                result = validate(data);
            }
            results.add(result);
        }

        return results;
    }

    /**
     * validates a single SpaceChem export
     *
     * @param data **single** export to check
     */
    @NotNull
    static ValidationResult<ScSubmission> validate(@NotNull String data) throws SChemException {
        SChemResult result;
        try {
            result = run(data);
        }
        catch (SChemException e) {
            return new ValidationResult.Unparseable<>(e.getMessage());
        }

        ScScore score = new ScScore(result.getCycles(), result.getReactors(), result.getSymbols(), false,
                                    result.getPrecog() != null && result.getPrecog());

        SingleParseResult<ScPuzzle> puzzleParseResult = ScPuzzle.parsePuzzle(result.getLevelName());
        if (puzzleParseResult instanceof SingleParseResult.Ambiguous && result.getResnetId() != null) {
            puzzleParseResult = ScPuzzle.parsePuzzle(result.getLevelName() +
                                                     Arrays.stream(result.getResnetId()).mapToObj(Integer::toString)
                                                           .collect(Collectors.joining("-", " (", ")")));
        }
        ScPuzzle puzzle = puzzleParseResult.orElseThrow();

        ScSubmission submission = new ScSolutionMetadata(puzzle, result.getAuthor(), score,
                                                         result.getSolutionName()).extendToSubmission(null, data);

        if (result.getError() != null)
            return new ValidationResult.Invalid<>(submission, result.getError());

        boolean declaresBugged = false;
        boolean declaresPrecog = false;
        if (result.getSolutionName() != null) {
            Matcher m = ScSolutionMetadata.SOLUTION_NAME_REGEX.matcher(result.getSolutionName());
            if (!m.matches()) {
                return new ValidationResult.Unparseable<>("Invalid solution name: \"" + result.getSolutionName() + "\"");
            }
            declaresBugged = m.group("Bflag") != null;
            declaresPrecog = m.group("Pflag") != null;
        }

        // check if the user is lying:
        // we know the score isn't bugged because SChem ran it and we can check SChem's precog opinion
        if (declaresBugged || (result.getPrecog() != null && declaresPrecog != result.getPrecog())) {
            return new ValidationResult.Invalid<>(submission,
                                                  "Incoherent solution flags, given " +
                                                  "\"" + ScScore.sepFlags("/", declaresBugged, declaresPrecog) + "\"" +
                                                  " but SChem wanted \"" + score.sepFlags("/") + "\"\n" +
                                                  "SChem reports the following precognition analysis:\n" +
                                                  result.getPrecogExplanation());
        }

        return new ValidationResult.Valid<>(submission);
    }

    @NotNull
    static SChemResult run(@NotNull String export) throws SChemException {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("python3", "-m", "schem", "--json", "--check-precog");

        try {
            Process process = builder.start();
            process.getOutputStream().write(export.getBytes());
            process.getOutputStream().close();

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new SChemException(new String(process.getErrorStream().readAllBytes()));
            }

            return objectMapper.readValue(process.getInputStream(), SChemResult.class);

        } catch (JsonProcessingException e) {
            throw new SChemException("Error in reading back results", e);
        } catch (IOException e) {
            throw new SChemException("Error in communicating with the SChem executable", e);
        } catch (InterruptedException e) {
            throw new SChemException("Thread was killed while waiting for SChem", e);
        }
    }
}
