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

enum class OmGroup(override val displayName: String) : Group {
    CHAPTER_1("Chapter 1"),
    CHAPTER_2("Chapter 2"),
    CHAPTER_3("Chapter 3"),
    CHAPTER_4("Chapter 4"),
    CHAPTER_5("Chapter 5"),
    CHAPTER_PRODUCTION("Production Alchemy"),
    JOURNAL_I("Journal I"),
    JOURNAL_II("Journal II"),
    JOURNAL_III("Journal III"),
    JOURNAL_IV("Journal IV"),
    JOURNAL_V("Journal V"),
    JOURNAL_VI("Journal VI"),
    JOURNAL_VII("Journal VII"),
    JOURNAL_VIII("Journal VIII"),
    JOURNAL_IX("Journal IX"),
    TOURNAMENT_2019("Tournament 2019"),
    TOURNAMENT_2020("Tournament 2020"),
    TOURNAMENT_2021("Tournament 2021"),
    TOURNAMENT_2022("Tournament 2022"),
    ;
}