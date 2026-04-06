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

package com.faendir.zachtronics.bot.fc.model;

import com.faendir.zachtronics.bot.fc.validation.FoodCourtSim;
import com.faendir.zachtronics.bot.model.Submission;
import com.faendir.zachtronics.bot.utils.Utils;
import com.faendir.zachtronics.bot.validation.ValidationResult;
import lombok.Value;
import lombok.With;
import org.jspecify.annotations.NonNull;

import java.util.Collection;

@Value
public class FcSubmission implements Submission<FcCategory, FcPuzzle> {
    @NonNull FcPuzzle puzzle;
    @NonNull FcScore score;
    @NonNull String author;
    @With String displayLink;
    byte @NonNull [] data;

    @NonNull
    public static Collection<ValidationResult<FcSubmission>> fromData(byte @NonNull [] data, String author) {
        return FoodCourtSim.validateMultiExport(data, author);
    }

    @NonNull
    public static Collection<ValidationResult<FcSubmission>> fromLink(@NonNull String link, String author) {
        byte[] data = Utils.downloadFile(link).getData();
        return fromData(data, author);
    }
}
