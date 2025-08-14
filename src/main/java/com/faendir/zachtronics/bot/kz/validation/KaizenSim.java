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

package com.faendir.zachtronics.bot.kz.validation;

import com.faendir.zachtronics.bot.kz.model.KzPuzzle;
import com.faendir.zachtronics.bot.kz.model.KzScore;
import com.faendir.zachtronics.bot.kz.model.KzSubmission;
import com.faendir.zachtronics.bot.validation.ValidationException;
import com.faendir.zachtronics.bot.validation.ValidationUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/** wrapper for a kaizensim executable installed on the system */
public class KaizenSim {

    @NotNull
    public static KzSubmission validate(byte @NotNull [] data, @NotNull String author, String displayLink) throws ValidationException {
        KzSimResult result = validate(data);
        if (result.getError() != null) {
            throw new ValidationException(result.getError());
        }

        // l33t h4x0rs
        assert result.getManipulated() != null;
        if (result.getManipulated()) {
            throw new ValidationException("Solution was manipulated using an external editor");
        }

        // puzzle
        assert result.getLevel() != null;
        KzPuzzle puzzle = Arrays.stream(KzPuzzle.values())
                                .filter(p -> p.getId() == result.getLevel())
                                .findFirst()
                                .orElseThrow();

        // score
        assert result.getTime() != null;
        assert result.getCost() != null;
        assert result.getArea() != null;
        KzScore score = new KzScore(result.getTime(), result.getCost(), result.getArea());

        return new KzSubmission(puzzle, score, author, displayLink, data);
    }

    @NotNull
    static KzSimResult validate(byte @NotNull [] data) throws ValidationException {
        List<String> command = List.of("kaizensim", "score");
        return ValidationUtils.callValidator(KzSimResult.class, data, command);
    }
}
