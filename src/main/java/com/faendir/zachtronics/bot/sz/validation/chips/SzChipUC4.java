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

import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import static com.faendir.zachtronics.bot.sz.validation.SzSave.getInt;

/**
 * <pre>
 * [chip]
 * [type] UC4?
 * [x] 13
 * [y] 4
 * [code]
 * line1
 * line2
 * ...
 * </pre>
 */
@Value
class SzChipUC4 implements SzChipUC {
    private static final int cost = 3;

    @NotNull SzChipType type;
    int x;
    int y;
    @NotNull List<SzCodeLine> lines;

    static @NotNull SzChipUC4 unmarshal(@NotNull SzChipType type, @NotNull Map<String, String> chipMap) {
        return new SzChipUC4(type,
                             getInt(chipMap, "x"),
                             getInt(chipMap, "y"),
                             SzChipUC.readLines(chipMap, 9));
    }

    @NotNull
    @Override
    public SzChipType getType() {
        return type;
    }

    @Override
    public int getCost() {
        return cost;
    }
}
