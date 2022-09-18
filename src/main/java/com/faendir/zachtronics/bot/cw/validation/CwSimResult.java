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

package com.faendir.zachtronics.bot.cw.validation;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

/**
 <pre>  {
 "level_name": "Pulse Echo Detector",
 "level_id": 18,
 "slot_id": 2,
 "solution": "Volgograd.Solution.18.0 = ...",
 "is_correct": true,
 "is_unstable": false,
 "num_metal": 26,
 "num_bare_ntype": 10,
 "num_ntype": 15,
 "num_bare_ptype": 4,
 "num_ptype": 9,
 "num_capacitors": 2,
 "num_vias": 10,
 "num_npn_transistors": 4,
 "num_pnp_transistors": 1,
 "num_transistors": 5,
 "silicon_area": 21,
 "silicon_volume": 26,
 "total_volume": 62,
 "silicon_width": 5,
 "silicon_height": 5,
 "silicon_size": 25,
 "footprint": 28
 }</pre>
 */
@Value
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CwSimResult {
    @NotNull String levelName;
    int levelId;
    int slotId;
    /** present only if <tt>--include-solution</tt> is given */
    @NotNull String solution;
    boolean isCorrect;
    boolean isUnstable;
    int numMetal;
    int numBareNtype;
    int numNtype;
    int numBarePtype;
    int numPtype;
    int numCapacitors;
    int numVias;
    int numNpnTransistors;
    int numPnpTransistors;
    int numTransistors;
    int siliconArea;
    int siliconVolume;
    int totalVolume;
    int siliconWidth;
    int siliconHeight;
    int siliconSize;
    int footprint;
}
