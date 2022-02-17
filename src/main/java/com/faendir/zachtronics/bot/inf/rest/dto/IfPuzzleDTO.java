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

package com.faendir.zachtronics.bot.inf.rest.dto;

import com.faendir.zachtronics.bot.inf.model.IfPuzzle;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Value
public class IfPuzzleDTO {
    @NotNull String id;
    @NotNull String displayName;
    @NotNull IfGroupDTO group;
    @NotNull String type;

    @NotNull
    public static IfPuzzleDTO fromPuzzle(@NotNull IfPuzzle puzzle) {
        return new IfPuzzleDTO(puzzle.getId(), puzzle.getDisplayName(), IfGroupDTO.fromGroup(puzzle.getGroup()), puzzle.getType().name());
    }
}
