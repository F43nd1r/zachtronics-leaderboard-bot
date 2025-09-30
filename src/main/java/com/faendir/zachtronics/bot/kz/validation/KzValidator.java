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
import org.jetbrains.annotations.NotNull;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;

/** wrapper for libkaizensim */
public class KzValidator {
    private KzValidator() {}

    public static @NotNull KzSubmission validate(byte @NotNull [] data, @NotNull String author, String displayLink)
    throws ValidationException {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment ffiScore = KaizenSimFFI.scoreCreate(arena, data);

            String error = KaizenSimFFI.error(ffiScore);
            if (error != null) {
                throw new ValidationException(error);
            }

            // l33t h4x0rs
            if (KaizenSimFFI.manipulated(ffiScore)) {
                throw new ValidationException("Solution was manipulated using an external editor");
            }

            // puzzle
            int level = KaizenSimFFI.level(ffiScore);
            KzPuzzle puzzle = Arrays.stream(KzPuzzle.values())
                                    .filter(p -> p.getId() == level)
                                    .findFirst()
                                    .orElseThrow();

            KzScore score = new KzScore(KaizenSimFFI.time(ffiScore),
                                        KaizenSimFFI.cost(ffiScore),
                                        KaizenSimFFI.area(ffiScore));

            return new KzSubmission(puzzle, score, author, displayLink, data);
        }
    }
}
