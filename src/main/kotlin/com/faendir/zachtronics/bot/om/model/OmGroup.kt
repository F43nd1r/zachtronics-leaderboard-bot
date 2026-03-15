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

package com.faendir.zachtronics.bot.om.model

import com.faendir.zachtronics.bot.model.Group
import com.faendir.zachtronics.bot.om.model.OmCollection.CAMPAIGN
import com.faendir.zachtronics.bot.om.model.OmCollection.COMMUNITY
import com.faendir.zachtronics.bot.om.model.OmCollection.JOURNAL

enum class OmGroup(val collection: OmCollection, override val displayName: String) : Group {
    CHAPTER_1(CAMPAIGN, "Chapter I"),
    CHAPTER_2(CAMPAIGN, "Chapter II"),
    CHAPTER_3(CAMPAIGN, "Chapter III"),
    CHAPTER_4(CAMPAIGN, "Chapter IV"),
    CHAPTER_5(CAMPAIGN, "Chapter V"),
    CHAPTER_PRODUCTION(CAMPAIGN, "Appendix"),
    CHAPTER_DRM_1(DRM, "Chapter I"),
    CHAPTER_DRM_2(DRM, "Chapter II"),
    CHAPTER_DRM_3(DRM, "Chapter III"),
    JOURNAL_I(JOURNAL, "Journal I"),
    JOURNAL_II(JOURNAL, "Journal II"),
    JOURNAL_III(JOURNAL, "Journal III"),
    JOURNAL_IV(JOURNAL, "Journal IV"),
    JOURNAL_V(JOURNAL, "Journal V"),
    JOURNAL_VI(JOURNAL, "Journal VI"),
    JOURNAL_VII(JOURNAL, "Journal VII"),
    JOURNAL_VIII(JOURNAL, "Journal VIII"),
    JOURNAL_IX(JOURNAL, "Journal IX"),
    JOURNAL_X(JOURNAL, "Journal X"),
    JOURNAL_XI(JOURNAL, "Journal XI"),
    JOURNAL_XII(JOURNAL, "Journal XII"),
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