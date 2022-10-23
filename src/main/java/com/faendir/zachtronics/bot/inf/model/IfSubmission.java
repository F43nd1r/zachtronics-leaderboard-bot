/*
 * Copyright (c) 2022
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
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Value
public class IfSubmission implements Submission<IfCategory, IfPuzzle> {
    @NotNull IfPuzzle puzzle;
    @NotNull IfScore score;
    @NotNull String author;
    @NotNull List<String> displayLinks;
    @NotNull String data;

    @Nullable
    @Override
    public String getDisplayLink() {
        return displayLinks.isEmpty() ? null : displayLinks.get(0);
    }

    /**
     * @throws IllegalArgumentException if we can't correctly parse metadata
     */
    @NotNull
    public static IfSubmission fromData(@NotNull String data, @NotNull String author, @NotNull IfScore score,
                                        @NotNull List<String> displayLinks) throws IllegalArgumentException {
        return IfValidator.validate(data, author, score, displayLinks);
    }

    @NotNull
    public static IfSubmission fromLink(@NotNull String link, String author, @NotNull IfScore score,
                                        @NotNull List<String> displayLinks) {
        String data = Utils.downloadSolutionFile(link);
        return fromData(data, author, score, displayLinks);
    }
}
