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

package com.faendir.zachtronics.bot.inf.model;

import com.faendir.zachtronics.bot.model.Puzzle;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public enum IfPuzzle implements Puzzle<IfCategory> {
    LEVEL_1_1("1-1", IfGroup.ZONE_1, IfType.STANDARD, "Training Routine 1"),
    LEVEL_1_2("1-2", IfGroup.ZONE_1, IfType.STANDARD, "Training Routine 2"),
    LEVEL_1_3("1-3", IfGroup.ZONE_1, IfType.STANDARD, "Training Routine 3"),
    LEVEL_1_4("1-4", IfGroup.ZONE_1, IfType.STANDARD, "Training Routine 4"),
    LEVEL_1_5("1-5", IfGroup.ZONE_1, IfType.STANDARD, "Training Routine 5"),
    LEVEL_2_1("2-1", IfGroup.ZONE_2, IfType.STANDARD, "Munitions Refill Type 2"),
    LEVEL_2_2("2-2b", IfGroup.ZONE_2, IfType.STANDARD, "Munitions Refill Type 6"),
    LEVEL_2_3("2-4", IfGroup.ZONE_2, IfType.STANDARD, "Shuttle Propulsion Units"),
    LEVEL_2_4("2-3", IfGroup.ZONE_2, IfType.STANDARD, "Wave Detection Array"),
    LEVEL_2_5("2-5", IfGroup.ZONE_2, IfType.STANDARD, "Guided Javelin Type 1"),
    LEVEL_3_1("3-1b", IfGroup.ZONE_3, IfType.STANDARD, "Optical Sensor Array Type 2"),
    LEVEL_3_2("3-2", IfGroup.ZONE_3, IfType.STANDARD, "Landing Alignment Lights"),
    LEVEL_3_3("3-3", IfGroup.ZONE_3, IfType.STANDARD, "Optical Sensor Array Type 4"),
    LEVEL_3_4("3-5b", IfGroup.ZONE_3, IfType.STANDARD, "Small Excavator"),
    LEVEL_3_5("3-4b", IfGroup.ZONE_3, IfType.STANDARD, "Cargo Uplifter"),
    LEVEL_4_1("4-1", IfGroup.ZONE_4, IfType.STANDARD, "Terminal Display Reclamation"),
    LEVEL_4_2("4-3", IfGroup.ZONE_4, IfType.STANDARD, "Shuttle Maintenance"),
    LEVEL_4_3("4-2", IfGroup.ZONE_4, IfType.STANDARD, "Oversight Terminal Model 6"),
    LEVEL_4_4("4-4", IfGroup.ZONE_4, IfType.STANDARD, "Drone Maintenance"),
    LEVEL_4_5("4-5", IfGroup.ZONE_4, IfType.STANDARD, "Furnished Studio Apartment"),
    LEVEL_5_1("5-1", IfGroup.ZONE_5, IfType.STANDARD, "Guided Javelin Type 2"),
    LEVEL_5_2("5-3", IfGroup.ZONE_5, IfType.STANDARD, "Gneiss Chair"),
    LEVEL_5_3("5-4b", IfGroup.ZONE_5, IfType.STANDARD, "Relay Satellite"),
    LEVEL_5_4("5-2b", IfGroup.ZONE_5, IfType.STANDARD, "Terrestrial Surveyor (5-4)"),
    LEVEL_5_5("5-5", IfGroup.ZONE_5, IfType.STANDARD, "Anti-Javelin Point Defense"),

    LEVEL_6_1("6-1", IfGroup.ZONE_6, IfType.STANDARD, "Meat Product Type 57"),
    LEVEL_6_2("6-2", IfGroup.ZONE_6, IfType.STANDARD, "Relaxant Formula 13"),
    LEVEL_6_3("6-3", IfGroup.ZONE_6, IfType.STANDARD, "Terrestrial Drone"),
    LEVEL_6_4("6-4", IfGroup.ZONE_6, IfType.STANDARD, "Meat Product Type 101"),
    LEVEL_6_5("6-5", IfGroup.ZONE_6, IfType.STANDARD, "Aerial Combat Shuttle"),
    LEVEL_7_1("7-1", IfGroup.ZONE_7, IfType.STANDARD, "Teleporter Experiment 1"),
    LEVEL_7_2("7-2", IfGroup.ZONE_7, IfType.STANDARD, "Teleporter Experiment 2"),
    LEVEL_7_3("7-3", IfGroup.ZONE_7, IfType.STANDARD, "Teleporter Experiment 3"),
    LEVEL_7_4("7-4", IfGroup.ZONE_7, IfType.STANDARD, "Teleporter Experiment 4"),
    LEVEL_7_5("7-5", IfGroup.ZONE_7, IfType.STANDARD, "Terrestrial Surveyor (7-5)"),
    LEVEL_7_6("7-6", IfGroup.ZONE_7, IfType.STANDARD, "Anti-Javelin Satellite"),
    LEVEL_7_7("7-7", IfGroup.ZONE_7, IfType.STANDARD, "The Big Blowout"),
    LEVEL_8_1("8-1", IfGroup.ZONE_8, IfType.STANDARD, "Hull Panels"),
    LEVEL_8_2("8-2", IfGroup.ZONE_8, IfType.STANDARD, "Central Axis Support"),
    LEVEL_8_3("8-3", IfGroup.ZONE_8, IfType.STANDARD, "Docking Clamp"),
    LEVEL_8_4("8-4", IfGroup.ZONE_8, IfType.STANDARD, "Structural Frame"),
    LEVEL_8_5("8-5", IfGroup.ZONE_8, IfType.STANDARD, "Anti-Javelin Battery"),
    LEVEL_8_6("8-6", IfGroup.ZONE_8, IfType.STANDARD, "Mag-Tube Corridor"),
    LEVEL_8_7("8-7", IfGroup.ZONE_8, IfType.STANDARD, "Solar Cell Array"),
    LEVEL_9_1("9-1", IfGroup.ZONE_9, IfType.STANDARD, "Landing Alignment Guide"),
    LEVEL_9_2("9-2", IfGroup.ZONE_9, IfType.STANDARD, "Control Console"),
    LEVEL_9_3("9-3", IfGroup.ZONE_9, IfType.STANDARD, "Laser Calibration Target"),
    LEVEL_9_4("9-4", IfGroup.ZONE_9, IfType.STANDARD, "Life Support System"),
    LEVEL_9_5("9-5", IfGroup.ZONE_9, IfType.STANDARD, "Space Buoy"),
    LEVEL_9_6("9-6", IfGroup.ZONE_9, IfType.STANDARD, "Resistance Shuttles"),
    LEVEL_10_1("10-1", IfGroup.ZONE_10, IfType.STANDARD, "Navigation Computer"),
    LEVEL_10_2("10-2", IfGroup.ZONE_10, IfType.STANDARD, "Fusion Reactor"),
    LEVEL_10_3("10-3", IfGroup.ZONE_10, IfType.STANDARD, "Crew Quarters"),
    LEVEL_10_4("10-4", IfGroup.ZONE_10, IfType.STANDARD, "Plasma Engine"),
    LEVEL_10_5("10-5", IfGroup.ZONE_10, IfType.BOSS, "Fire-Control System"),
    LEVEL_10_6("10-6", IfGroup.ZONE_10, IfType.STANDARD, "Skip Drive");

    private final String id;
    private final IfGroup group;
    private final IfType type;
    private final String displayName;
    private final List<IfCategory> supportedCategories;
    private final String link;

    IfPuzzle(String id, IfGroup group, IfType type, String displayName) {
        this.id = id;
        this.group = group;
        this.type = type;
        this.displayName = displayName;
        this.supportedCategories = type == IfType.BOSS ? Collections.emptyList() : List.of(IfCategory.values());
        this.link = "https://zlbb.faendir.com/if/" + id;
    }

}
