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
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

@Value
public class ScMetric<T extends Comparable<T>> implements MetricJava<ScScore, T> {
    public static final ScMetric<Integer> CYCLES = new ScMetric<>("C", ScScore::getCycles);
    public static final ScMetric<Integer> REACTORS = new ScMetric<>("R", ScScore::getReactors);
    public static final ScMetric<Integer> SYMBOLS = new ScMetric<>("S", ScScore::getSymbols);

    public static final ScMetric<Boolean> ANY_FLAG = new ScMetric<>("", s -> false);
    public static final ScMetric<Boolean> NO_BUGS = new ScMetric<>("NB", ScScore::isBugged);
    public static final ScMetric<Boolean> NO_PRECOG = new ScMetric<>("NP", ScScore::isPrecognitive);
    public static final ScMetric<Boolean> NO_FLAGS = new ScMetric<>("NBP", s -> s.isBugged() || s.isPrecognitive());

    @NotNull String displayName;
    @NotNull Function<ScScore, T> extract;
}
