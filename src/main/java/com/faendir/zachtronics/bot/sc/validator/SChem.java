package com.faendir.zachtronics.bot.sc.validator;

import com.faendir.zachtronics.bot.sc.discord.ScPuzzleConverter;
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
import java.util.regex.Pattern;

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
        String[] contents = export.split("(?=SOLUTION:)");
        LinkedHashSet<ScSolution> result = new LinkedHashSet<>();
        StringBuilder exceptions = new StringBuilder();
        int line = 1;
        for (String content : contents) {
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
    static ScSolution validate(@NotNull String export) throws SChemException {
        SChemResult result = run(export);

        ScPuzzle puzzle;
        try {
            puzzle = ScPuzzleConverter.parsePuzzle(result.getLevelName());
        } catch (IllegalArgumentException e) {
            assert result.getResnetId() != null;
            puzzle = ScPuzzleConverter.parsePuzzle(
                    result.getLevelName() + " (" + result.getResnetId()[0] + "-" + result.getResnetId()[1] + "-" +
                    result.getResnetId()[2] + ")");
        }

        // we know the score isn't bugged because SChem ran it, we get the precog flag from the title
        boolean usesPrecog = !puzzle.isDeterministic() && result.getSolutionName() != null &&
                             result.getSolutionName().matches("^/P .+");
        ScScore score = new ScScore(result.getCycles(), result.getReactors(), result.getSymbols(), false,
                                    usesPrecog);


        String content = Pattern.compile("^SOLUTION:.+$", Pattern.MULTILINE).matcher(export).replaceFirst(
                String.format("SOLUTION:%s,Archiver,%d-%d-%d,%sArchived Solution", result.getLevelName(),
                              result.getCycles(), result.getReactors(), result.getSymbols(), usesPrecog ? "/P " : ""));
        return new ScSolution(puzzle, score, content);
    }

    static SChemResult run(@NotNull String export) throws SChemException {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("python3", "-m", "schem", "--json");

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
