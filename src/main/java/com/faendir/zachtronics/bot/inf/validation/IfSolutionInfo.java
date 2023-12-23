/*
 * Copyright (c) 2023
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

import lombok.Data;

/**
 * A solution file is of the form:
 * <pre>
 * Best.1-1.Blocks = 44
 * Best.1-1.Cycles = 44
 * Best.1-1.Footprint = 47
 * InputRate.1-1.1 = 1
 * Last.1-1.1.Blocks = 44
 * Last.1-1.1.Cycles = 44
 * Last.1-1.1.Footprint = 47
 * Solution.1-1.1 = AwAAAAAAAAA=
 * </pre>
 *
 * this class holds the properties of a specific save slot, doesn't hold the best of a whole level
 */
@Data
class IfSolutionInfo {
    Integer inputRate;
    Integer blocks;
    Integer cycles;
    Integer footprint;
    String solution;

    boolean hasData() {
        return inputRate != null && solution != null;
    }

    boolean hasScore() {
        return blocks != null && cycles != null && footprint != null;
    }
}
