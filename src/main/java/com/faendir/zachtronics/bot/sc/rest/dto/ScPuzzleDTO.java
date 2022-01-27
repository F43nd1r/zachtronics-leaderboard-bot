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

package com.faendir.zachtronics.bot.sc.rest.dto;

import com.faendir.zachtronics.bot.sc.model.ScPuzzle;
import kotlin.jvm.internal.Intrinsics;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Value
public class ScPuzzleDTO {
    @NotNull String id;
    @NotNull String displayName;
    @NotNull ScGroupDTO group;
    @NotNull String type;

    @NotNull
    public static ScPuzzleDTO fromPuzzle(@NotNull ScPuzzle puzzle) {
        return new ScPuzzleDTO(puzzle.name(), puzzle.getDisplayName(), ScGroupDTO.fromGroup(puzzle.getGroup()), puzzle.getType().name());
    }
}
