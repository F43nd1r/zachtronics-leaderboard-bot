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

package com.faendir.zachtronics.bot.sz.validation.chips;

import com.faendir.zachtronics.bot.validation.ValidationException;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <pre>
 * [chip]
 * [type] UC??
 * [x] 1
 * [y] 2
 * [code]
 * line1
 * line2
 * ...
 * </pre>
 */
public interface SzChipUC extends SzChip {
    Pattern LINE_REGEX = Pattern.compile(
            String.join("\\s*", new String[]{"", "(?:(?<label>[^#:]+):)?", "(?<code>[^#:]+)?", "(?:#(?<comment>.*))?", ""}));

    @Value
    class SzCodeLine {
        @NotNull String rawLine;

        @Nullable String label;
        @Nullable String code;
        @Nullable String comment;
    }

    @NotNull List<SzCodeLine> getLines();

    @NotNull
    static List<SzCodeLine> readLines(@NotNull Map<String, String> chipMap, int limit) {
        String[] rawLines = chipMap.get("code").split("\\n");
        if (rawLines.length > limit)
            throw new ValidationException("UC has " + rawLines.length + " LOC when the limit is " + limit);

        List<SzCodeLine> lines = new ArrayList<>();
        for (String rawLine: rawLines) {
            Matcher m = LINE_REGEX.matcher(rawLine);
            if (m.matches()) {
                lines.add(new SzCodeLine(rawLine, m.group("label"), m.group("code"), m.group("comment")));
            }
            else
                throw new ValidationException("Malformed line: \"" + rawLine + "\"");
        }
        return lines;
    }

    default int linesOfCode() {
        return (int) getLines().stream().filter(l -> l.getCode() != null).count();
    }
}
