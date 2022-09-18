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

package com.faendir.zachtronics.bot.sz.model;

import com.faendir.zachtronics.bot.model.Submission;
import com.faendir.zachtronics.bot.utils.Utils;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value
public class SzSubmission implements Submission<SzCategory, SzPuzzle> {
    @NotNull SzPuzzle puzzle;
    @NotNull SzScore score;
    @NotNull String author;
    @NotNull String data;

    @Nullable
    @Override
    public String getDisplayLink() {
        return null;
    }

    /**
     * @throws IllegalArgumentException if we can't correctly parse metadata
     */
    @NotNull
    public static SzSubmission fromData(@NotNull String data, @NotNull String author) throws IllegalArgumentException {
        SzSolutionMetadata metadata = SzSolutionMetadata.fromData(data);
        return metadata.extendToSubmission(author, data);
    }

    @NotNull
    public static SzSubmission fromLink(@NotNull String link, String author) {
        String data = Utils.downloadSolutionFile(link);
        return fromData(data, author);
    }
}
