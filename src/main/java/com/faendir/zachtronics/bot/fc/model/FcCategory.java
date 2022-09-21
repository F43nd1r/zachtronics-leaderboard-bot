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

package com.faendir.zachtronics.bot.fc.model;

import com.faendir.zachtronics.bot.model.CategoryJava;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static com.faendir.zachtronics.bot.fc.model.FcMetric.*;
import static com.faendir.zachtronics.bot.fc.model.FcType.STANDARD;

@Getter
public enum FcCategory implements CategoryJava<FcCategory, FcScore, FcMetric, FcType> {
    TCS("TCS", List.of(TIME, COST, SUM_TIMES, WIRES), 0b1000),
    TSW("TSW", List.of(TIME, SUM_TIMES, WIRES, COST), 0b1000),

    CTS("CTS", List.of(COST, TIME, SUM_TIMES, WIRES), 0b0100),
    CWT("CWT", List.of(COST, WIRES, TIME, SUM_TIMES), 0b0100),

    STC("STC", List.of(SUM_TIMES, TIME, COST, WIRES), 0b0010),
    SWC("SWC", List.of(SUM_TIMES, WIRES, COST, TIME), 0b0010),

    WTC("WTC", List.of(WIRES, TIME, COST, SUM_TIMES), 0b0001),
    WCT("WCT", List.of(WIRES, COST, TIME, SUM_TIMES), 0b0001);

    /** contains <tt>%dT%s%dk%s%dS%s%dW</tt> plus a bunch of <tt>*</tt> most likely */
    static final String[] FORMAT_STRINGS = {"%dT%s%dk%s%dS%s%dW",
                                            "%dT%s%dk%s%dS%s**%dW**",
                                            "%dT%s%dk%s**%dS**%s%dW", null,
                                            "%dT%s**%dk**%s%dS%s%dW", null, null, null,
                                            "**%dT**%s%dk%s%dS%s%dW"};

    private final String displayName;
    @Accessors(fluent = true)
    private final List<FcMetric> metrics;
    private final Comparator<FcScore> scoreComparator;
    private final Set<FcType> supportedTypes = Collections.singleton(STANDARD);
    private final int scoreFormatId;

    FcCategory(String displayName, @NotNull List<FcMetric> metrics, int scoreFormatId) {
        this.displayName = displayName;
        this.metrics = metrics;
        this.scoreComparator = makeCategoryComparator(metrics);
        this.scoreFormatId = scoreFormatId;
    }

    @Override
    public boolean supportsScore(@NotNull FcScore score) {
        return true;
    }
}
