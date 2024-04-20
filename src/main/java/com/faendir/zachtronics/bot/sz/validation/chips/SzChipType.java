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

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SzChipType {
    NOTE(0, 3, 2),
    BRIDGE(0, 1, 3),

    // MCs
    UC4(3, 3, 2),
    UC4X(3, 3, 2),
    UC6(5, 3, 3),

    DX3(1, 2, 3),

    // memories
    RAM(2, 3, 2),
    BANK(2, 3, 2),

    // gates
    NOT(1, 2, 1),
    AND(1, 2, 2),
    OR(1, 2, 2),
    XOR(1, 2, 2),

    PGA(5, 3, 3),

    OTHER(0, 1, 1); // TODO handle more components

    private final int cost;
    private final int sizeX;
    private final int sizeY;
}
