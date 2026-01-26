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

package com.faendir.zachtronics.bot.kz.model;

import com.faendir.zachtronics.bot.kz.validation.KzValidator;
import com.faendir.zachtronics.bot.model.Submission;
import com.faendir.zachtronics.bot.utils.Utils;
import com.faendir.zachtronics.bot.validation.ValidationException;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value
public class KzSubmission implements Submission<KzCategory, KzPuzzle> {
    @NotNull KzPuzzle puzzle;
    @NotNull KzScore score;
    @NotNull String author;
    @Nullable String displayLink;
    byte @NotNull [] data;

    /**
     * @throws ValidationException if we can't correctly parse metadata
     */
    @NotNull
    public static KzSubmission fromData(byte @NotNull [] data, @NotNull String author, String displayLink) throws ValidationException {
        return KzValidator.validateZach(data, author, displayLink);
    }

    @NotNull
    public static KzSubmission fromLink(@NotNull String link, String author, String displayLink) {
        byte @NotNull [] data = Utils.downloadFile(link).getData();
        return fromData(data, author, displayLink);
    }
}
