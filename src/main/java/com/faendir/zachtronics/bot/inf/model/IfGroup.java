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

package com.faendir.zachtronics.bot.inf.model;

import com.faendir.zachtronics.bot.model.Group;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IfGroup implements Group {

    ZONE_1("Proving Grounds"),
    ZONE_2("Skydock 19"),
    ZONE_3("Resource Site 526.81"),
    ZONE_4("Production Zone 2"),
    ZONE_5("Resource Site 338.11"),
    ZONE_6("Resource Site 902.42"),
    ZONE_7("The Heist"),
    ZONE_8("Production Zone 1"),
    ZONE_9("Atropos Station"),
    ZONE_10("The Homeward Fleet");

    private final String displayName;
}
