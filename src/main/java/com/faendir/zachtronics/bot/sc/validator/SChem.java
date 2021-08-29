package com.faendir.zachtronics.bot.sc.validator;

import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import com.faendir.zachtronics.bot.sc.model.ScScore;
import com.faendir.zachtronics.bot.sc.model.ScSolution;
import com.faendir.zachtronics.bot.sc.model.SpaceChem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/** Wrapper for a schem package installed on the system */
public class SChem {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * validates a possibly multi SpaceChem export
     *
     * @param export multiExport to check
     * @param userScore score to attach to one of the solutions to specify extra flags
     * @param puzzle to aid in puzzle resolution
     * @return list of solutions, solutions that didn't validate are
     *         <tt>{{@link ScPuzzle#research_example_1}, {@link ScScore#INVALID_SCORE}, "reason SChem is kill"}</tt>
     */
    @NotNull
    public static List<ScSolution> validateMultiExport(@NotNull String export, ScPuzzle puzzle, @Nullable ScScore userScore) {
        String[] contents = export.split("(?=SOLUTION:)");
        if (contents.length == 1 && userScore != null && userScore.isBugged()) {
            /* we won't be able to validate this, because SChem will crash
             * we trust the user didn't mess with us and pass the score to the non-validating handler
             */
            return Collections.singletonList(ScSolution.fromContentNoValidation(export, puzzle, userScore));
        }

        List<ScSolution> result = new ArrayList<>();
        StringBuilder exceptions = new StringBuilder();
        int line = 1;
        for (String content : contents) {
            try {
                result.add(validate(content, userScore));
            } catch (SChemException e) {
                exceptions.append(line).append(": ").append(e.getMessage()).append('\n');
            }
            line++;
        }

        if (!exceptions.isEmpty()) {
            throw new IllegalArgumentException(exceptions.toString());
        }
        return result;
    }

    /**
     * validates a single SpaceChem export
     *
     * @param export **single** export to check
     * @return solution if validation succeeded
     * @throws SChemException if validation failed, reason is in message
     */
    static ScSolution validate(@NotNull String export, @Nullable ScScore userScore) throws SChemException {
        SChemResult result = run(export);

        ScPuzzle puzzle;
        try {
            puzzle = SpaceChem.parsePuzzle(result.getLevelName());
        } catch (IllegalArgumentException e) {
            assert result.getResnetId() != null;
            puzzle = SpaceChem.parsePuzzle(
                    result.getLevelName() + " (" + result.getResnetId()[0] + "-" + result.getResnetId()[1] + "-" +
                    result.getResnetId()[2] + ")");
        }

        // we know the score isn't bugged because SChem ran it, but we have to assume precognition where possible
        ScScore score = new ScScore(result.getCycles(), result.getReactors(), result.getSymbols(), false,
                                    !puzzle.isDeterministic());
        if (userScore != null &&
            userScore.getCycles() == score.getCycles() &&
            userScore.getReactors() == score.getReactors() &&
            userScore.getSymbols() == score.getSymbols()) {
            // override
            score = userScore;
        }

        String content = Pattern.compile("^SOLUTION:.+$", Pattern.MULTILINE).matcher(export).replaceFirst(
                String.format("SOLUTION:%s,Archiver,%d-%d-%d,Archived Solution", result.getLevelName(),
                              result.getCycles(), result.getReactors(), result.getSymbols()));
        return new ScSolution(puzzle, score, content);
    }

    static SChemResult run(@NotNull String export) throws SChemException {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("python", "-m", "schem", "--json");

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
