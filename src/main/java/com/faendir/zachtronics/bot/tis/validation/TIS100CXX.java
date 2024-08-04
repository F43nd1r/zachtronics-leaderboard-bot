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
import com.faendir.zachtronics.bot.tis.model.TISSubmission;
import com.faendir.zachtronics.bot.tis.model.TISType;
import com.faendir.zachtronics.bot.validation.ValidationException;
import com.faendir.zachtronics.bot.validation.ValidationUtils;
import org.jetbrains.annotations.NotNull;

/** Wrapper for a TIS-100-CXX executable installed on the system */
public class TIS100CXX {

    public static @NotNull TISSubmission validate(@NotNull String data, @NotNull TISPuzzle puzzle, boolean cheating,
                                                  @NotNull String author, String displayLink) {
        if (puzzle.getType() == TISType.SANDBOX)
            throw new ValidationException("Sandbox levels are not supported");

        String[] command = {"TIS-100-CXX", "-q", "-",  puzzle.getId()};
        byte[] result = ValidationUtils.callValidator(data.getBytes(), command);
        String simResult = new String(result).trim();

        TISScore score = TISScore.parseScore(simResult);
        if (score == null)
            throw new ValidationException(simResult);

        // cheating isn't handled yet with the sim
        score = score.withCheating(cheating);

        return new TISSubmission(puzzle, score, author, displayLink, data);
    }
}
