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

package com.faendir.zachtronics.bot.sc.model;

import com.faendir.zachtronics.bot.model.Submission;
import com.faendir.zachtronics.bot.sc.validation.SChem;
import com.faendir.zachtronics.bot.sc.validation.ScMetadataReader;
import com.faendir.zachtronics.bot.utils.Utils;
import com.faendir.zachtronics.bot.validation.ValidationException;
import com.faendir.zachtronics.bot.validation.ValidationResult;
import lombok.Value;
import lombok.With;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/** Archive-only submissions have a <tt>null</tt> {@link #displayLink} */
@Value
public class ScSubmission implements Submission<ScCategory, ScPuzzle> {
    @NotNull ScPuzzle puzzle;
    @NotNull ScScore score;
    @NotNull String author;
    @With String displayLink;
    @NotNull String data;

    /**
     * @throws ValidationException if we can't correctly parse metadata
     */
    @NotNull
    public static ScSubmission fromDataNoValidation(@NotNull String data, @Nullable ScPuzzle puzzle, @Nullable String displayLink)
    throws ValidationException {
        return ScMetadataReader.fromHeader(data, puzzle, displayLink);
    }

    @NotNull
    public static Collection<ValidationResult<ScSubmission>> fromData(@NotNull String export, boolean bypassValidation,
                                                                      String author) {
        return SChem.validateMultiExport(export, bypassValidation, author);
    }

    @NotNull
    public static Collection<ValidationResult<ScSubmission>> fromExportLink(@NotNull String exportLink, boolean bypassValidation,
                                                                            String author) {
        String export = Utils.downloadFile(exportLink).dataAsString();
        return fromData(export, bypassValidation, author);
    }
}
