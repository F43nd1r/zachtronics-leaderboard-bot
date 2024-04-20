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

package com.faendir.zachtronics.bot.sz.validation.chips;

import com.faendir.zachtronics.bot.validation.ValidationException;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static com.faendir.zachtronics.bot.sz.validation.SzSave.getInt;

/**
 * <pre>
 * [chip]
 * [type] NOTE
 * [x] 13
 * [y] 4
 * [code]
 * line1
 * line2
 * ...
 * </pre>
 */
@Value
public class SzChipNOTE implements SzChip {
    private static final SzChipType type = SzChipType.NOTE;

    int x;
    int y;
    @NotNull String[] lines;

    static @NotNull SzChipNOTE unmarshal(@NotNull Map<String, String> chipMap) {
        String[] lines = chipMap.get("code").split("\\n");
        if (lines.length > 9)
            throw new ValidationException("NOTE has " + lines.length + " LOC when the limit is 9");

        return new SzChipNOTE(getInt(chipMap, "x"),
                              getInt(chipMap, "y"),
                              lines);
    }

    @NotNull
    @Override
    public SzChipType getType() {
        return type;
    }
}
