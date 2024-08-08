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

package com.faendir.zachtronics.bot.tis.model;

import com.faendir.zachtronics.bot.model.MetricJava;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

@Value
public class TISMetric<T extends Comparable<T>> implements MetricJava<TISScore, T> {
    public static final TISMetric<Integer> CYCLES = new TISMetric<>("C", TISScore::getCycles);
    public static final TISMetric<Integer> NODES = new TISMetric<>("N", TISScore::getNodes);
    public static final TISMetric<Integer> INSTRUCTIONS = new TISMetric<>("I", TISScore::getInstructions);

    public static final TISMetric<Integer> PROD_CN = new TISMetric<>("X", s -> s.getCycles() * s.getNodes());
    public static final TISMetric<Integer> PROD_CI = new TISMetric<>("X", s -> s.getCycles() * s.getInstructions());
    public static final TISMetric<Integer> PROD_NI = new TISMetric<>("X", s -> s.getNodes() * s.getInstructions());

    public static final TISMetric<Boolean> ACHIEVEMENT = new TISMetric<>("a", TISScore::isAchievement);
    public static final TISMetric<Boolean> NOT_CHEATING = new TISMetric<>("", s -> !s.isCheating());
    public static final TISMetric<Boolean> NOT_HARDCODED = new TISMetric<>("c", s -> !s.isHardcoded());
    public static final TISMetric<Boolean> CAN_HARDCODE = new TISMetric<>("h", s -> true);

    @NotNull String displayName;
    @NotNull Function<TISScore, T> extract;
}