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

package com.faendir.zachtronics.bot.exa.model;

import com.faendir.zachtronics.bot.model.MetricJava;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

@Value
public class ExaMetric<T extends Comparable<T>> implements MetricJava<ExaScore, T> {
    public static final ExaMetric<Integer> CYCLES = new ExaMetric<>("C", ExaScore::getCycles);
    public static final ExaMetric<Integer> SIZE = new ExaMetric<>("S", ExaScore::getSize);
    public static final ExaMetric<Integer> ACTIVITY = new ExaMetric<>("A", ExaScore::getActivity);

    public static final ExaMetric<Boolean> LEGIT = new ExaMetric<>("", s -> !s.isCheesy());
    public static final ExaMetric<Boolean> CAN_CHEESE = new ExaMetric<>("c", s -> true);

    @NotNull String displayName;
    @NotNull Function<ExaScore, T> extract;
}