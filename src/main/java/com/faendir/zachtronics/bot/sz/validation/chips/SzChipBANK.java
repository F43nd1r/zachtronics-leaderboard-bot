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

import java.util.Map;

import static com.faendir.zachtronics.bot.sz.validation.SzValidator.getInt;

/**
 * <pre>
 * [chip]
 * [type] BANK
 * [x] 13
 * [y] 4
 * [rom]
 * -9,-90,-900,-101,0,-101,0,-101,0,-101,0,-101,999,-999
 * </pre>
 */
@Value
class SzChipBANK implements SzChip {
    private static final SzChipType type = SzChipType.BANK;
    private static final int cost = 2;

    int x;
    int y;
    @NotNull String rom;

    static SzChipBANK unmarshal(@NotNull Map<String, String> chipMap) {
        return new SzChipBANK(getInt(chipMap, "x"),
                              getInt(chipMap, "y"),
                              chipMap.get("rom"));
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
