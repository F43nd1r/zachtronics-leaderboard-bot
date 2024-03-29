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

package com.faendir.zachtronics.bot.inf.model;

import com.faendir.zachtronics.bot.model.MetricJava;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

@Value
public class IfMetric<T extends Comparable<T>> implements MetricJava<IfScore, T> {
    public static final IfMetric<Integer> CYCLES = new IfMetric<>("C", IfScore::getCycles);
    public static final IfMetric<Integer> FOOTPRINT = new IfMetric<>("F", IfScore::getFootprint);
    public static final IfMetric<Integer> BLOCKS = new IfMetric<>("B", IfScore::getBlocks);

    public static final IfMetric<Boolean> ANY_FLAG = new IfMetric<>("", s -> false);
    public static final IfMetric<Boolean> INBOUNDS = new IfMetric<>("I", IfScore::isOutOfBounds);
    public static final IfMetric<Boolean> NO_GRA = new IfMetric<>("NG", IfScore::usesGRA);
    public static final IfMetric<Boolean> INFINITE = new IfMetric<>("I", IfScore::isFinite);
    public static final IfMetric<Boolean> NO_OOB_GRA = new IfMetric<>("N", s -> s.isOutOfBounds() || s.usesGRA());
    public static final IfMetric<Boolean> NO_FLAGS = new IfMetric<>("N", s -> s.isOutOfBounds() || s.usesGRA() || s.isFinite());

    @NotNull String displayName;
    @NotNull Function<IfScore, T> extract;
}
