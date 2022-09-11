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

package com.faendir.zachtronics.bot.fp.model;

import com.faendir.zachtronics.bot.model.DisplayContext;
import com.faendir.zachtronics.bot.model.Record;
import com.faendir.zachtronics.bot.utils.Markdown;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

@Value
public class FpRecord implements Record<FpCategory> {
    @NotNull FpPuzzle puzzle;
    @NotNull FpScore score;
    @NotNull String author;
    String displayLink;
    String dataLink;
    Path dataPath;

    @NotNull
    @Override
    public String toDisplayString(@NotNull DisplayContext<FpCategory> context) {
        return Markdown.fileLinkOrEmpty(dataLink) +
               Markdown.linkOrText(score.toDisplayString(context) + " " + author, displayLink);
    }
}