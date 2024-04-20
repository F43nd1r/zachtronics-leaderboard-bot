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
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * The chips all have a common header:
 * <pre>
 * [chip]
 * [type] TYPE
 * [x] 1
 * [y] 2
 * </pre>
 * then type-specific information
 */
public interface SzChip {
    @NotNull SzChipType getType();
    /** min 1 at left */
    int getX();
    /** min 1 at the bottom */
    int getY();

    @NotNull
    static SzChip unmarshal(@NotNull Map<String, String> chipMap) {
        SzChipType type;
        try {
            type = SzChipType.valueOf(chipMap.get("type"));
        }
        catch (IllegalArgumentException e) {
            type = SzChipType.OTHER;
        }
        catch (NullPointerException e) {
            throw new ValidationException("Missing chip type");
        }

        switch (type) {
            case NOTE -> {
                return SzChipNOTE.unmarshal(chipMap);
            }
            case BRIDGE -> {
                return SzChipBRIDGE.unmarshal(chipMap);
            }
            case UC4, UC4X -> {
                return SzChipUC4.unmarshal(type, chipMap);
            }
            case UC6 -> {
                return SzChipUC6.unmarshal(chipMap);
            }
            case DX3 -> {
                return SzChipDX3.unmarshal(chipMap);
            }
            case RAM -> {
                return SzChipRAM.unmarshal(chipMap);
            }
            case BANK -> {
                return SzChipBANK.unmarshal(chipMap);
            }
            case NOT -> {
                return SzChipNOT.unmarshal(chipMap);
            }
            case AND, OR, XOR -> {
                return SzChipBinGate.unmarshal(type, chipMap);
            }
            case PGA -> {
                return SzChipPGA.unmarshal(chipMap);
            }
            default -> {
                return SzChipOther.unmarshal(chipMap);
            }
        }
    }
}
