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

package com.faendir.zachtronics.bot.fc.model;

import com.faendir.zachtronics.bot.model.Puzzle;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum FcPuzzle implements Puzzle<FcCategory> {
    TWO_TWELVE(1, "2twelve", "2Twelve"),
    HOT_POCKET(2, "original-hot-pocket-experience", "Original Hot Pocket Experience"),
    WINE_OCLOCK(3, "wine-oclock", "Wine O'Clock"),
    MUMBAI_CHAAT(4, "mumbai-chaat", "Mumbai Chaat"),
    MR_CHILLY(5, "mr.-chilly", "Mr. Chilly"),
    KAZAN(6, "kazan", "KAZAN"),
    SODA_TRENCH(7, "soda-trench", "Soda Trench"),
    ROSIES_DOUGHNUTS(8, "rosies-doughnuts", "Rosie's Doughnuts"),
    ON_THE_FRIED_SIDE(9, "on-the-fried-side", "On the Fried Side"),
    SWEET_HEAT_BBQ(10, "sweet-heat-bbq", "Sweet Heat BBQ"),
    THE_WALRUS(11, "the-walrus", "The Walrus"),
    MEAT_3(12, "meat+3", "Meat+3"),
    CAFE_TRISTE(13, "cafe-triste", "Cafe Triste"),
    THE_COMMISSARY(14, "the-commissary", "The Commissary"),
    DA_WINGS(15, "da-wings", "Da Wings"),
    BREAKSIDE_GRILL(16, "breakside-grill", "Breakside Grill"),
    CHAZ_CHEDDAR(17, "chaz-cheddar", "Chaz Cheddar"),
    HALF_CAFF_COFFEE(18, "half-caff-coffee", "Half Caff Coffee"),
    MILDREDS_NOOK(19, "mildreds-nook", "Mildred's Nook"),
    BELLYS(20, "bellys", "Belly's"),
    SUSHI_YEAH(21, "sushi-yeah!", "Sushi Yeah!");

    private final int number;
    private final String id;
    private final FcGroup group;
    private final FcType type;
    private final String displayName;
    private final List<FcCategory> supportedCategories;
    private final String link;

    FcPuzzle(int number, String id, String displayName) {
        this.number = number;
        this.id = id;
        this.group = FcGroup.CAMPAIGN;
        this.type = FcType.STANDARD;
        this.displayName = displayName;
        this.supportedCategories = Arrays.stream(FcCategory.values())
                                         .filter(c -> c.getSupportedTypes().contains(type))
                                         .toList();
        this.link = "https://zlbb.faendir.com/fc/" + name();
    }

}
