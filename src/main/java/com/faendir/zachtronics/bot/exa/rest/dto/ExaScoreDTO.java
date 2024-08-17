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

package com.faendir.zachtronics.bot.exa.rest.dto;

import com.faendir.zachtronics.bot.exa.model.ExaScore;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Value
public class ExaScoreDTO {
    int cycles;
    int size;
    int activity;

    boolean cheesy;

    @NotNull
    public static ExaScoreDTO fromScore(@NotNull ExaScore score) {
        return new ExaScoreDTO(score.getCycles(), score.getSize(), score.getActivity(), score.isCheesy());
    }
}

