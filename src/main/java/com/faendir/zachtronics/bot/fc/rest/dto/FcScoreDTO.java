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

package com.faendir.zachtronics.bot.fc.rest.dto;

import com.faendir.zachtronics.bot.fc.model.FcScore;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Value
public class FcScoreDTO {
    int cost;
    int time;
    int sumTimes;
    int wires;

    @NotNull
    public static FcScoreDTO fromScore(@NotNull FcScore score) {
        return new FcScoreDTO(score.getCost(), score.getTime(), score.getSumTimes(), score.getWires());
    }
}

