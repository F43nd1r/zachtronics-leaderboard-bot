/*
 * Copyright (c) 2023
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

import com.faendir.zachtronics.bot.model.CategoryJava;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static com.faendir.zachtronics.bot.fp.model.FpMetric.*;
import static com.faendir.zachtronics.bot.fp.model.FpType.STANDARD;
import static com.faendir.zachtronics.bot.fp.model.FpType.TUTORIAL;

@Getter
public enum FpCategory implements CategoryJava<FpCategory, FpScore, FpMetric> {
    RCF("RCF", List.of(RULES, CONDITIONAL_RULES, FRAMES, WASTE), EnumSet.of(TUTORIAL, STANDARD), 0b1000),
    RFC("RFC", List.of(RULES, FRAMES, CONDITIONAL_RULES, WASTE), EnumSet.of(TUTORIAL, STANDARD), 0b1000),

    CRF("CRF", List.of(CONDITIONAL_RULES, RULES, FRAMES, WASTE), EnumSet.of(STANDARD), 0b0100),
    CFR("CFR", List.of(CONDITIONAL_RULES, FRAMES, RULES, WASTE), EnumSet.of(STANDARD), 0b0100),

    FRC("FRC", List.of(FRAMES, RULES, CONDITIONAL_RULES, WASTE), EnumSet.of(TUTORIAL, STANDARD), 0b0010),
    FCR("FCR", List.of(FRAMES, CONDITIONAL_RULES, RULES, WASTE), EnumSet.of(TUTORIAL, STANDARD), 0b0010),

    WRCF("WRCF", List.of(WASTE, RULES, CONDITIONAL_RULES, FRAMES), EnumSet.of(STANDARD), 0b0001),
    wRCF("wRCF", List.of(MOST_WASTE, RULES, CONDITIONAL_RULES, FRAMES), EnumSet.of(STANDARD), 0b0001),
    WFRC("WFRC", List.of(WASTE, FRAMES, RULES, CONDITIONAL_RULES), EnumSet.of(STANDARD), 0b0001),
    wFRC("wFRC", List.of(MOST_WASTE, FRAMES, RULES, CONDITIONAL_RULES), EnumSet.of(STANDARD), 0b0001);

    /** contains <tt>%dR%s%dC%s%dF%s%dW</tt> plus a bunch of <tt>*</tt> most likely */
    static final String[] FORMAT_STRINGS = {"%dR%s%dC%s%dF%s%dW",
                                            "%dR%s%dC%s%dF%s**%dW**",
                                            "%dR%s%dC%s**%dF**%s%dW", null,
                                            "%dR%s**%dC**%s%dF%s%dW", null, null, null,
                                            "**%dR**%s%dC%s%dF%s%dW"};

    private final String displayName;
    private final List<FpMetric> metrics;
    private final Comparator<FpScore> scoreComparator;
    private final Set<FpType> supportedTypes;
    private final int scoreFormatId;

    FpCategory(String displayName, @NotNull List<FpMetric> metrics, @NotNull Set<FpType> supportedTypes, int scoreFormatId) {
        this.displayName = displayName;
        this.metrics = metrics;
        this.scoreComparator = makeCategoryComparator(metrics);
        this.supportedTypes = supportedTypes;
        this.scoreFormatId = scoreFormatId;
    }

    @Override
    public boolean supportsScore(@NotNull FpScore score) {
        return true;
    }
}
