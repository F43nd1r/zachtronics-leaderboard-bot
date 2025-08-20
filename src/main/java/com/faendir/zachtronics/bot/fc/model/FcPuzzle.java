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
    TWO_TWELVE(1, "2twelve", FcGroup.CAMPAIGN, FcType.STANDARD, "2Twelve"),
    HOT_POCKET(2, "original-hot-pocket-experience", FcGroup.CAMPAIGN, FcType.STANDARD, "Original Hot Pocket Experience"),
    WINE_OCLOCK(3, "wine-oclock", FcGroup.CAMPAIGN, FcType.STANDARD, "Wine O'Clock"),
    MUMBAI_CHAAT(4, "mumbai-chaat", FcGroup.CAMPAIGN, FcType.STANDARD, "Mumbai Chaat"),
    MR_CHILLY(5, "mr.-chilly", FcGroup.CAMPAIGN, FcType.STANDARD, "Mr. Chilly"),
    KAZAN(6, "kazan", FcGroup.CAMPAIGN, FcType.STANDARD, "KAZAN"),
    SODA_TRENCH(7, "soda-trench", FcGroup.CAMPAIGN, FcType.STANDARD, "Soda Trench"),
    ROSIES_DOUGHNUTS(8, "rosies-doughnuts", FcGroup.CAMPAIGN, FcType.STANDARD, "Rosie's Doughnuts"),
    ON_THE_FRIED_SIDE(9, "on-the-fried-side", FcGroup.CAMPAIGN, FcType.STANDARD, "On the Fried Side"),
    SWEET_HEAT_BBQ(10, "sweet-heat-bbq", FcGroup.CAMPAIGN, FcType.STANDARD, "Sweet Heat BBQ"),
    THE_WALRUS(11, "the-walrus", FcGroup.CAMPAIGN, FcType.STANDARD, "The Walrus"),
    MEAT_3(12, "meat+3", FcGroup.CAMPAIGN, FcType.STANDARD, "Meat+3"),
    CAFE_TRISTE(13, "cafe-triste", FcGroup.CAMPAIGN, FcType.STANDARD, "Cafe Triste"),
    THE_COMMISSARY(14, "the-commissary", FcGroup.CAMPAIGN, FcType.STANDARD, "The Commissary"),
    DA_WINGS(15, "da-wings", FcGroup.CAMPAIGN, FcType.STANDARD, "Da Wings"),
    BREAKSIDE_GRILL(16, "breakside-grill", FcGroup.CAMPAIGN, FcType.STANDARD, "Breakside Grill"),
    CHAZ_CHEDDAR(17, "chaz-cheddar", FcGroup.CAMPAIGN, FcType.STANDARD, "Chaz Cheddar"),
    HALF_CAFF_COFFEE(18, "half-caff-coffee", FcGroup.CAMPAIGN, FcType.STANDARD, "Half Caff Coffee"),
    MILDREDS_NOOK(19, "mildreds-nook", FcGroup.CAMPAIGN, FcType.STANDARD, "Mildred's Nook"),
    BELLYS(20, "bellys", FcGroup.CAMPAIGN, FcType.STANDARD, "Belly's"),
    SUSHI_YEAH(21, "sushi-yeah!", FcGroup.CAMPAIGN, FcType.STANDARD, "Sushi Yeah!");

    private final int number;
    private final String id;
    private final FcGroup group;
    private final FcType type;
    private final String displayName;
    private final List<FcCategory> supportedCategories;
    private final String link;

    FcPuzzle(int number, String id, FcGroup group, FcType type, String displayName) {
        this.number = number;
        this.id = id;
        this.group = group;
        this.type = type;
        this.displayName = displayName;
        this.supportedCategories = Arrays.stream(FcCategory.values())
                                         .filter(c -> c.getSupportedTypes().contains(type))
                                         .toList();
        this.link = "https://zlbb.faendir.com/fc/" + name();
    }

}
