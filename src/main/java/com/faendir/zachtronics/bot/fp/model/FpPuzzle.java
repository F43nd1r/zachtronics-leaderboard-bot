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

package com.faendir.zachtronics.bot.fp.model;

import com.faendir.discord4j.command.parse.SingleParseResult;
import com.faendir.zachtronics.bot.model.Puzzle;
import com.faendir.zachtronics.bot.utils.UtilsKt;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@Getter
public enum FpPuzzle implements Puzzle<FpCategory> {
    NORMAL_1_1(1, FpGroup.NORMAL_CAMPAIGN, FpType.STANDARD, "1-1"),
    NORMAL_1_2(2, FpGroup.NORMAL_CAMPAIGN, FpType.STANDARD, "1-2"),
    NORMAL_1_3(3, FpGroup.NORMAL_CAMPAIGN, FpType.STANDARD, "1-3"),
    NORMAL_2_1(4, FpGroup.NORMAL_CAMPAIGN, FpType.STANDARD, "2-1"),
    NORMAL_2_2(5, FpGroup.NORMAL_CAMPAIGN, FpType.STANDARD, "2-2"),
    NORMAL_2_3(6, FpGroup.NORMAL_CAMPAIGN, FpType.STANDARD, "2-3"),
    NORMAL_3_1(7, FpGroup.NORMAL_CAMPAIGN, FpType.STANDARD, "3-1"),
    NORMAL_3_2(8, FpGroup.NORMAL_CAMPAIGN, FpType.STANDARD, "3-2"),
    NORMAL_3_3(11, FpGroup.NORMAL_CAMPAIGN, FpType.STANDARD, "3-3"),
    NORMAL_4_1(10, FpGroup.NORMAL_CAMPAIGN, FpType.STANDARD, "4-1"),
    NORMAL_4_2(9, FpGroup.NORMAL_CAMPAIGN, FpType.STANDARD, "4-2"),
    NORMAL_4_3(12, FpGroup.NORMAL_CAMPAIGN, FpType.STANDARD, "4-3"),
    NORMAL_5_1(13, FpGroup.NORMAL_CAMPAIGN, FpType.STANDARD, "5-1"),
    NORMAL_5_2(14, FpGroup.NORMAL_CAMPAIGN, FpType.STANDARD, "5-2"),
    NORMAL_5_3(15, FpGroup.NORMAL_CAMPAIGN, FpType.STANDARD, "5-3"),

    ADDITIONAL_1_1(17, FpGroup.ADDITIONAL_CAMPAIGN, FpType.STANDARD, "Additional 1-1"),
    ADDITIONAL_1_2(18, FpGroup.ADDITIONAL_CAMPAIGN, FpType.STANDARD, "Additional 1-2"),
    ADDITIONAL_1_3(19, FpGroup.ADDITIONAL_CAMPAIGN, FpType.STANDARD, "Additional 1-3"),
    ADDITIONAL_2_1(20, FpGroup.ADDITIONAL_CAMPAIGN, FpType.STANDARD, "Additional 2-1"),
    ADDITIONAL_2_2(21, FpGroup.ADDITIONAL_CAMPAIGN, FpType.STANDARD, "Additional 2-2"),
    ADDITIONAL_2_3(22, FpGroup.ADDITIONAL_CAMPAIGN, FpType.STANDARD, "Additional 2-3"),
    ADDITIONAL_3_1(23, FpGroup.ADDITIONAL_CAMPAIGN, FpType.STANDARD, "Additional 3-1"),
    ADDITIONAL_3_2(24, FpGroup.ADDITIONAL_CAMPAIGN, FpType.STANDARD, "Additional 3-2"),
    ADDITIONAL_3_3(25, FpGroup.ADDITIONAL_CAMPAIGN, FpType.STANDARD, "Additional 3-3"),
    ADDITIONAL_4_1(26, FpGroup.ADDITIONAL_CAMPAIGN, FpType.STANDARD, "Additional 4-1"),
    ADDITIONAL_4_2(27, FpGroup.ADDITIONAL_CAMPAIGN, FpType.STANDARD, "Additional 4-2"),
    ADDITIONAL_4_3(28, FpGroup.ADDITIONAL_CAMPAIGN, FpType.STANDARD, "Additional 4-3"),
    ADDITIONAL_5_1(29, FpGroup.ADDITIONAL_CAMPAIGN, FpType.STANDARD, "Additional 5-1"),
    ADDITIONAL_5_2(30, FpGroup.ADDITIONAL_CAMPAIGN, FpType.STANDARD, "Additional 5-2"),

    PUZZLE_EDITOR(16, FpGroup.ADDITIONAL_CAMPAIGN, FpType.EDITOR, "Puzzle Editor");

    private final int id;
    private final FpGroup group;
    private final FpType type;
    private final String displayName;
    private final List<FpCategory> supportedCategories;
    private final String link;

    FpPuzzle(int id, FpGroup group, FpType type, String displayName) {
        this.id = id;
        this.group = group;
        this.type = type;
        this.displayName = displayName;
        this.supportedCategories = Arrays.stream(FpCategory.values())
                                         .filter(c -> c.getSupportedTypes().contains(type))
                                         .toList();
        this.link = "https://zlbb.faendir.com/fp/" + name();
    }

    @NotNull
    public static SingleParseResult<FpPuzzle> parsePuzzle(@NotNull String name) {
        return UtilsKt.getSingleMatchingPuzzle(FpPuzzle.values(), name);
    }
}
