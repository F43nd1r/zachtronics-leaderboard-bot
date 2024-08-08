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
import com.faendir.zachtronics.bot.tis.validation.TIS100CXX;
import com.faendir.zachtronics.bot.utils.Utils;
import com.faendir.zachtronics.bot.validation.ValidationException;
import lombok.Value;
import lombok.With;
import org.jetbrains.annotations.NotNull;

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
        TISScore score = TIS100CXX.validate(data, puzzle);
        return new TISSubmission(puzzle, score, author, displayLink, data);
    }

    @NotNull
    public static TISSubmission fromLink(@NotNull String link, @NotNull TISPuzzle puzzle, @NotNull String author, String displayLink)
    throws ValidationException {
        String data = Utils.downloadSolutionFile(link);
        return fromData(data, puzzle, author, displayLink);
    }
}
