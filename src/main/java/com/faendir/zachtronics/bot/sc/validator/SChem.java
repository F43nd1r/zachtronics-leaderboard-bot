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

import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.ScScore;
import com.faendir.zachtronics.bot.sc.model.ScSolution;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
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
     * @return list of solutions, solutions that didn't validate are
     *         <tt>{{@link ScPuzzle#research_example_1}, {@link ScScore#INVALID_SCORE}, "reason SChem is kill"}</tt>
     */
    @NotNull
    public static List<ScSolution> validateMultiExport(@NotNull String export, ScPuzzle puzzle) {
        String[] contents = export.trim().split("(?=SOLUTION:)");
        if (contents.length > 50) {
            throw new IllegalArgumentException(
                    "You can archive a maximum of 50 solutions at a time, you tried " + contents.length);
        }
        LinkedHashSet<ScSolution> result = new LinkedHashSet<>();
        StringBuilder exceptions = new StringBuilder();
        int line = 1;
        for (String content : contents) {
            content = content.replaceFirst("\\s*$", "\n"); // ensure there is one and only one newline at the end
            try {
                result.add(validate(content));
            } catch (SChemException e) {
                try {
                    ScSolution solution = ScSolution.fromContentNoValidation(content, puzzle);
                    if (solution.getScore().isBugged()) {
                        /* we won't be able to validate this, because SChem will crash
                         * we trust the user didn't mess with us and pass the solution up
                         */
                        result.add(solution);
                        continue;
                    }
                }
                catch (IllegalArgumentException ignored) {
                    // solution import is not readable, we'll show the SChem exception
                }

                exceptions.append(line).append(": ").append(e.getMessage()).append('\n');
            }
            line++;
        }

        if (!exceptions.isEmpty()) {
            throw new IllegalArgumentException(exceptions.toString());
        }
        return Arrays.asList(result.toArray(new ScSolution[0]));
    }

    /**
     * validates a single SpaceChem export
     *
     * @param export **single** export to check
     * @return solution if validation succeeded
     * @throws SChemException if validation failed, reason is in message
     */
    @NotNull
    static ScSolution validate(@NotNull String export) throws SChemException {
        SChemResult result = run(export);

        ScPuzzle puzzle;
        try {
            puzzle = ScPuzzle.parsePuzzle(result.getLevelName());
        } catch (IllegalArgumentException e) {
            assert result.getResnetId() != null;
            puzzle = ScPuzzle.parsePuzzle(result.getLevelName() +
                                          Arrays.stream(result.getResnetId()).mapToObj(Integer::toString)
                                                .collect(Collectors.joining("-", " (", ")")));
        }

        Matcher m = ScSolution.SOLUTION_HEADER.matcher(export);
        if (!m.find())
            throw new SChemException("Invalid header");

        String commaSolName = "";
        if (result.getSolutionName() != null) {
            commaSolName = "," + result.getSolutionName();
            if (commaSolName.length() > 100) {
                commaSolName = commaSolName.substring(0, 100) + "..." + (commaSolName.startsWith("'") ? "'" : "");
            }
        }

        String content = m.replaceFirst("SOLUTION:${puzzle},${author},${cycles}-${reactors}-${symbols}" + commaSolName);

        ScScore score = new ScScore(result.getCycles(), result.getReactors(), result.getSymbols(), false,
                                    m.group("Pflag") != null);

        return new ScSolution(puzzle, score, content);
    }

    @NotNull
    static SChemResult run(@NotNull String export) throws SChemException {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("python3", "-m", "schem", "--json", "--check-precog", "--verbose");

        try {
            Process process = builder.start();
            process.getOutputStream().write(export.getBytes());
            process.getOutputStream().close();

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new SChemException(new String(process.getErrorStream().readAllBytes()));
            }

            SChemResult result = objectMapper.readValue(process.getInputStream(), SChemResult.class);

            boolean declaresBugged = false;
            boolean declaresPrecog = false;
            if (result.getSolutionName() != null) {
                Matcher m = ScSolution.SOLUTION_NAME_REGEX.matcher(result.getSolutionName());
                if (!m.matches()) {
                    throw new SChemException("Invalid solution name: \"" + result.getSolutionName() + "\"");
                }
                declaresBugged = m.group("Bflag") != null;
                declaresPrecog = m.group("Pflag") != null;
            }

            // check if the user is lying:
            // we know the score isn't bugged because SChem ran it and we can check SChem's precog opinion
            if (declaresBugged || (result.getPrecog() != null && declaresPrecog != result.getPrecog())) {
                String declaredScore = new ScScore(result.getCycles(), result.getReactors(), result.getSymbols(),
                                                   declaresBugged, declaresPrecog).toDisplayString();
                String schemScore = new ScScore(result.getCycles(), result.getReactors(), result.getSymbols(),
                                                false, Boolean.TRUE.equals(result.getPrecog())).toDisplayString();
                throw new SChemException("Incoherent solution flags, given " + declaredScore +
                                         " but SChem wanted " + schemScore + "\n" +
                                         "SChem reports the following precognition analysis:\n" +
                                         new String(process.getErrorStream().readAllBytes()).trim());
            }

            return result;

        } catch (JsonProcessingException e) {
            throw new SChemException("Error in reading back results", e);
        } catch (IOException e) {
            throw new SChemException("Error in communicating with the SChem executable", e);
        } catch (InterruptedException e) {
            throw new SChemException("Thread was killed while waiting for SChem", e);
        }
    }
}
