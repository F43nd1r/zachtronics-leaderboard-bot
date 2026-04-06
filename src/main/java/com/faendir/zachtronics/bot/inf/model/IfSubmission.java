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

package com.faendir.zachtronics.bot.inf.model;

import com.faendir.zachtronics.bot.inf.validation.IfValidator;
import com.faendir.zachtronics.bot.model.Submission;
import com.faendir.zachtronics.bot.utils.Utils;
import com.faendir.zachtronics.bot.validation.ValidationResult;
import lombok.Value;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;

@Value
public class IfSubmission implements Submission<IfCategory, IfPuzzle> {
    @NonNull IfPuzzle puzzle;
    @NonNull IfScore score;
    @NonNull String author;
    @NonNull List<String> displayLinks;
    @NonNull String data;

    @Nullable
    @Override
    public String getDisplayLink() {
        return displayLinks.isEmpty() ? null : displayLinks.get(0);
    }

    @NonNull
    public static Collection<ValidationResult<IfSubmission>> fromData(@NonNull String data, @NonNull String author, IfScore score,
                                                                      List<String> videos, boolean isAdmin) {
        return IfValidator.validateSavefile(data, author, score, videos, isAdmin);
    }

    @NonNull
    public static Collection<ValidationResult<IfSubmission>> fromLink(@NonNull String link, @NonNull String author, IfScore score,
                                                                      List<String> videos, boolean isAdmin) {
        String data = Utils.downloadFile(link).dataAsString();
        return fromData(data, author, score, videos, isAdmin);
    }
}
