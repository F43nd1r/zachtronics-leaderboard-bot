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

package com.faendir.zachtronics.bot.fp.model;

import com.faendir.zachtronics.bot.fp.validation.XBPGHSim;
import com.faendir.zachtronics.bot.model.Submission;
import com.faendir.zachtronics.bot.utils.Utils;
import com.faendir.zachtronics.bot.validation.ValidationResult;
import lombok.Value;
import lombok.With;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

@Value
public class FpSubmission implements Submission<FpCategory, FpPuzzle> {
    FpPuzzle puzzle;
    FpScore score;
    String author;
    @With @Nullable String displayLink;
    String data;

    public static Collection<ValidationResult<FpSubmission>> fromData(String data, String author) {
        return XBPGHSim.validateMultiExport(data, author);
    }

    public static Collection<ValidationResult<FpSubmission>> fromLink(String link, String author) {
        String data = Utils.downloadFile(link).dataAsString();
        return fromData(data, author);
    }
}
