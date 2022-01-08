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

package com.faendir.zachtronics.bot.sc.model;

import com.faendir.zachtronics.bot.model.Metric;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.ToIntFunction;

@Getter
@RequiredArgsConstructor
public enum ScMetric implements Metric {
    CYCLES("C", ScScore::getCycles),
    REACTORS("R", ScScore::getReactors),
    SYMBOLS("S", ScScore::getSymbols),
    ANY_FLAG("", null),
    NO_BUGS("NB", null),
    NO_PRECOG("NP", null),
    NO_FLAGS("NBP", null);

    private final String displayName;
    private final ToIntFunction<ScScore> extract;
}
