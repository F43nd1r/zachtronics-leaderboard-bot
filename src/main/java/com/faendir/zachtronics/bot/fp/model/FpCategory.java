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

package com.faendir.zachtronics.bot.fp.model;

import com.faendir.zachtronics.bot.model.Category;
import com.faendir.zachtronics.bot.model.Metric;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static com.faendir.zachtronics.bot.fp.model.FpMetric.*;
import static com.faendir.zachtronics.bot.fp.model.FpType.STANDARD;

@Getter
public enum FpCategory implements Category {
    RCRF("RCRF", List.of(RULES, CONDITIONAL_RULES, FRAMES), 0b100),
    RFCR("RFCR", List.of(RULES, FRAMES, CONDITIONAL_RULES), 0b100),

    CRRF("CRRF", List.of(CONDITIONAL_RULES, RULES, FRAMES), 0b010),
    CRFR("CRFR", List.of(CONDITIONAL_RULES, FRAMES, RULES), 0b010),

    FRCR("FRCR", List.of(FRAMES, RULES, CONDITIONAL_RULES), 0b001),
    FCRR("FCRR", List.of(FRAMES, CONDITIONAL_RULES, RULES), 0b001);

    /** contains <tt>%dR%s%dCR%s%dF%s%dW</tt> plus a bunch of <tt>*</tt> most likely */
    static final String[] FORMAT_STRINGS = {"%dR%s%dCR%s%dF%s%dW", "%dR%s%dCR%s**%d**F%s%dW",
                                            "%dR%s**%d**CR%s%dF%s%dW", null, "**%d**R%s%dCR%s%dF%s%dW"};

    private final String displayName;
    private final List<Metric> metrics;
    private final Comparator<FpScore> scoreComparator;
    private final Set<FpType> supportedTypes = Collections.singleton(STANDARD);
    private final int scoreFormatId;

    @SuppressWarnings("unchecked")
    FpCategory(String displayName, @NotNull List<FpMetric> metrics, int scoreFormatId) {
        this.displayName = displayName;
        this.metrics = (List<Metric>)(List<?>) metrics;
        this.scoreComparator = metrics.stream()
                                      .map(FpMetric::getExtract)
                                      .map(Comparator::comparingInt)
                                      .reduce(Comparator::thenComparing)
                                      .orElseThrow();
        this.scoreFormatId = scoreFormatId;
    }

    public boolean supportsScore(@NotNull FpScore score) {
        return true;
    }
}
