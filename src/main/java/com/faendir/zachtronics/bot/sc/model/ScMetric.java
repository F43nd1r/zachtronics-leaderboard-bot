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

package com.faendir.zachtronics.bot.sc.model;

import com.faendir.zachtronics.bot.model.MetricJava;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

@Getter
@RequiredArgsConstructor
public enum ScMetric implements MetricJava<ScScore> {
    CYCLES("C", ScScore::getCycles),
    REACTORS("R", ScScore::getReactors),
    SYMBOLS("S", ScScore::getSymbols),
    ANY_FLAG("", s -> false),
    NO_BUGS("NB", ScScore::isBugged),
    NO_PRECOG("NP", ScScore::isPrecognitive),
    NO_FLAGS("NBP", s -> s.isBugged() || s.isPrecognitive());

    @NotNull private final String displayName;
    @NotNull private final Function<ScScore, Comparable<?>> extract;
}
