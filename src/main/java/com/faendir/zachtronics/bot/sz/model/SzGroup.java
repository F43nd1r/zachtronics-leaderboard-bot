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

import com.faendir.zachtronics.bot.model.Group;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SzGroup implements Group {
    FIRST_CAMPAIGN("First Campaign", "first_campaign"),
    SECOND_CAMPAIGN("Second Campaign", "second_campaign"),
    BONUS_PUZZLES("Bonus Puzzles", "bonus_puzzles");

    private final String displayName;
    private final String repoFolder;
}
