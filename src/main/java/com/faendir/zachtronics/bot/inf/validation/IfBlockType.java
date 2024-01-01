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

package com.faendir.zachtronics.bot.inf.validation;

@SuppressWarnings("unused")
class IfBlockType {
    // basic
    static final short PLATFORM = 18;
    // movement
    static final short CONVEYOR = 1;
    static final short ROTATOR_CW = 39;
    static final short ROTATOR_CCW = 25;
    static final short LIFTER = 138;
    // modification
    static final short WELDER_A = 8;
    static final short WELDER_B = 131;
    static final short EVISCERATOR = 17;
    // special
    static final short CONVEYOR_INV = 0;
    static final short LASER = 9;
    static final short COUNTER_A = 15;
    static final short COUNTER_B = 16;
    // logic
    static final short SENSOR_A = 5;
    static final short SENSOR_B = 149;
    static final short PUSHER = 4;
    static final short BLOCKER = 35;
    static final short CONDUIT = 2;
    static final short TOGGLE = 152; // boss only

    private IfBlockType() {}
}
