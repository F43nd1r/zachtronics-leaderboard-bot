/*
 * Copyright (c) 2026
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

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.List;

public class KzValidator {
    private KzValidator() {}

    /** wrapper for libkaizensim, WL's sim */
    public static @NotNull KzSubmission validateFFI(byte @NotNull [] data, @NotNull String author, String displayLink)
    throws ValidationException {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment ffiScore = KzSimFFIWrapper.scoreCreate(arena, data);

            String error = KzSimFFIWrapper.error(ffiScore);
            if (error != null) {
                throw new ValidationException(error);
            }

            // l33t h4x0rs
            if (KzSimFFIWrapper.manipulated(ffiScore)) {
                throw new ValidationException("Solution was manipulated using an external editor");
            }

            // puzzle
            int level = KzSimFFIWrapper.level(ffiScore);
            KzPuzzle puzzle = Arrays.stream(KzPuzzle.values())
                                    .filter(p -> p.getId() == level)
                                    .findFirst()
                                    .orElseThrow();

            KzScore score = new KzScore(KzSimFFIWrapper.time(ffiScore),
                                        KzSimFFIWrapper.cost(ffiScore),
                                        KzSimFFIWrapper.area(ffiScore));

            return new KzSubmission(puzzle, score, author, displayLink, data);
        }
    }

    /** wrapper for kaizen-sim, Zach's sim */
    public static @NotNull KzSubmission validateZach(byte @NotNull [] data, @NotNull String author, String displayLink)
    throws ValidationException {
        KzSimZachResult result = validateZach(data);
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

        assert result.getNormalized() != null;
        return new KzSubmission(puzzle, score, author, displayLink, result.getNormalized());
    }

    static @NotNull KzSimZachResult validateZach(byte @NotNull [] data) throws ValidationException {
        List<String> command = List.of("kaizen-sim", "--normalize", "-");
        return ValidationUtils.callValidator(KzSimZachResult.class, data, command);
    }
}
