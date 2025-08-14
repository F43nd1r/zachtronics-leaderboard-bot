/*
 * Copyright (c) 2025
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

package com.faendir.zachtronics.bot.tis.validation;


import com.faendir.zachtronics.bot.tis.model.TISPuzzle;
import com.faendir.zachtronics.bot.tis.model.TISScore;
import com.faendir.zachtronics.bot.tis.model.TISType;
import com.faendir.zachtronics.bot.validation.ValidationException;
import com.faendir.zachtronics.bot.validation.ValidationUtils;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/** Wrapper for a TIS-100-CXX executable installed on the system */
public class TIS100CXX {

    @SneakyThrows(IOException.class)
    public static @NotNull TISScore validate(@NotNull String data, @NotNull TISPuzzle puzzle) throws ValidationException {
        if (puzzle.getType() == TISType.SANDBOX)
            throw new ValidationException("Sandbox levels are not supported");

        List<String> command = new ArrayList<>(Arrays.asList("TIS-100-CXX", "-q", "--limit", "150k", "--total-limit", "100M"));
        if (puzzle.getExtraWitnessSeeds().length != 0) {
            command.add("--seeds");
            command.add(Arrays.stream(puzzle.getExtraWitnessSeeds()).mapToObj(Integer::toString).collect(Collectors.joining(",")));
        }
        command.add("--seeds");
        command.add("100000.." + (200000 - 1 - puzzle.getExtraWitnessSeeds().length));

        Path customSpecPath = null;
        if (puzzle.isCustomSpec()) {
            customSpecPath = puzzle.extractCustomSpec();
            command.add("-L");
            command.add(customSpecPath.toString());
        }
        else {
            command.add("-l");
            command.add(puzzle.getId());
        }
        command.add("-");

        byte[] result = ValidationUtils.callValidator(data.getBytes(), command);
        String simResult = new String(result).trim();

        if (puzzle.isCustomSpec()) {
            assert customSpecPath != null;
            Files.deleteIfExists(customSpecPath);
        }

        TISScore score = TISScore.parseScore(simResult);
        if (score == null)
            throw new ValidationException("```\n" + simResult + "\n```");
        return score;
    }
}
