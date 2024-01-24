/*
 * Copyright (c) 2024
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

package com.faendir.zachtronics.bot.inf.validation;

import com.faendir.zachtronics.bot.inf.model.*;
import com.faendir.zachtronics.bot.validation.ValidationResult;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A general solution file is of the form:
 * <pre>
 * Best.1-1.Blocks = 44
 * Best.1-1.Cycles = 44
 * Best.1-1.Footprint = 47
 * InputRate.1-1.0 = 1
 * InputRate.1-1.1 = 1
 * Last.1-1.0.Blocks = 44
 * Last.1-1.0.Cycles = 44
 * Last.1-1.0.Footprint = 47
 * Last.1-1.1.Blocks = 25
 * Last.1-1.1.Cycles = 58
 * Last.1-1.1.Footprint = 76
 * Solution.1-1.0 = AwAAAAAAAAA=
 * Solution.1-1.1 = AwAAAAAAAAA=
 * </pre>
 * the solution is a base64 encoded string represented by {@link IfSave}<br><br>
 *
 * On top of that we support the 2 extensions:
 * <pre>
 * Last.1-1.0.Flags = [/][O][G][F]
 * Author.1-1.0 = someGuy
 * </pre>
 * to set extra fields that aren't in the savefile<br><br>
 *
 * The leaderboard stores just the minimal information needed:
 * <pre>
 * InputRate.1-1.1 = 1
 * Solution.1-1.1 = AwAAAAAAAAA=
 * </pre>
 */
public class IfValidator {

    public static Collection<ValidationResult<IfSubmission>> validateSavefile(@NotNull String data, @NotNull String author, IfScore score) {
        Map<String, IfSolutionInfo> infosByIdSlot = new LinkedHashMap<>(); // 1-1.0 -> {...}
        Function<String[], IfSolutionInfo> find =
            keyParts -> infosByIdSlot.computeIfAbsent(keyParts[1] + "." + keyParts[2], p -> new IfSolutionInfo());

        for (String line: Pattern.compile("\r?\n").split(data)) {
            if (!line.contains("=")) continue;
            String[] kv = line.split("\\s*=\\s*", 2);
            String[] keyParts = kv[0].split("\\.");
            String value = kv[1];
            if (value.isBlank()) {
                // empty flags are different from null flags
                if (keyParts[0].equals("Last") && keyParts.length == 4 && keyParts[3].equals("Flags"))
                    find.apply(keyParts).loadFlags("");
            }
            else if (keyParts[0].equals("InputRate") && keyParts.length == 3) // InputRate.1-1.0 = 1
                find.apply(keyParts).setInputRate(Integer.parseInt(value));
            else if (keyParts[0].equals("Solution") && keyParts.length == 3) // Solution.1-1.0 = AwAAAAAAAAA=
                find.apply(keyParts).setSolution(value);
            else if (keyParts[0].equals("Author") && keyParts.length == 3) // Author.1-1.0 = someGuy
                find.apply(keyParts).setAuthor(value);
            else if (keyParts[0].equals("Last") && keyParts.length == 4) {
                switch (keyParts[3]) {
                    case "Blocks" -> // Last.1-1.0.Blocks = 44
                        find.apply(keyParts).setBlocks(Integer.parseInt(value));
                    case "Cycles" -> // Last.1-1.0.Cycles = 44
                        find.apply(keyParts).setCycles(Integer.parseInt(value));
                    case "Footprint" -> // Last.1-1.0.Footprint = 47
                        find.apply(keyParts).setFootprint(Integer.parseInt(value));
                    case "Flags" -> // Last.1-1.0.Flags = F
                        find.apply(keyParts).loadFlags(value);
                }
            }
        }
        return infosByIdSlot.entrySet()
                            .stream()
                            .filter(e -> e.getKey().matches("1?\\d-\\db?\\.\\d")) // main game puzzles only
                            .map(e -> validateOne(e.getKey(), e.getValue(), author, score))
                            .toList();
    }

    @NotNull
    private static ValidationResult<IfSubmission> validateOne(@NotNull String idSlot, @NotNull IfSolutionInfo info, String author,
                                                              IfScore score) {
        if (!(info.hasData() && (info.hasScore() || score != null)))
            return new ValidationResult.Unparseable<>("Incomplete data for idSlot: " + idSlot);
        IfSave save;
        try {
            save = IfSave.unmarshal(info.getSolution());
        }
        catch (Exception e) {
            return new ValidationResult.Unparseable<>("Unparseable solution string for idSlot: " + idSlot);
        }
        String id = idSlot.replaceFirst("\\.\\d$", "");
        IfPuzzle puzzle = Arrays.stream(IfPuzzle.values())
                                .filter(p -> p.getId().equals(id))
                                .findFirst().orElse(null);
        if (puzzle == null)
            return new ValidationResult.Unparseable<>("Unknown puzzle: " + id);
        // if we have no score we load it from the file, by extending reasonable trust to it
        if (score == null) {
            score = new IfScore(info.getCycles(), info.getFootprint(), info.getBlocks(),
                                info.isOutOfBounds(), info.usesGRA() && couldHaveGRA(save, puzzle),
                                info.isFinite() && puzzle.getType() == IfType.STANDARD); // boss doesn't track finite
        }
        if (info.getAuthor() != null) // prefer a custom savefile-specified author
            author = info.getAuthor();
        String leaderboardData = String.format("""
                                               InputRate.%s.0 = %d
                                               Solution.%s.0 = %s
                                               """, id, info.getInputRate(), id, info.getSolution());
        // we use a mutable list, as we could fill it later with display links if we have a single valid submission
        IfSubmission submission = new IfSubmission(puzzle, score, author, new ArrayList<>(), leaderboardData);

        // each level has 10 outputs, plus one cycle of travel
        if (score.getCycles() <= 10)
            return new ValidationResult.Invalid<>(submission, "Cycles too low: " + score.getCycles());

        int blockScore = save.blockScore();
        if (blockScore != score.getBlocks())
            return new ValidationResult.Invalid<>(submission,
                                                  "Solution has " + blockScore + " blocks, score has " + score.getBlocks());
        int footprintBound = save.footprintLowerBound();
        if (footprintBound > score.getFootprint())
            return new ValidationResult.Invalid<>(submission,
                                                  "Solution has at least " + footprintBound + " footprint, score has " +
                                                  score.getFootprint());
        if (!couldHaveGRA(save, puzzle) && score.usesGRA())
            return new ValidationResult.Invalid<>(submission,
                                                  "Score declares to have GRA, but prerequisite blocks have not been found");

        // the boss level:
        // * has manual toggles, which we disallow
        // * finite by design (with a typically hardcoded solution)
        // * forced to have InputRate = 8
        if (puzzle.getType() == IfType.BOSS) {
            if (save.hasManualToggles())
                return new ValidationResult.Invalid<>(submission, "Manual Toggles are not allowed");
            if (score.isFinite())
                return new ValidationResult.Invalid<>(submission, "Finiteness is not tracked in the boss level");
            if (info.getInputRate() != 8)
                return new ValidationResult.Invalid<>(submission, "Invalid InputRate in the boss level: " + info.getInputRate());
        }

        return new ValidationResult.Valid<>(submission);
    }

    /**
     * Checks if the save&level have a rotator, a welder and an (eviscerator or laser or teleporter),
     * which are prerequisites to realize GRA<br>
     * Giant Rotating Arms (GRA for friends) are created by:
     * <li>welding input blocks to factory blocks</li>
     * <li>rotating the whole assembly</li>
     * <li>detaching the input blocks by teleporter or by eviscerating/lasering some connecting input blocks</li>
     */
    public static boolean couldHaveGRA(@NotNull IfSave save, @NotNull IfPuzzle puzzle) {
        Set<Short> types = Arrays.stream(save.getBlocks())
                                 .map(IfBlock::getType)
                                 .collect(Collectors.toSet());
        return (types.contains(IfBlockType.ROTATOR_CW) || types.contains(IfBlockType.ROTATOR_CCW)) &&
               (types.contains(IfBlockType.WELDER_A) || types.contains(IfBlockType.WELDER_B)) &&
               (types.contains(IfBlockType.EVISCERATOR) || types.contains(IfBlockType.LASER) ||
                puzzle.getGroup() == IfGroup.ZONE_7); // only and all `The Heist` puzzles have teleporters
    }
}
