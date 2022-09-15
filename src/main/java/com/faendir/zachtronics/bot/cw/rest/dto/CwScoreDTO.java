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

package com.faendir.zachtronics.bot.cw.rest.dto;

import com.faendir.zachtronics.bot.cw.model.CwScore;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Value
public class CwScoreDTO {
    int width;
    int height;
    int footprint;

    @NotNull
    public static CwScoreDTO fromScore(@NotNull CwScore score) {
        return new CwScoreDTO(score.getWidth(), score.getHeight(), score.getFootprint());
    }
}

