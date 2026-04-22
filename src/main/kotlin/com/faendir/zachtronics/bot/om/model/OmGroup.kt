/*
 * Copyright (c) 2026
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

package com.faendir.zachtronics.bot.om.model

import com.faendir.zachtronics.bot.model.Group
import com.faendir.zachtronics.bot.om.model.OmCollection.*

enum class OmGroup(val collection: OmCollection, override val displayName: String) : Group {
    CHAPTER_1(CAMPAIGN, "Chapter I"),
    CHAPTER_2(CAMPAIGN, "Chapter II"),
    CHAPTER_3(CAMPAIGN, "Chapter III"),
    CHAPTER_4(CAMPAIGN, "Chapter IV"),
    CHAPTER_5(CAMPAIGN, "Chapter V"),
    CHAPTER_PRODUCTION(CAMPAIGN, "Appendix"),
    DRM_CHAPTER_1(DRM, "DRM Chapter I"),
    DRM_CHAPTER_2(DRM, "DRM Chapter II"),
    DRM_CHAPTER_3(DRM, "DRM Chapter III"),
    JOURNAL_I(JOURNAL_XCIX, "XCIX Issue I"),
    JOURNAL_II(JOURNAL_XCIX, "XCIX Issue II"),
    JOURNAL_III(JOURNAL_XCIX, "XCIX Issue III"),
    JOURNAL_IV(JOURNAL_XCIX, "XCIX Issue IV"),
    JOURNAL_V(JOURNAL_XCIX, "XCIX Issue V"),
    JOURNAL_VI(JOURNAL_XCIX, "XCIX Issue VI"),
    JOURNAL_VII(JOURNAL_XCIX, "XCIX Issue VII"),
    JOURNAL_VIII(JOURNAL_XCIX, "XCIX Issue VIII"),
    JOURNAL_IX(JOURNAL_XCIX, "XCIX Issue IX"),
    JOURNAL_X(JOURNAL_XCIX, "XCIX Issue X"),
    JOURNAL_XI(JOURNAL_XCIX, "XCIX Issue XI"),
    JOURNAL_XII(JOURNAL_XCIX, "XCIX Issue XII"),
    JOURNAL_CVIII_I(JOURNAL_CVIII, "CVIII Issue I"),
    JOURNAL_CVIII_II(JOURNAL_CVIII, "CVIII Issue II"),
    JOURNAL_CVIII_III(JOURNAL_CVIII, "CVIII Issue III"),
    JOURNAL_CVIII_IV(JOURNAL_CVIII, "CVIII Issue IV"),
    TOURNAMENT_2019(COMMUNITY, "Tournament 2019"),
    TOURNAMENT_2020(COMMUNITY, "Tournament 2020"),
    TOURNAMENT_2021(COMMUNITY, "Tournament 2021"),
    TOURNAMENT_2022(COMMUNITY, "Tournament 2022"),
    TOURNAMENT_2023(COMMUNITY, "Tournament 2023"),
    TOURNAMENT_2024(COMMUNITY, "Tournament 2024"),
    TOURNAMENT_2025(COMMUNITY, "Tournament 2025"),
    WEEKLIES_1(COMMUNITY, "Best Of Weeklies 1"),
    ;
}
