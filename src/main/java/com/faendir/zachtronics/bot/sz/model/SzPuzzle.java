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

package com.faendir.zachtronics.bot.sz.model;

import com.faendir.discord4j.command.parse.SingleParseResult;
import com.faendir.zachtronics.bot.model.Puzzle;
import com.faendir.zachtronics.bot.utils.UtilsKt;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@Getter
public enum SzPuzzle implements Puzzle<SzCategory> {
    Sz000("fake-surveillance-camera", SzGroup.FIRST_CAMPAIGN, SzType.STANDARD, "Fake Surveillance Camera"),
    Sz030("control-signal-amplifier", SzGroup.FIRST_CAMPAIGN, SzType.STANDARD, "Control Signal Amplifier"),
    Sz002("diagnostic-pulse-generator", SzGroup.FIRST_CAMPAIGN, SzType.STANDARD, "Diagnostic Pulse Generator"),
    Sz001("animated-esports-sign", SzGroup.FIRST_CAMPAIGN, SzType.STANDARD, "Animated Esports Sign"),
    Sz028("drinking-game-scorekeeper", SzGroup.FIRST_CAMPAIGN, SzType.STANDARD, "Drinking Game Scorekeeper"),
    Sz003("harmonic-maximization-engine", SzGroup.FIRST_CAMPAIGN, SzType.STANDARD, "Harmonic Maximization Engine"),
    Sz005("passive-infrared-sensor", SzGroup.FIRST_CAMPAIGN, SzType.STANDARD, "Passive Infrared Sensor"),
    Sz035("virtual-reality-buzzer", SzGroup.FIRST_CAMPAIGN, SzType.STANDARD, "Virtual Reality Buzzer"),
    SzSandbox("prototyping-area", SzGroup.FIRST_CAMPAIGN, SzType.SANDBOX, "Prototyping Area"),
    Sz011("wireless-game-controller", SzGroup.FIRST_CAMPAIGN, SzType.STANDARD, "Wireless Game Controller"),
    Sz048("laser-tag-equipment", SzGroup.FIRST_CAMPAIGN, SzType.STANDARD, "Laser Tag Equipment"),
    Sz010("color-changing-vape-pen", SzGroup.FIRST_CAMPAIGN, SzType.STANDARD, "Color-Changing Vape Pen"),
    Sz015("unknown-optimization-device", SzGroup.FIRST_CAMPAIGN, SzType.STANDARD, "Unknown Optimization Device"),
    Sz009("token-based-payment-kiosk", SzGroup.FIRST_CAMPAIGN, SzType.STANDARD, "Token-Based Payment Kiosk"),
    Sz007EZ("personal-sandwich-maker", SzGroup.FIRST_CAMPAIGN, SzType.STANDARD, "Personal Sandwich Maker"),
    Sz014("carbine-target-illuminator", SzGroup.FIRST_CAMPAIGN, SzType.STANDARD, "Carbine Target Illuminator"),
    Sz008("haunted-doll", SzGroup.FIRST_CAMPAIGN, SzType.STANDARD, "Haunted Doll"),
    Sz023("aquaponics-maintenance-robot", SzGroup.FIRST_CAMPAIGN, SzType.STANDARD, "Aquaponics Maintenance Robot"),
    Sz016("remote-kill-switch", SzGroup.FIRST_CAMPAIGN, SzType.STANDARD, "Remote Kill Switch"),
    Sz024("smart-grid-control-router", SzGroup.FIRST_CAMPAIGN, SzType.STANDARD, "Smart Grid Control Router"),
    Sz017("pocket-i-ching-oracle", SzGroup.FIRST_CAMPAIGN, SzType.STANDARD, "Pocket I Ching Oracle"),
    Sz012("precision-food-scale", SzGroup.FIRST_CAMPAIGN, SzType.STANDARD, "Precision Food Scale"),
    Sz013("cryptocurrency-deposit-terminal", SzGroup.FIRST_CAMPAIGN, SzType.STANDARD, "Cryptocurrency Deposit Terminal"),
    Sz004("pollution-sensing-smart-window", SzGroup.FIRST_CAMPAIGN, SzType.STANDARD, "Pollution-Sensing Smart Window"),
    Sz026("traffic-signal", SzGroup.FIRST_CAMPAIGN, SzType.STANDARD, "Traffic Signal"),
    Sz022("meat-based-printer", SzGroup.FIRST_CAMPAIGN, SzType.STANDARD, "Meat-Based Printer"),
    Sz031("electronic-door-lock", SzGroup.FIRST_CAMPAIGN, SzType.STANDARD, "Electronic Door Lock"),
    Sz021("deep-sea-sensor-grid", SzGroup.FIRST_CAMPAIGN, SzType.STANDARD, "Deep Sea Sensor Grid"),
    Sz019("spoiler-blocking-headphones", SzGroup.FIRST_CAMPAIGN, SzType.STANDARD, "Spoiler Blocking Headphones"),
    Sz018("color-coordinating-shoes", SzGroup.FIRST_CAMPAIGN, SzType.STANDARD, "Color Coordinating Shoes"),
    Sz020("airline-cocktail-mixer", SzGroup.FIRST_CAMPAIGN, SzType.STANDARD, "Airline Cocktail Mixer"),
    Sz027("safetynet-tracking-badge", SzGroup.FIRST_CAMPAIGN, SzType.STANDARD, "Safetynet Tracking Badge"),

    Sz038("cold-storage-robot", SzGroup.SECOND_CAMPAIGN, SzType.STANDARD, "Cold Storage Robot"),
    Sz039("scientific-chronometer", SzGroup.SECOND_CAMPAIGN, SzType.STANDARD, "Scientific Chronometer"),
    Sz040("automatic-pet-feeder", SzGroup.SECOND_CAMPAIGN, SzType.STANDARD, "Automatic Pet Feeder"),
    Sz041("electronic-practice-target", SzGroup.SECOND_CAMPAIGN, SzType.STANDARD, "Electronic Practice Target"),
    Sz042EZ("kelp-harvesting-robot", SzGroup.SECOND_CAMPAIGN, SzType.STANDARD, "Kelp Harvesting Robot"),
    Sz043("sushi-making-robot", SzGroup.SECOND_CAMPAIGN, SzType.STANDARD, "Sushi-Making Robot"),
    Sz044("thorium-reactor-status-monitor", SzGroup.SECOND_CAMPAIGN, SzType.STANDARD, "Thorium Reactor Status Monitor"),
    Sz045("brain-computer-interface", SzGroup.SECOND_CAMPAIGN, SzType.STANDARD, "Brain-Computer Interface"),
    Sz046("cellular-scaffold-printer", SzGroup.SECOND_CAMPAIGN, SzType.STANDARD, "Cellular Scaffold Printer"),
    Sz047("neural-processor-logic-board", SzGroup.SECOND_CAMPAIGN, SzType.STANDARD, "Neural Processor Logic Board"),

    Sz036("shenzhen-io-trailer", SzGroup.BONUS_PUZZLES, SzType.STANDARD, "Shenzhen I/O Trailer (0451)"),
    Sz042("aquatic-harvesting-robot", SzGroup.BONUS_PUZZLES, SzType.STANDARD, "Aquatic Harvesting Robot (3113)"),
    Sz007("personal-sandwich-assembler", SzGroup.BONUS_PUZZLES, SzType.STANDARD, "Personal Sandwich Assembler (2241)");

    private final String id;
    private final SzGroup group;
    private final SzType type;
    private final String displayName;
    private final List<SzCategory> supportedCategories;
    private final String link;

    SzPuzzle(String id, SzGroup group, SzType type, String displayName) {
        this.id = id;
        this.group = group;
        this.type = type;
        this.displayName = displayName;
        this.supportedCategories = Arrays.stream(SzCategory.values())
                                         .filter(c -> c.getSupportedTypes().contains(type))
                                         .toList();
        this.link = "https://zlbb.faendir.com/sz/" + id;
    }

    @NotNull
    public static SingleParseResult<SzPuzzle> parsePuzzle(@NotNull String name) {
        return UtilsKt.getSingleMatchingPuzzle(SzPuzzle.values(), name);
    }
}
