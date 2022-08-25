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

package com.faendir.zachtronics.bot.fp.validator;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

/**
 <pre>{
 "level_name": "4-3",
 "level_id": 12,
 "slot_id": 0,
 "solution": "eNqtjl0KgEAIhGc0todY6P5X7BClbNLgcwOKfv4wlwMnAGKJVd+hLI6IrWBN5wtt9FNtpv5j2zTLpCCcwAV4Tkf3xQZtxyfij0atPoboBXc=",
 "is_correct": true,
 "num_rules": 10,
 "num_rules_conditional": 3,
 "num_frames": 10,
 "is_stable": true,
 "num_waste": 0,
 "is_wasteful": false
 }</pre>
 */
@Value
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SimResult {
    @NotNull String levelName;
    int levelId;
    int slotId;
    /** present only if <tt>--include-solution</tt> is given */
    @NotNull String solution;
    boolean isCorrect;
    int numRules;
    int numRulesConditional;
    int numFrames;
    boolean isStable;
    int numWaste;
    boolean isWasteful;
}
