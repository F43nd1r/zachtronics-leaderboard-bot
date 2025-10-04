/*
 * Copyright (c) 2025
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

package com.faendir.zachtronics.bot.kz.model;

import com.faendir.zachtronics.bot.model.Puzzle;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.faendir.zachtronics.bot.kz.model.KzGroup.BONUS_CAMPAIGN;
import static com.faendir.zachtronics.bot.kz.model.KzGroup.NORMAL_CAMPAIGN;

@Getter
public enum KzPuzzle implements Puzzle<KzCategory> {
    CORPORATE_BINDER(6, NORMAL_CAMPAIGN, "Corporate binder"),
    PORTABLE_RADIO(2, NORMAL_CAMPAIGN, "Portable radio"),
    WORLD_CLOCK(1, NORMAL_CAMPAIGN, "World clock"),
    SPORT_WATCH(3, NORMAL_CAMPAIGN, "Sport watch"),
    FIELD_BINOCULARS(4, NORMAL_CAMPAIGN, "Field binoculars"),
    DEFENDER_BOT(5, NORMAL_CAMPAIGN, "Defender bot"),

    ONIGIRI(11, NORMAL_CAMPAIGN, "Onigiri"),
    MAKI_SUSHI(14, NORMAL_CAMPAIGN, "Maki sushi"),
    SANDWICH(13, NORMAL_CAMPAIGN, "Sandwich"),
    NIGIRI_SUSHI(16, NORMAL_CAMPAIGN, "Nigiri sushi"),
    HAMBURGER(12, NORMAL_CAMPAIGN, "Hamburger"),
    KATSU_CURRY(15, NORMAL_CAMPAIGN, "Katsu curry"),

    COMPACT_CAMERA(22, NORMAL_CAMPAIGN, "Compact camera"),
    POCKET_CALCULATOR(21, NORMAL_CAMPAIGN, "Pocket calculator"),
    HANDHELD_GAME(23, NORMAL_CAMPAIGN, "Handheld game"),
    PERSONAL_COMPUTER(25, NORMAL_CAMPAIGN, "Personal computer"),
    CAMCORDER(24, NORMAL_CAMPAIGN, "Camcorder"),

    RICE_COOKER(34, NORMAL_CAMPAIGN, "Rice cooker"),
    BIDET_SEAT(36, NORMAL_CAMPAIGN, "Bidet seat"),
    COLOR_TELEVISION(31, NORMAL_CAMPAIGN, "Color television"),
    VIDEO_RECORDER(35, NORMAL_CAMPAIGN, "Video recorder"),
    COFFEE_MAKER(32, NORMAL_CAMPAIGN, "Coffee maker"),

    TUBE_SOCK(41, NORMAL_CAMPAIGN, "Tube sock"),
    JOGGING_SHORTS(44, NORMAL_CAMPAIGN, "Jogging shorts"),
    ATHLETIC_SHIRT(43, NORMAL_CAMPAIGN, "Athletic shirt"),
    ELEGANT_HANDBAG(42, NORMAL_CAMPAIGN, "Elegant handbag"),
    SPRING_DRESS(45, NORMAL_CAMPAIGN, "Spring dress"),

    CLAW_MACHINE(51, NORMAL_CAMPAIGN, "Claw machine"),
    CAPSULE_MACHINE(52, NORMAL_CAMPAIGN, "Capsule machine"),
    RC_CAR(53, NORMAL_CAMPAIGN, "RC car"),
    PACHINKO_MACHINE(55, NORMAL_CAMPAIGN, "Pachinko machine"),
    ARCADE_GAME(54, NORMAL_CAMPAIGN, "Arcade game"),

    MOTOR_SCOOTER(61, NORMAL_CAMPAIGN, "Motor scooter"),

    SUPER_DEFENDER_BOT(104, BONUS_CAMPAIGN, "Super defender bot"),
    SYNTHESIZER(101, BONUS_CAMPAIGN, "Synthesizer"),
    ARCHERY_TARGET(109, BONUS_CAMPAIGN, "Archery target"),
    ELECTRIC_GUITAR(103, BONUS_CAMPAIGN, "Electric guitar"),
    CHESS_COMPUTER(107, BONUS_CAMPAIGN, "Chess computer"),
    BOOMBOX(102, BONUS_CAMPAIGN, "Boombox"),
    DOLLHOUSE_KITCHEN(108, BONUS_CAMPAIGN, "Dollhouse kitchen"),
    SLOT_CAR_TRACK(105, BONUS_CAMPAIGN, "Slot car track"),
    TOY_CASH_REGISTER(106, BONUS_CAMPAIGN, "Toy cash register"),

    SANDWICH_ALT(213, BONUS_CAMPAIGN, "Sandwich (Alt)"),
    POCKET_CALCULATOR_ALT(221, BONUS_CAMPAIGN, "Pocket Calculator (Alt)"),
    HANDHELD_GAME_ALT(223, BONUS_CAMPAIGN, "Handheld Game (Alt)"),
    VIDEO_RECORDER_ALT(235, BONUS_CAMPAIGN, "Video Recorder (Alt)"),
    BIDET_SEAT_ALT(236, BONUS_CAMPAIGN, "Bidet Seat (Alt)"),
    TUBE_SOCK_ALT(241, BONUS_CAMPAIGN, "Tube Sock (Alt)"),
    ELEGANT_HANDBAG_ALT(242, BONUS_CAMPAIGN, "Elegant Handbag (Alt)"),
    ATHLETIC_SHIRT_ALT(243, BONUS_CAMPAIGN, "Athletic Shirt (Alt)"),
    SPRING_OUTFIT_ALT(245, BONUS_CAMPAIGN, "Spring Outfit (Alt)"),
    ARCADE_CONTROLS_ALT(254, BONUS_CAMPAIGN, "Arcade Controls (Alt)"),
    PACHINKO_MACHINE_ALT(255, BONUS_CAMPAIGN, "Pachinko Machine (Alt)"),
    CHESS_COMPUTER_ALT(307, BONUS_CAMPAIGN, "Chess Computer (Alt)"),
    ;

    private final int id;
    private final KzGroup group;
    private final KzType type = KzType.STANDARD;
    private final String prefix;
    private final String displayName;
    private final List<KzCategory> supportedCategories = List.of(KzCategory.values());
    private final String link;

    KzPuzzle(int id, KzGroup group, @NotNull String displayName) {
        this.id = id;
        this.group = group;
        this.prefix = displayName.toLowerCase().replace(' ', '-').replaceAll("[()]", "");
        this.displayName = displayName;
        this.link = "https://zlbb.faendir.com/kz/" + name();
    }

}
