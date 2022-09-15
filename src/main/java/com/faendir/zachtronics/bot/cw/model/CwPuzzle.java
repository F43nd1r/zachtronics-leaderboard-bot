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

package com.faendir.zachtronics.bot.cw.model;

import com.faendir.zachtronics.bot.model.Puzzle;
import lombok.Getter;

import java.util.List;

@Getter
public enum CwPuzzle implements Puzzle<CwCategory> {
    PUZZLE_1_1(2, "Signal Crossover"),
    PUZZLE_1_2(3, "AND Gate"),
    PUZZLE_1_3(1, "OR Gate"),
    PUZZLE_1_4(4, "NOT Gate"),
    PUZZLE_1_5(9, "Power-on Reset"),
    PUZZLE_1_6(22, "Digital Signal Mixer"),
    PUZZLE_1_7(12, "Interrupt Controller"),
    PUZZLE_2_1(13, "Ignition Sequencer"),
    PUZZLE_2_2(8, "Equality Tester"),
    PUZZLE_2_3(10, "Dual Oscillator"),
    PUZZLE_2_4(19, "Safety Interlock"),
    PUZZLE_2_5(11, "PWM Solenoid Driver"),
    PUZZLE_2_6(5, "Electronic Lock"),
    PUZZLE_2_7(7, "Motor Controller"),
    PUZZLE_3_1(20, "Programmable Delay"),
    PUZZLE_3_2(16, "Synchrony Detector"),
    PUZZLE_3_3(21, "AND-OR Combo Gate"),
    PUZZLE_3_4(15, "Switch Debouncer"),
    PUZZLE_3_5(17, "Stepper Motor Driver"),
    PUZZLE_3_6(14, "Serial Number ROM"),
    PUZZLE_3_7(18, "Pulse Echo Detector");

    private final int id;
    private final CwGroup group = CwGroup.CAMPAIGN;
    private final CwType type = CwType.STANDARD;
    private final String displayName;
    private final List<CwCategory> supportedCategories = List.of(CwCategory.values());
    private final String link;

    CwPuzzle(int id, String displayName) {
        this.id = id;
        this.displayName = displayName;
        this.link = "https://zlbb.faendir.com/cw/" + name();
    }

}
