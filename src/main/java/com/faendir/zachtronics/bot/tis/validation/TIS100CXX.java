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

package com.faendir.zachtronics.bot.tis.validation;


import com.faendir.zachtronics.bot.tis.model.TISPuzzle;
import com.faendir.zachtronics.bot.tis.model.TISScore;
import com.faendir.zachtronics.bot.tis.model.TISType;
import com.faendir.zachtronics.bot.validation.ValidationException;
import com.faendir.zachtronics.bot.validation.ValidationUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.StringJoiner;

/** Wrapper for a TIS-100-CXX executable installed on the system */
public class TIS100CXX {

    public static @NotNull TISScore validate(@NotNull String data, @NotNull TISPuzzle puzzle) {
        if (puzzle.getType() == TISType.SANDBOX)
            throw new ValidationException("Sandbox levels are not supported");

        StringJoiner seedJoiner = new StringJoiner(",");
        Arrays.stream(puzzle.getExtraWitnessSeeds()).mapToObj(Integer::toString).forEach(seedJoiner::add);
        seedJoiner.add("100000..199999");
        String seeds = seedJoiner.toString();

        String[] command = {"TIS-100-CXX",
                            "-q", "--seeds", seeds, "--limit", "120000", "--total-limit", Integer.toString(100_000_000),
                            "-", puzzle.getId()};
        byte[] result = ValidationUtils.callValidator(data.getBytes(), command);
        String simResult = new String(result).trim();

        TISScore score = TISScore.parseScore(simResult);
        if (score == null)
            throw new ValidationException("```\n" + simResult + "\n```");
        return score;
    }
}
