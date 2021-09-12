/*
 * Copyright (c) 2021
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

import com.faendir.zachtronics.bot.model.Category;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

import static com.faendir.zachtronics.bot.sz.model.SzCategory.SzScoreFormatStrings.*;
import static com.faendir.zachtronics.bot.sz.model.SzType.STANDARD;
import static com.faendir.zachtronics.bot.utils.Utils.makeComparator3;

@RequiredArgsConstructor
public enum SzCategory implements Category {
    CP("CP", makeComparator3(SzScore::getCost, SzScore::getPower, SzScore::getLines), F100, 101),
    CL("CL", makeComparator3(SzScore::getCost, SzScore::getLines, SzScore::getPower), F100, 102),

    PC("PC", makeComparator3(SzScore::getPower, SzScore::getCost, SzScore::getLines), F010, 201),
    PL("PL", makeComparator3(SzScore::getPower, SzScore::getLines, SzScore::getCost), F010, 202),

    LC("LC", makeComparator3(SzScore::getLines, SzScore::getCost, SzScore::getPower), F001, 301),
    LP("LP", makeComparator3(SzScore::getLines, SzScore::getPower, SzScore::getCost), F001, 302);

    @Getter
    private final String displayName;
    @Getter
    private final Comparator<SzScore> scoreComparator;
    private final Set<SzType> supportedTypes = Collections.singleton(STANDARD);
    @Getter
    private final String formatStringLb;
    @Getter
    private final int repoSuffix;

    public boolean supportsPuzzle(@NotNull SzPuzzle puzzle) {
        return supportedTypes.contains(puzzle.getType());
    }

    public boolean supportsScore(@NotNull SzScore score) {
        return true;
    }

    static class SzScoreFormatStrings {
        static final String F100 = "**%d**/%d/%d";
        static final String F010 = "%d/**%d**/%d";
        static final String F001 = "%d/%d/**%d**";
    }
}
