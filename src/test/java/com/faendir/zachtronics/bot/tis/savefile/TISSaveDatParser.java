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

package com.faendir.zachtronics.bot.tis.savefile;

import com.faendir.zachtronics.bot.tis.model.TISPuzzle;
import com.faendir.zachtronics.bot.tis.model.TISScore;
import com.faendir.zachtronics.bot.tis.model.TISSubmission;
import com.faendir.zachtronics.bot.validation.ValidationException;
import com.faendir.zachtronics.bot.validation.ValidationResult;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A general save.dat file is of the form:
 * <pre>
 * Last.00150.0.Cycles = 83
 * Last.00150.0.Instructions = 8
 * Last.00150.0.Nodes = 8
 * Last.00150.1.Cycles = 2002115
 * Last.00150.1.Instructions = 19
 * Last.00150.1.Nodes = 8
 * </pre>
 * solutions are in separate files called <tt>00150.$i.txt</tt><br><br>
 *
 * On top of that we support the extension:
 * <pre>
 * Last.00150.0.Flags = [/][A][C]
 * </pre>
 * to set flags that aren't in the save.dat<br><br>
 *
 * The leaderboard only needs metadata from here, the real solves are in the separate files
 */
public class TISSaveDatParser {
    private static final Pattern LINE_REGEX = Pattern.compile("Last\\.(?<idSlot>.+)\\.(?<part>.+?)\\s*=\\s*(?<value>.*)");

    /**
     * @param folder we assume that we find <tt>$/save.dat</tt> and <tt>$/save/*.txt</tt>
     */
    public static Collection<ValidationResult<TISSubmission>> validateSave(@NotNull Path folder, @NotNull String author,
                                                                           boolean trustSave) throws IOException {
        String data = Files.readString(folder.resolve("save.dat"));

        Map<String, TISSolutionInfo> infosByIdSlot = new LinkedHashMap<>(); // 00150.0 -> {...}
        Function<Matcher, TISSolutionInfo> find =
            m -> infosByIdSlot.computeIfAbsent(m.group("idSlot"), p -> new TISSolutionInfo());

        for (String line: Pattern.compile("\r?\n").split(data)) {
            Matcher m = LINE_REGEX.matcher(line);
            if (!m.matches()) continue;
            String value = m.group("value");
            switch (m.group("part")) {
                case "Cycles" -> // Last.00150.0.Cycles = 83
                    find.apply(m).setCycles(Integer.parseInt(value));
                case "Instructions" -> // Last.00150.0.Instructions = 8
                    find.apply(m).setInstructions(Integer.parseInt(value));
                case "Nodes" -> // Last.00150.1.Nodes = 8
                    find.apply(m).setNodes(Integer.parseInt(value));
                case "Flags" -> // Last.00150.1.Flags = A
                    find.apply(m).setFlags(value);
            }
        }
        return infosByIdSlot.entrySet()
                            .stream()
                            .filter(e -> !e.getKey().startsWith("SPEC")) // ignore custom puzzles
                            .map(e -> validateOne(e.getKey(), e.getValue(), author, folder.resolve("save"), trustSave))
                            .toList();
    }

    private static final Pattern FLAGS_REGEX = Pattern.compile("/?" + TISScore.FLAGS_REGEX);

    @NotNull
    private static ValidationResult<TISSubmission> validateOne(@NotNull String idSlot, @NotNull TISSolutionInfo info, String author,
                                                               Path savesPath, boolean trustSave) {
        if (!info.hasScore())
            return new ValidationResult.Unparseable<>("Incomplete data for idSlot: " + idSlot);

        String id = idSlot.replaceFirst("\\.\\d$", "");
        TISPuzzle puzzle = Arrays.stream(TISPuzzle.values())
                                 .filter(p -> p.getId().equals(id))
                                 .findFirst().orElse(null);
        if (puzzle == null)
            return new ValidationResult.Unparseable<>("Unknown puzzle: " + id);

        boolean achievement;
        boolean cheating;
        if (info.getFlags() == null) {
            if (trustSave) {
                achievement = false;
                cheating = info.getCycles() < puzzle.getLegitCyclesBound() || info.getNodes() < puzzle.getLegitNodesBound();
            }
            else
                return new ValidationResult.Unparseable<>("Missing score flags for idSlot: " + idSlot);
        }
        else {
            Matcher m = FLAGS_REGEX.matcher(info.getFlags());
            if (m.matches()) {
                achievement = m.group("Aflag") != null;
                cheating = m.group("Cflag") != null;
            }
            else
                return new ValidationResult.Unparseable<>("Invalid score flags for idSlot: " + idSlot + " flags: " + info.getFlags());
        }
        TISScore score = new TISScore(info.getCycles(), info.getNodes(), info.getInstructions(), achievement, cheating);

        Path solutionPath = savesPath.resolve(idSlot + ".txt");
        String solution;
        try {
            solution = Files.readString(solutionPath).replace("\r\n", "\n");
        }
        catch (IOException e) {
            return new ValidationResult.Unparseable<>("Invalid file: " + solutionPath);
        }

        TISSubmission submission;
        try {
            submission = TISSubmission.fromData(solution, puzzle, author, score, "https://li.nk");
        }
        catch (ValidationException e) {
            return new ValidationResult.Unparseable<>(e.getMessage());
        }

        return new ValidationResult.Valid<>(submission);
    }
}
