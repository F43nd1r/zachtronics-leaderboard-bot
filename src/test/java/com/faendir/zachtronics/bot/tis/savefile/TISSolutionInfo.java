/*
 * Copyright (c) 2024
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

package com.faendir.zachtronics.bot.tis.savefile;

import lombok.Data;

/**
 * This class holds the properties of a specific save slot, custom extensions included
 */
@Data
class TISSolutionInfo {
    Integer cycles;
    Integer instructions;
    Integer nodes;

    String flags;

    boolean hasScore() {
        return cycles != null && instructions != null && nodes != null;
    }
}
