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

package com.faendir.zachtronics.bot.sc.validator;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 <pre>{
 "level_name": "Fuming Nitric Acid",
 "resnet_id": [3, 7, 2], # null for main game
 "cycles": 115,
 "reactors": 1,
 "symbols": 6,
 "author": "12345ieee",
 "solution_name": "s",
 "precog": false,
 "precogExplanation": "PrecogError(\"Solution is precognitive;...",
 "error": "ReactionError(\"Cycle 209: Reactor 1: Collision between molecules.\")"
 }</pre>
 */
@Value
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SChemResult {
    @NotNull String levelName;
    @Nullable int[] resnetId;
    int cycles;
    int reactors;
    int symbols;
    @NotNull String author;
    @Nullable String solutionName;
    @Nullable Boolean precog;
    @Nullable String precogExplanation;
    @Nullable String error;
}
