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

package com.faendir.zachtronics.bot.exa.model;

import com.faendir.zachtronics.bot.model.Puzzle;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum ExaPuzzle implements Puzzle<ExaCategory> {
    PB000(ExaGroup.MAIN_CAMPAIGN, ExaType.UNCHEESABLE, 50, "trash-world-news", "TW1"),
    PB001(ExaGroup.MAIN_CAMPAIGN, ExaType.UNCHEESABLE, 50, "trash-world-news", "TW2"),
    PB037(ExaGroup.MAIN_CAMPAIGN, ExaType.UNCHEESABLE, 50, "trash-world-news", "TW3"),
    PB002(ExaGroup.MAIN_CAMPAIGN, ExaType.UNCHEESABLE, 50, "trash-world-news", "TW4"),
    PB003B(ExaGroup.MAIN_CAMPAIGN, ExaType.STANDARD, 50, "euclids-pizza", "Pizza"),
    PB004(ExaGroup.MAIN_CAMPAIGN, ExaType.STANDARD, 50, "mitsuzen-hdi10", "Left Arm"),
    PB005(ExaGroup.MAIN_CAMPAIGN, ExaType.STANDARD, 50, "last-stop-snaxnet", "Snaxnet 1"),
    PB006B(ExaGroup.MAIN_CAMPAIGN, ExaType.STANDARD, 50, "zebros-copies", "Zebros"),
    PB007(ExaGroup.MAIN_CAMPAIGN, ExaType.UNCHEESABLE, 50, "sfcta-highway-sign-#4902", "Highway"),
    PB008(ExaGroup.MAIN_CAMPAIGN, ExaType.UNCHEESABLE, 50, "unknown-network", "UN1"),
    PB009(ExaGroup.MAIN_CAMPAIGN, ExaType.UNCHEESABLE, 75, "uc-berkeley", "Berkeley"),
    PB010B(ExaGroup.MAIN_CAMPAIGN, ExaType.STANDARD, 75, "workhouse", "Workhouse"),
    PB012(ExaGroup.MAIN_CAMPAIGN, ExaType.STANDARD, 50, "equity-first-bank", "Bank 1"),
    PB011B(ExaGroup.MAIN_CAMPAIGN, ExaType.STANDARD, 50, "mitsuzen-hdi10", "Heart"),
    PB013C(ExaGroup.MAIN_CAMPAIGN, ExaType.STANDARD, 50, "trash-world-news", "TW5"),
    PB015(ExaGroup.MAIN_CAMPAIGN, ExaType.UNCHEESABLE, 50, "tec-redshift", "Redshift"),
    PB016(ExaGroup.MAIN_CAMPAIGN, ExaType.STANDARD, 75, "digital-library-project", "Library"),
    PB040(ExaGroup.MAIN_CAMPAIGN, ExaType.STANDARD, 100, "tec-exablaster-modem", "Modem 1"),
    PB018(ExaGroup.MAIN_CAMPAIGN, ExaType.STANDARD, 75, "emersons-guide", "Emersons"),
    PB038(ExaGroup.MAIN_CAMPAIGN, ExaType.STANDARD, 75, "mitsuzen-hdi10", "Left Hand"),
    PB020(ExaGroup.MAIN_CAMPAIGN, ExaType.STANDARD, 100, "sawayama-wonderdisc", "Sawayama"),
    PB021(ExaGroup.MAIN_CAMPAIGN, ExaType.STANDARD, 75, "alliance-power-and-light", "APL"),
    PB023(ExaGroup.MAIN_CAMPAIGN, ExaType.STANDARD, 100, "xtreme-league-baseball", "XLB"),
    PB024(ExaGroup.MAIN_CAMPAIGN, ExaType.STANDARD, 100, "kings-ransom-online", "KRO"),
    PB028(ExaGroup.MAIN_CAMPAIGN, ExaType.STANDARD, 100, "kgogtv", "KGOG"),
    PB025(ExaGroup.MAIN_CAMPAIGN, ExaType.STANDARD, 75, "equity-first-bank", "Bank 2"),
    PB026B(ExaGroup.MAIN_CAMPAIGN, ExaType.STANDARD, 100, "tec-exablaster-modem", "Modem 2"),
    PB029B(ExaGroup.MAIN_CAMPAIGN, ExaType.STANDARD, 100, "last-stop-snaxnet", "Snaxnet 2"),
    PB030(ExaGroup.MAIN_CAMPAIGN, ExaType.STANDARD, 75, "mitsuzen-hdi10", "Visual Cortex"),
    PB032(ExaGroup.MAIN_CAMPAIGN, ExaType.STANDARD, 150, "holman-dynamics", "Holman"),
    PB033(ExaGroup.MAIN_CAMPAIGN, ExaType.STANDARD, 150, "u.s.-government", "USGov"),
    PB034(ExaGroup.MAIN_CAMPAIGN, ExaType.STANDARD, 75, "unknown-network", "UN2"),
    PB035B(ExaGroup.MAIN_CAMPAIGN, ExaType.STANDARD, 100, "tec-exablaster-modem", "Modem 3"),
    PB036(ExaGroup.MAIN_CAMPAIGN, ExaType.UNCHEESABLE, 150, "mitsuzen-hdi10", "Cerebral Cortex"),

    PB054(ExaGroup.BONUS_PUZZLES, ExaType.STANDARD, 150, "bloodlust-online", "mutex8021"),
    PB053(ExaGroup.BONUS_PUZZLES, ExaType.STANDARD, 100, "motor-vehicle-administration", "NthDimension"),
    PB050(ExaGroup.BONUS_PUZZLES, ExaType.STANDARD, 150, "cybermyth-studios", "Ghast"),
    PB056(ExaGroup.BONUS_PUZZLES, ExaType.STANDARD, 150, "u.s.-department-of-defense", "hydroponix"),
    PB051(ExaGroup.BONUS_PUZZLES, ExaType.STANDARD, 150, "netronics-net40-modem", "=plastered"),
    PB057(ExaGroup.BONUS_PUZZLES, ExaType.STANDARD, 150, "española-valley-high-school", "selenium_wolf"),
    PB052(ExaGroup.BONUS_PUZZLES, ExaType.STANDARD, 150, "mitsuzen-d300n", "x10x10x"),
    PB055(ExaGroup.BONUS_PUZZLES, ExaType.STANDARD, 150, "crystalair-international", "deadlock"),
    PB058(ExaGroup.BONUS_PUZZLES, ExaType.STANDARD, 100, "localhost" /* changes based on user machine host */, "Moss");

    private final ExaGroup group;
    private final ExaType type;
    private final int sizeLimit;
    private final List<ExaCategory> supportedCategories;
    /** multiple puzzles have the same prefix and some puzzles changed it (so they have 2+) */
    private final String prefix;
    private final String displayName;
    private final String link;

    ExaPuzzle(ExaGroup group, ExaType type, int sizeLimit, String prefix, String displayName) {
        this.group = group;
        this.type = type;
        this.sizeLimit = sizeLimit;
        this.supportedCategories = Arrays.stream(ExaCategory.values())
                                         .filter(c -> c.getSupportedTypes().contains(type))
                                         .toList();
        this.prefix = prefix;
        this.displayName = displayName;
        this.link = "https://zlbb.faendir.com/exa/" + name();
    }

}
