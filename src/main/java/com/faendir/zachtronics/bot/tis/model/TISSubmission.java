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

package com.faendir.zachtronics.bot.tis.model;

import com.faendir.zachtronics.bot.model.Submission;
import com.faendir.zachtronics.bot.tis.validation.TISValidator;
import com.faendir.zachtronics.bot.utils.Utils;
import com.faendir.zachtronics.bot.validation.ValidationException;
import lombok.Value;
import lombok.With;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.Supplier;

@Value
public class TISSubmission implements Submission<TISCategory, TISPuzzle> {
    @NotNull TISPuzzle puzzle;
    @NotNull TISScore score;
    @NotNull String author;
    @With String displayLink;
    @NotNull String data;

    @NotNull
    public static TISSubmission fromData(@NotNull String data, @NotNull TISPuzzle puzzle, @NotNull String author, String displayLink)
    throws ValidationException {
        TISScore score = TISValidator.validate(data, puzzle);
        return new TISSubmission(puzzle, score, author, displayLink, data);
    }

    @NotNull
    public static TISSubmission fromLink(@NotNull String link, @Nullable TISPuzzle puzzle, @NotNull String author, String displayLink)
    throws ValidationException {
        Utils.FileInfo info = Utils.downloadFile(link);
        if (puzzle == null) {
            Supplier<ValidationException> failureSupplier = () -> new ValidationException(
                "It was not possible to deduce the puzzle automatically, use the `puzzle` option");
            if (info.getFilename() == null)
                throw failureSupplier.get();
            puzzle = Arrays.stream(TISPuzzle.values())
                           .filter(p -> info.getFilename().startsWith(p.getId()))
                           .findFirst()
                           .orElseThrow(failureSupplier);
        }
        return fromData(info.dataAsString(), puzzle, author, displayLink);
    }
}
