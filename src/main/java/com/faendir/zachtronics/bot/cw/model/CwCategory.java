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

package com.faendir.zachtronics.bot.cw.model;

import com.faendir.zachtronics.bot.model.CategoryJava;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static com.faendir.zachtronics.bot.cw.model.CwType.STANDARD;

@Getter
public enum CwCategory implements CategoryJava<CwCategory, CwScore, CwMetric, CwType> {
    SIZE("Size", List.of(CwMetric.SIZE, CwMetric.FOOTPRINT, CwMetric.WIDTH), 0b10),
    FOOTPRINT("Footprint", List.of(CwMetric.FOOTPRINT, CwMetric.SIZE, CwMetric.WIDTH), 0b01);

    /** contains <tt>HxW/FF</tt> plus a bunch of <tt>*</tt> most likely */
    static final String[] FORMAT_STRINGS = {"%dx%d%s%dF",
                                            "%dx%d%s**%dF**",
                                            "**%dx%d**%s%dF"};

    private final String displayName;
    @Accessors(fluent = true)
    private final List<CwMetric> metrics;
    private final Comparator<CwScore> scoreComparator;
    private final Set<CwType> supportedTypes = Collections.singleton(STANDARD);
    private final int scoreFormatId;

    CwCategory(String displayName, @NotNull List<CwMetric> metrics, int scoreFormatId) {
        this.displayName = displayName;
        this.metrics = metrics;
        this.scoreComparator = makeCategoryComparator(metrics);
        this.scoreFormatId = scoreFormatId;
    }

    @Override
    public boolean supportsScore(@NotNull CwScore score) {
        return true;
    }
}
