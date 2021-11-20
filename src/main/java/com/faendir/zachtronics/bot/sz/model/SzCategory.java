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

package com.faendir.zachtronics.bot.sz.model;

import com.faendir.zachtronics.bot.model.Category;
import com.faendir.zachtronics.bot.model.Metric;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static com.faendir.zachtronics.bot.sz.model.SzCategory.SzScoreFormatStrings.*;
import static com.faendir.zachtronics.bot.sz.model.SzMetric.*;
import static com.faendir.zachtronics.bot.sz.model.SzType.STANDARD;

@Getter
public enum SzCategory implements Category {
    CP("CP", List.of(COST, POWER, LINES), F100, 101),
    CL("CL", List.of(COST, LINES, POWER), F100, 102),

    PC("PC", List.of(POWER, COST, LINES), F010, 201),
    PL("PL", List.of(POWER, LINES, COST), F010, 202),

    LC("LC", List.of(LINES, COST, POWER), F001, 301),
    LP("LP", List.of(LINES, POWER, COST), F001, 302);

    private final String displayName;
    private final List<Metric> metrics;
    private final Comparator<SzScore> scoreComparator;
    private final Set<SzType> supportedTypes = Collections.singleton(STANDARD);
    private final String formatStringLb;
    private final int repoSuffix;

    @SuppressWarnings("unchecked")
    SzCategory(String displayName, @NotNull List<SzMetric> metrics, String formatStringLb, int repoSuffix) {
        this.displayName = displayName;
        this.metrics = (List<Metric>)(List<?>) metrics;
        this.scoreComparator = metrics.stream()
                                      .map(SzMetric::getExtract)
                                      .map(Comparator::comparingInt)
                                      .reduce(Comparator::thenComparing)
                                      .orElseThrow();
        this.formatStringLb = formatStringLb;
        this.repoSuffix = repoSuffix;
    }

    public boolean supportsScore(@NotNull SzScore score) {
        return true;
    }

    static class SzScoreFormatStrings {
        static final String F100 = "**%d**/%d/%d";
        static final String F010 = "%d/**%d**/%d";
        static final String F001 = "%d/%d/**%d**";
    }
}
