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

package com.faendir.zachtronics.bot.tis.rest.dto;

import com.faendir.zachtronics.bot.tis.model.TISPuzzle;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Value
public class TISPuzzleDTO {
    @NotNull String name;
    @NotNull String displayName;
    @NotNull TISGroupDTO group;
    @NotNull String type;

    @NotNull
    public static TISPuzzleDTO fromPuzzle(@NotNull TISPuzzle puzzle) {
        return new TISPuzzleDTO(puzzle.name(), puzzle.getDisplayName(), TISGroupDTO.fromGroup(puzzle.getGroup()), puzzle.getType().name());
    }
}
